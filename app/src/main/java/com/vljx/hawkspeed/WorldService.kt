package com.vljx.hawkspeed

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.os.*
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Tasks
import com.vljx.hawkspeed.data.socket.WorldSocketSession
import com.vljx.hawkspeed.data.socket.requestmodels.*
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestLeaveWorld
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate
import com.vljx.hawkspeed.domain.usecase.socket.RequestLeaveWorldUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendPlayerUpdateUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SetLocationAvailabilityUseCase
import com.vljx.hawkspeed.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.ExecutionException
import javax.inject.Inject

/**
 * The world service component; maintains the responsibility of single source of truth for location updates. New locations and location availabilities will be passed
 * directly to the world socket session instance from here. Communication with this service should be avoided for HawkSpeed specific functionality. That should be done
 * via view models that communicate via use cases to world socket session.
 */
@AndroidEntryPoint
class WorldService: Service() {
    inner class WorldServiceBinder: Binder() {
        fun getService(): WorldService = this@WorldService
    }

    @Inject
    lateinit var sendPlayerUpdateUseCase: SendPlayerUpdateUseCase

    @Inject
    lateinit var requestLeaveWorldUseCase: RequestLeaveWorldUseCase

    @Inject
    lateinit var setLocationAvailabilityUseCase: SetLocationAvailabilityUseCase

    // A binder to provide on bind to clients.
    private val binder = WorldServiceBinder()
    // Access to the location client.
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // Provides parameters for the location request.
    private lateinit var locationRequest: LocationRequest
    // Handlers for changes in location.
    private lateinit var locationReceived: LocationCallback
    private lateinit var serviceHandler: Handler

    // The notification manager service.
    private lateinit var notificationManager: NotificationManager
    // The notification channel.
    private lateinit var notificationChannel: NotificationChannel

    // Objects for managing asynchronous operations.
    // Create a new job.
    private val job = SupervisorJob()
    // Create a new coroutine scope based on this job.
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // Whether or not we are currently receiving updates.
    private var receivingLocationUpdates: Boolean = false

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
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
        // Setup a location callback.
        locationReceived = object: LocationCallback() {
            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                locationAvailabilityChanged(locationAvailability)
                super.onLocationAvailability(locationAvailability)
            }

            override fun onLocationResult(locationResult: LocationResult) {
                newLocationReceived(locationResult)
                super.onLocationResult(locationResult)
            }
        }
        // Create the location request.
        locationRequest = newLocationRequest()
        // Now, create a new thread to handle the location updates.
        val serviceHandlerThread = HandlerThread(WorldService::class.java.simpleName)
            .apply {
                start()
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
                ARG_STARTED_FROM_NOTIFICATION, false) ?: false
            if(startedFromNotification) {
                Timber.d("WorldService has been started from the notification!")
                // TODO: onStartCommand invoked by notification.
                throw NotImplementedError("WorldService.onStartCommand being invoked via notification is NOT YET handled!")
            }
            // Begin receiving location updates.
            beginLocationUpdates()
            // Start the service in the foreground now.
            startForeground(ONGOING_NOTIFICATION_ID, getNotification())
        } catch(se: SecurityException) {
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
        // Cancel the job.
        job.cancel()
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
        requestLeaveWorldUseCase(
            RequestLeaveWorld()
        )
    }

    /**
     * Stop the world service completely. This is to be called when the User is exiting the app, or they have decided to revoke permissions or change settings
     * that affect their eligibility.
     */
    fun stopWorldService() {
        Timber.d("Stopping world service...")
        // Stop receiving location updates.
        removeLocationUpdates()
        // Leave the world.
        leaveWorld()
        // Finally, stop the service.
        stopSelf()
    }

    /**
     * Commence location updates. This requests updates toward the location callback and performs initialisation operations.
     */
    @SuppressLint("MissingPermission") // TODO: suppressing MissingPermission warning, probably not a good idea.
    private fun beginLocationUpdates() {
        Timber.d("WorldService now beginning location updates...")
        try {
            // Now, request location updates.
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationReceived,
                serviceHandler.looper
            )
            receivingLocationUpdates = true
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
            scope.launch {
                // TODO: properly handle this, not implemented error will be called when sendPlayerUpdate is called but socket is not connected.
                try {
                    sendPlayerUpdateUseCase(
                        RequestPlayerUpdate(playerPosition)
                    )
                } catch (iee: NotImplementedError) {
                    Timber.w(iee)
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
     * Get the location availability and return it. This will run the task blocking.
     */
    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocationAvailability(): Boolean {
        // Get the most recent location availability status.
        try {
            // We'll check our location availability now.
            val locationAvailability: LocationAvailability = Tasks.await(
                fusedLocationClient.locationAvailability
            )
            return locationAvailability.isLocationAvailable
        } catch (ee: ExecutionException) {
            // TODO: exception occurred whilst running task.
            // TODO: This is the same exception we'd get in the handler.
            Timber.e(ee)
            throw NotImplementedError("Getting location availability failed! ANd is also not implemented.")
        } catch (ie: InterruptedException) {
            Timber.w("Getting location availability was interrupted!")
            // TODO: implement a proper handler here.
            throw NotImplementedError("Failed to get location availability because it was interrupted - this is not yet handled.")
        }
    }

    /**
     * Run a blocking get current location call for a Location instance matching the settings given by the location request. This function will
     * either return the desired location, or will throw an exception.
     */
    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(currentLocationRequest: CurrentLocationRequest): Location {
        val location: Location
        try {
            // Await the query for the current location.
            location = Tasks.await(
                fusedLocationClient.getCurrentLocation(
                    currentLocationRequest,
                    null
                )
            )
            // Return the location.
            return location
        } catch (ee: ExecutionException) {
            // TODO: exception occurred whilst running task.
            // TODO: This is the same exception we'd get in the handler.
            Timber.e(ee)
            throw NotImplementedError("Getting current location failed! ANd is also not implemented.")
        } catch (it: InterruptedException) {
            Timber.w("Getting current location was interrupted!")
            // TODO: implement a proper handler here.
            throw NotImplementedError("Failed to get current location because it was interrupted - this is not yet handled.")
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
            .setSmallIcon(com.vljx.hawkspeed.R.mipmap.ic_launcher)
            .setWhen(System.currentTimeMillis())
        // Build and return the notification.
        return notificationBuilder.build()
    }

    companion object {
        fun newLocationRequest(): LocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        const val CHANNEL_ID = "com.vljx.hawkspeed.WorldService.NOTIFICATION"
        const val CHANNEL_NAME = "HawkSpeed"
        const val ONGOING_NOTIFICATION_ID = 8011997
        const val ARG_STARTED_FROM_NOTIFICATION = "com.vljx.hawkspeed.WorldService.ARG_STARTED_FROM_NOTIFICATION"
    }
}