package com.vljx.hawkspeed

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import android.hardware.SensorManager.SENSOR_DELAY_UI
import android.location.Location
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkRequest
import android.os.*
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.DetectedActivity.IN_VEHICLE
import com.google.android.gms.location.DetectedActivity.ON_BICYCLE
import com.google.android.gms.location.DetectedActivity.ON_FOOT
import com.google.android.gms.location.DetectedActivity.STILL
import com.google.android.gms.tasks.Tasks
import com.vljx.hawkspeed.Extension.addDetectedActivity
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.data.di.scope.BackgroundScope
import com.vljx.hawkspeed.data.socket.WorldSocketSession
import com.vljx.hawkspeed.data.socket.requestmodels.*
import com.vljx.hawkspeed.domain.exc.socket.NotConnectedException
import com.vljx.hawkspeed.domain.models.world.ActivityTransitionUpdates
import com.vljx.hawkspeed.domain.models.world.LocationUpdateRate
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestLeaveWorld
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateAccelerometerReadings
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateMagnetometerReadings
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateNetworkConnectivity
import com.vljx.hawkspeed.domain.usecase.socket.RequestLeaveWorldUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendPlayerUpdateUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SetActivityTransitionUpdatesUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SetLocationAvailabilityUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SetLocationUpdateRateUseCase
import com.vljx.hawkspeed.domain.usecase.socket.UpdateAccelerometerReadingsUseCase
import com.vljx.hawkspeed.domain.usecase.socket.UpdateMagnetometerReadingsUseCase
import com.vljx.hawkspeed.domain.usecase.socket.UpdateNetworkConnectivityUseCase
import com.vljx.hawkspeed.ui.MainActivity
import com.vljx.hawkspeed.util.Extension.applyLowPass
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.ExecutionException
import javax.inject.Inject

/**
 * The world service component; maintains the responsibility of single source of truth for location and sensor updates. New locations location availabilities and sensor
 * readings will be passed directly to the world socket session instance from here. Communication with this service should be avoided for HawkSpeed specific functionality.
 * That should be done via view models that communicate via use cases to world socket session.
 */
@AndroidEntryPoint
class WorldService: Service(), SensorEventListener {
    inner class WorldServiceBinder: Binder() {
        fun getService(): WorldService = this@WorldService
    }

    @Inject
    lateinit var backgroundScope: BackgroundScope
    @Inject
    lateinit var sendPlayerUpdateUseCase: SendPlayerUpdateUseCase
    @Inject
    lateinit var requestLeaveWorldUseCase: RequestLeaveWorldUseCase
    @Inject
    lateinit var setLocationAvailabilityUseCase: SetLocationAvailabilityUseCase
    @Inject
    lateinit var setLocationUpdateRateUseCase: SetLocationUpdateRateUseCase
    @Inject
    lateinit var setActivityTransitionUpdatesUseCase: SetActivityTransitionUpdatesUseCase
    @Inject
    lateinit var updateNetworkConnectivityUseCase: UpdateNetworkConnectivityUseCase
    @Inject
    lateinit var updateAccelerometerReadingsUseCase: UpdateAccelerometerReadingsUseCase
    @Inject
    lateinit var updateMagnetometerReadingsUseCase: UpdateMagnetometerReadingsUseCase

    // A binder to provide on bind to clients.
    private val binder = WorldServiceBinder()
    // Access to the activity recognition client.
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    // Construct a new pending intent for receiving broadcast updates about activity transition changes.
    private lateinit var transitionPendingIntent: PendingIntent
    // Sensor manager, so we can access accelerometer and magnetometer etc.
    private lateinit var sensorManager: SensorManager
    // Get the connectivity manager.
    private lateinit var connectivityManager: ConnectivityManager
    // Access to the location client.
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // Provides parameters for the location request.
    private lateinit var locationRequest: LocationRequest
    // Handler for connectivity changes.
    private lateinit var networkCallback: NetworkCallback
    // Handlers for changes in location.
    private lateinit var locationReceived: LocationCallback
    private lateinit var serviceHandler: Handler
    // Receiver for transitions.
    private lateinit var transitionBroadcastReceiver: BroadcastReceiver

    // The notification manager service.
    private lateinit var notificationManager: NotificationManager
    // The notification channel.
    private lateinit var notificationChannel: NotificationChannel

    // Storing our most recent accelerometer readings.
    private var accelerometerReadings = FloatArray(3)
    // Storing our most recent magnetometer readings.
    private var magnetometerReadings = FloatArray(3)
    // Whether or not we are currently receiving updates.
    private var receivingLocationUpdates: Boolean = false

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        // Get the sensor manager.
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        // Get the connectivity manager.
        connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        // Setup location client.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Get the notification manager.
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Build a notification channel for the app.
        notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
            lightColor = Color.BLUE
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        notificationManager.createNotificationChannel(notificationChannel)
        // Setup a network callback.
        networkCallback = object: NetworkCallback() {
            override fun onAvailable(network: Network) =
                onNetworkAvailable(network)

            override fun onLost(network: Network) =
                onNetworkLost(network)

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) = onNetworkCapabilitiesChanged(network, networkCapabilities)

            override fun onLinkPropertiesChanged(
                network: Network,
                linkProperties: LinkProperties
            ) = onNetworkLinkPropertiesChanged(network, linkProperties)
        }
        // Setup a location callback.
        locationReceived = object: LocationCallback() {
            override fun onLocationAvailability(locationAvailability: LocationAvailability) =
                locationAvailabilityChanged(locationAvailability)

            override fun onLocationResult(locationResult: LocationResult) =
                newLocationReceived(locationResult)
        }
        // Now, create a new thread to handle the location updates.
        val serviceHandlerThread = HandlerThread(WorldService::class.java.simpleName)
            .apply {
                start()
            }
        // Setup activity recognition client.
        activityRecognitionClient = ActivityRecognition.getClient(this)
        // Setup broadcast receiver for activity transitions.
        transitionBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Attempt to do something with activity transition result; but first ensure one was even sent.
                if(ActivityTransitionResult.hasResult(intent)) {
                    val activityTransitionResult = ActivityTransitionResult.extractResult(
                        intent ?: throw Exception("Intent is NULL, yet passed hasResult call?")
                    ) ?: throw NotImplementedError("When getting an activity transition result, extractResult() returned null and this is not yet handled.")
                    // Now, pass these to world service handler.
                    activityTransitionEventsReceived(
                        activityTransitionResult.transitionEvents
                    )
                }
            }
        }
        // Instantiate the service handler toward this thread.
        serviceHandler = Handler(serviceHandlerThread.looper)
    }

    /**
     * Sending a start command to the world service will bring it to the foreground, and begin location tracking operations. It will not connect the client to the
     * game server, however.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            Timber.d("World service has received a start command...")
            // Determine whether the start command was invoked by the notification.
            val startedFromNotification: Boolean = intent?.getBooleanExtra(
                ARG_STARTED_FROM_NOTIFICATION, false
            ) ?: false
            if (startedFromNotification) {
                Timber.d("WorldService has been started from the notification!")
                // TODO: onStartCommand invoked by notification.
                throw NotImplementedError("WorldService.onStartCommand being invoked via notification is NOT YET handled!")
            }
            // Begin receiving activity transition updates.
            beginActivityTransitionUpdates()
            // Begin receiving network connectivity updates.
            beginNetworkConnectivityUpdates()
            // Begin receiving sensor changes.
            beginSensorUpdates()
            // Begin receiving location updates.
            beginLocationUpdates()
            // Start the service in the foreground now.
            startForeground(ONGOING_NOTIFICATION_ID, getNotification())
        } catch (se: SecurityException) {
            // TODO: emit a world service failure status indicating that there's a locational permission error.
            Timber.e(se)
            throw NotImplementedError()
        }
        // Return not sticky, so the service does not attempt restart if process killed.
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        Timber.d("WorldService has been bound...")
        return binder
    }

    override fun onRebind(intent: Intent?) {
        Timber.d("WorldService has been rebound...")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("WorldService has been unbound...")
        return true
    }

    override fun onDestroy() {
        Timber.d("WorldService is being destroyed...")
        // Remove all callbacks and messages from the handler.
        serviceHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    /**
     * Function responsible for leaving the world. This will attempt to communicate this to the remote server, as well as clearing all information on the
     * device about the world. This will also cease location updates being received.
     */
    fun leaveWorld() {
        Timber.d("Leaving the world...")
        // Instructed to leave world, we'll revoke intent to connect to the game server.
        // TODO: can add reason here.
        requestLeaveWorldUseCase(RequestLeaveWorld())
    }

    /**
     * Stop the world service completely. This is to be called when the User is exiting the app, or they have decided to revoke permissions or change settings
     * that affect their eligibility.
     */
    fun stopWorldService() {
        Timber.d("Stopping world service...")
        // Stop receiving activity transition updates.
        stopActivityTransitionUpdates()
        // Stop receiving network connectivity updates.
        stopNetworkConnectivityUpdates()
        // Stop receiving sensor changes.
        removeSensorUpdates()
        // Stop receiving location updates.
        removeLocationUpdates()
        // Leave the world.
        leaveWorld()
        // Finally, stop the service.
        stopSelf()
    }

    /**
     * Begin receiving activity transition updates.
     */
    @SuppressLint("MissingPermission")
    private fun beginActivityTransitionUpdates() {
        try {
            // Register a receiver for receiver action.
            registerReceiver(
                transitionBroadcastReceiver,
                IntentFilter(TRANSITIONS_RECEIVER_ACTION)
            )
            // Setup the pending intent for activity transitions.
            transitionPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(TRANSITIONS_RECEIVER_ACTION),
                PendingIntent.FLAG_IMMUTABLE
            )
            // Create a new request for activity transition changes.
            val transitionRequest = ActivityTransitionRequest(getActivityTransitionList())
            // Actually register for updates now.
            activityRecognitionClient.requestActivityTransitionUpdates(transitionRequest, transitionPendingIntent)
                .addOnSuccessListener {
                    Timber.d("WorldService successfully began receiving activity transition updates...")
                    setActivityTransitionUpdatesUseCase(
                        ActivityTransitionUpdates(true)
                    )
                }
                .addOnFailureListener { exception ->
                    Timber.e("WorldService could NOT begin receiving activity transition updates.")
                    throw exception
                }
        } catch(se: SecurityException) {
            Timber.d("SecurityException thrown when trying to request activity transition updates. Permission has probably not been granted. We'll set a static update rate.")
            // Not receiving activity transition updates.
            setActivityTransitionUpdatesUseCase(ActivityTransitionUpdates(false))
            // Restart location updates with the Static location update rate.
            changeLocationUpdateRate(LocationUpdateRate.Static)
        }
    }

    /**
     * Stop receiving activity transition updates.
     */
    private fun stopActivityTransitionUpdates() {
        try {
            // Unregister receiver.
            unregisterReceiver(transitionBroadcastReceiver)
            // Remove transition updates.
            activityRecognitionClient.removeActivityTransitionUpdates(transitionPendingIntent)
            setActivityTransitionUpdatesUseCase(ActivityTransitionUpdates(false))
            Timber.d("Activity transition updates are no longer being received.")
        } catch(se: SecurityException) {
            // Failed to stop updates because of permission, but surely that means they were never started in the first place.
            setActivityTransitionUpdatesUseCase(ActivityTransitionUpdates(false))
        }
    }

    /**
     * Register a callback for network connectivity change updates to this service.
     */
    private fun beginNetworkConnectivityUpdates() {
        connectivityManager.registerNetworkCallback(newNetworkRequest(), networkCallback)
    }

    /**
     * Remove callback from network connectivity change updates.
     */
    private fun stopNetworkConnectivityUpdates() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    /**
     * Register listeners for the accelerometer and magnetometer sensors.
     */
    private fun beginSensorUpdates() {
        // Register a listener for the accelerometer.
        sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI)
        }
        // Register a listener for the magnetometer.
        sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(this, magneticField, SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI)
        }
    }

    /**
     * Remove our listeners for the accelerometer and magnetometer sensors.
     */
    private fun removeSensorUpdates() {
        // Remove listeners for sensor manager as a whole.
        sensorManager.unregisterListener(this)
    }

    /**
     * Commence location updates. This requests updates toward the location callback and performs initialisation operations.
     */
    @SuppressLint("MissingPermission")
    private fun beginLocationUpdates(
        desiredLocationUpdateRate: LocationUpdateRate = LocationUpdateRate.Default
    ) {
        Timber.d("WorldService now beginning location updates...")
        try {
            // Set new location request.
            locationRequest = newLocationRequest(
                locationPriority = desiredLocationUpdateRate.priority,
                desiredIntervalMillis = desiredLocationUpdateRate.updateIntervalMillis,
                minUpdateDistanceMeters = desiredLocationUpdateRate.minUpdateMeters,
                minUpdateDistanceIntervalMillis = desiredLocationUpdateRate.minUpdateIntervalMillis
            )
            // Now, request location updates.
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationReceived,
                serviceHandler.looper
            )
            // Set receiving location updates to true.
            receivingLocationUpdates = true
            // Update world socket state with latest location update rate.
            setLocationUpdateRateUseCase(desiredLocationUpdateRate)
        } catch (npe: java.lang.NullPointerException) {
            // TODO: this can be caused by 'invalid null looper' which is when the looper is actually running but the activity has finished underneath it.
            Timber.e(npe)
            throw npe
        } catch(se: SecurityException) {
            Timber.e(se)
            // TODO: security exception
            throw NotImplementedError("beginLocationUpdates throwing SecurityException (permissions no longer compatible) is not yet handled!")
        }
    }

    /**
     * Restart location updates with a new location request.
     */
    private fun changeLocationUpdateRate(desiredLocationUpdateRate: LocationUpdateRate) {
        Timber.d("Changing location request for getting updates...")
        // First, stop location updates.
        removeLocationUpdates()
        // Restart location updates.
        beginLocationUpdates(desiredLocationUpdateRate = desiredLocationUpdateRate)
    }

    /**
     * Simply stops receiving location updates.
     */
    private fun removeLocationUpdates() {
        try {
            if(receivingLocationUpdates) {
                // Remove location updates toward this callback.
                fusedLocationClient.removeLocationUpdates(locationReceived)
                receivingLocationUpdates = false
            }
        } catch(se: SecurityException) {
            Timber.e(se)
            // TODO: handle security exception.
            throw NotImplementedError("removeLocationUpdates throwing SecurityException (could not stop location updates because permissions no longer compatible) is not yet handled!")
        }
    }

    /**
     * A list of activity transition events have been received, this function will parse them and modify service running configuration accordingly. Essentially, if device reports the user
     * is in a vehicle; the fastest location updates will be requested. Then bicycle, then on foot and finally, still; which is just 'keep alive' updates honestly.
     */
    private fun activityTransitionEventsReceived(transitionEvents: List<ActivityTransitionEvent>) {
        for(event in transitionEvents) {
            Timber.d("Device recognises current activity: ${event.activityType}")
            when(event.activityType) {
                /**
                 * We can reset back to default location request.
                 */
                STILL -> changeLocationUpdateRate(LocationUpdateRate.Default)
                /**
                 * Set location request to request updates slightly faster, this time with high priority accuracy.
                 */
                ON_FOOT -> changeLocationUpdateRate(LocationUpdateRate.SlowMoving)
                /**
                 * Get fastest location updates possible.
                 */
                ON_BICYCLE, IN_VEHICLE -> changeLocationUpdateRate(LocationUpdateRate.Fast)
            }
        }
    }

    /**
     * Called when the current network in use has become available.
     */
    private fun onNetworkAvailable(network: Network) {
        updateNetworkConnectivityUseCase(
            RequestUpdateNetworkConnectivity(
                true
            )
        )
    }

    /**
     * Called when the network in use has been lost.
     */
    private fun onNetworkLost(network: Network) {
        updateNetworkConnectivityUseCase(
            RequestUpdateNetworkConnectivity(
                false
            )
        )
    }

    /**
     * Called when the capabilities list for the network in use has changed. We should verify that we are still able to communicate within
     * minimum requirements for HawkSpeed.
     */
    private fun onNetworkCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
    ) {

    }

    /**
     *
     */
    private fun onNetworkLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {

    }

    /**
     * Location availability has changed, if this is false, this means we may not be receiving location updates for some unknown amount of time. If it is true, this means
     * we have regained location availability back. Both states should be communicated to both the server and the view.
     */
    private fun locationAvailabilityChanged(locationAvailability: LocationAvailability) {
        when(locationAvailability.isLocationAvailable) {
            true -> Timber.d("Location availability to world service has changed to AVAILABLE!")
            else -> Timber.d("Location availability to world service has changed to NOT AVAILABLE!")
        }
        // Update this service's awareness of location availability.
        setLocationAvailabilityUseCase(locationAvailability.isLocationAvailable)
    }

    /**
     * Handle a new location being received from the device. This function will cause an update to be sent on the basis of the
     * given location result, if the location and current viewport is valid.
     */
    private fun newLocationReceived(locationResult: LocationResult) {
        val latestLocation: Location
        try {
            latestLocation = locationResult.lastLocation
                ?: throw NullPointerException()
            // Create a new player position from this.
            val playerPosition = PlayerPosition(latestLocation)
            // Whenever we get a new location from the service, pass it directly to the world socket session.
            backgroundScope.launch {
                try {
                    sendPlayerUpdateUseCase(RequestPlayerUpdate(playerPosition))
                } catch (nce: NotConnectedException) {
                    // Do nothing, update wasn't sent to server, but current location was stored within socket stat.e
                }
            }
        } catch(npe: NullPointerException) {
            Timber.e("No player update sent - there is no last location received from updates.")
            // We'll return for now.
            return
        } catch(e: Exception) {
            throw e
        }
    }

    /**
     * Accuracy refers to sensor accuracy changes, not location.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    /**
     * Called by the system when a sensor changes. This is where we'll copy the latest values for the given sensor, and pass them to the world socket state
     * for further use.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        when(event?.sensor?.type) {
            TYPE_ACCELEROMETER -> {
                // Get accelerometer readings from the event, and send through our low pass filter.
                accelerometerReadings = applyLowPass(event.values.clone(), accelerometerReadings)
                // Now, call the update use case to send the latest readings off.
                updateAccelerometerReadingsUseCase(
                    RequestUpdateAccelerometerReadings(accelerometerReadings)
                )
            }
            TYPE_MAGNETIC_FIELD -> {
                // Get magnetometer readings from the event, and send through our low pass filter.
                magnetometerReadings = applyLowPass(event.values.clone(), magnetometerReadings)
                // Now, call the update use case to send the latest readings off.
                updateMagnetometerReadingsUseCase(
                    RequestUpdateMagnetometerReadings(magnetometerReadings)
                )
            }
        }
    }

    private fun getNotification(): Notification {
        // Setup an intent to differentiate between accessing onStartCommand via the notification.
        val intent = Intent(this, WorldService::class.java).apply {
            putExtra(ARG_STARTED_FROM_NOTIFICATION, true)
        }
        // Pending intent that leads to the onStartCommand call.
        val servicePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        // Pending intent to launch the activity.
        // TODO: Change the Activity referred to when tapping notiication?
        val activityPendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        // Now a new notification builder.
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .addAction(0, "LAUNCH", activityPendingIntent) // TODO: string for notification_action_launch
            .addAction(0, "STOP", servicePendingIntent) // TODO: string for notification_action_stop
            .setContentTitle("HawkSpeed") // TODO: strng for content title
            .setContentText("This is a cool text") // TODO: string for content text
            .setOngoing(true)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setWhen(System.currentTimeMillis())
        // Build and return the notification.
        return notificationBuilder.build()
    }

    companion object {
        const val CHANNEL_ID = "com.vljx.hawkspeed.WorldService.NOTIFICATION"
        const val CHANNEL_NAME = "HawkSpeed"
        const val ONGOING_NOTIFICATION_ID = 8011997
        const val ARG_STARTED_FROM_NOTIFICATION = "com.vljx.hawkspeed.WorldService.ARG_STARTED_FROM_NOTIFICATION"
        const val TRANSITIONS_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION"

        /**
         * Build a new location request, that will, at minimum, generate a location update every 10000 seconds by default with balanced power accuracy.
         * But call this function again to overwrite those parameters for a new request.
         */
        fun newLocationRequest(
            locationPriority: Int = Priority.PRIORITY_HIGH_ACCURACY,
            desiredIntervalMillis: Long = 10000,
            minUpdateDistanceMeters: Float = 0f,
            minUpdateDistanceIntervalMillis: Long = 3000L
        ): LocationRequest =
            LocationRequest.Builder(
                locationPriority,
                desiredIntervalMillis
            )
                .setMinUpdateDistanceMeters(minUpdateDistanceMeters)
                .setMinUpdateIntervalMillis(minUpdateDistanceIntervalMillis)
                .build()

        /**
         * Build a new network request that will require a network with internet capability and either mobile data or wifi transport.
         * TODO: move this elsewhere, ideally we want these checks done/running before even authentication is attempted.
         */
        fun newNetworkRequest(): NetworkRequest =
            NetworkRequest.Builder()
                .addCapability(NET_CAPABILITY_INTERNET)
                .addTransportType(TRANSPORT_WIFI)
                .addTransportType(TRANSPORT_CELLULAR)
                .build()

        /**
         * Return a list of activity transitions to keep track of and receive updates for.
         */
        fun getActivityTransitionList(): List<ActivityTransition> =
            mutableListOf<ActivityTransition>()
                .addDetectedActivity(STILL)
                .addDetectedActivity(ON_FOOT)
                .addDetectedActivity(ON_BICYCLE)
                .addDetectedActivity(IN_VEHICLE)
    }
}