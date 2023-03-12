package com.vljx.hawkspeed

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.location.R
import com.google.android.gms.tasks.Tasks
import com.vljx.hawkspeed.data.socket.ServerInfoState
import com.vljx.hawkspeed.data.socket.WorldSocketPermissionState
import com.vljx.hawkspeed.data.socket.WorldSocketSession
import com.vljx.hawkspeed.data.socket.WorldSocketState
import com.vljx.hawkspeed.data.socket.requests.ConnectAuthenticationRequestDto
import com.vljx.hawkspeed.data.socket.requests.PlayerLocationRequestDto
import com.vljx.hawkspeed.data.socket.requests.PlayerUpdateRequestDto
import com.vljx.hawkspeed.data.socket.requests.StartRaceRequestDto
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.models.world.Viewport
import com.vljx.hawkspeed.models.world.WorldInitial
import com.vljx.hawkspeed.models.world.WorldInitial.Companion.ARG_WORLD_INITIAL
import com.vljx.hawkspeed.util.Extension.putExtra
import com.vljx.hawkspeed.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.util.concurrent.ExecutionException
import javax.inject.Inject

@AndroidEntryPoint
class WorldService: Service() {
    enum class WorldStatus(val idx: Int) {
        CONNECTING(-1),
        JOINED(0),
        UPDATE(1),
        LOCATION(2),
        ERROR(3),
        LEFT(4)
    }

    @Inject
    lateinit var worldSocketSession: WorldSocketSession

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

    // The latest viewport reported by any user of the world service.
    private val mutableLatestViewport: MutableStateFlow<Viewport?> = MutableStateFlow(null)

    // Objects for managing asynchronous operations.
    // Create a new job.
    private val job = SupervisorJob()
    // Create a new coroutine scope based on this job.
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // A boolean that indicates whether location updates are generally available.
    private var currentLocationAvailability: Boolean = false

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
            // TODO: move this as we only need foreground when app is active (?)
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
     * Update the latest viewport.
     */
    fun setLatestViewport(viewport: Viewport) {
        mutableLatestViewport.tryEmit(viewport)
    }

    /**
     * This function will begin the process of authenticating and connecting this service instance to the HawkSpeed world. If the internal structure has already
     * been instructed to be in a connected state to the world, nothing will happen (for now.)
     */
    @SuppressLint("MissingPermission") // TODO: suppressing MissingPermission warning, probably not a good idea.
    fun joinWorld(viewport: Viewport, forceRejoin: Boolean = false) {
        // Check the currently world permission state, if it is already CanJoin, do not do anything unless we are being forced.
        if(worldSocketSession.currentWorldSocketPermissionState is WorldSocketPermissionState.CanJoinWorld && !forceRejoin) {
            // Do nothing, since we are already connected.
            Timber.d("Skipped joining world once again, we are already joined.")
            return
        }
        // In our custom scope, launch a new coroutine that will actually connect to the server.
        scope.launch {
            // First, broadcast a connecting status.
            broadcastWorldState(WorldStatus.CONNECTING)
            try {
                // Get the most recent location availability status.
                try {
                    // We'll check our location availability now.
                    val locationAvailability: LocationAvailability = Tasks.await(
                        fusedLocationClient.locationAvailability
                    )
                    // Set location availability in the service.
                    currentLocationAvailability = locationAvailability.isLocationAvailable
                    Timber.d("Connecting to game server started, location available: ${locationAvailability.isLocationAvailable}")
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
                // Next, continue by getting the current location for this device. This is required for connecting to the server.
                val location: Location
                try {
                    // TODO: check currentLocationAvailability and decide on a coarse of action, depending on its value. Is it worth it to try and get a
                    // TODO: location irrespective???
                    // TODO: Customise the CUrrentLocationRequest further to include distance travelled.
                    location = Tasks.await(
                        fusedLocationClient.getCurrentLocation(
                            CurrentLocationRequest.Builder()
                                .setMaxUpdateAgeMillis(5000)
                                .build(),
                            null
                        )
                    )
                    // With this location, send this from service.
                    broadcastWorldState(WorldStatus.LOCATION, Bundle().apply {
                        putParcelable(ARG_LOCATION, location)
                    })
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
                // Build a connect/authentication request dto for the connection protocol.
                // TODO: this breaks clean arch, I think... Revise.
                val connectAuthenticationRequestDto = ConnectAuthenticationRequestDto(
                    viewport.minX,
                    viewport.minY,
                    viewport.maxX,
                    viewport.maxY,
                    location.latitude,
                    location.longitude,
                    location.bearing,
                    location.speed,
                    (location.time / 1000L).toInt()
                )
                // TODO: get entry token & target game server info, if provided and when implemented.
                // Update the world socket session to be aware of this configuration. All other requirements satisfied, this should trigger the connection procedure.
                worldSocketSession.updateServerInfo(
                    "TOKEN HERE",
                    "GAME SERVER INFO HERE",
                    connectAuthenticationRequestDto
                )
                // Now start a collection for the state of the world socket.
                worldSocketSession.worldSocketState.collect { worldSocketState: WorldSocketState ->
                    // The idea is, this collection will continue until the world socket state changes to disconnected, at which point, we will run disconnection
                    // logic, then cancel the coroutine; which will hopefully also cancel this collection...
                    when(worldSocketState) {
                        is WorldSocketState.Connected -> {
                            Timber.d("World service has reported that it has successfully connected to the World service!")
                            // Begin the location updates.
                            beginLocationUpdates()
                            // Broadcast that we are now joined to the game server.
                            broadcastWorldState(WorldStatus.JOINED, Bundle().apply {
                                putParcelable(ARG_WORLD_INITIAL, WorldInitial(
                                    worldSocketState.playerUid,
                                    worldSocketState.latitude,
                                    worldSocketState.longitude,
                                    worldSocketState.rotation
                                ))
                            })
                        }
                        is WorldSocketState.Connecting -> {
                            Timber.d("World service has reported that the connection to World service is in progress...")
                        }
                        is WorldSocketState.Disconnected -> {
                            Timber.d("World service has reported that connection to the World service is no longer available...")
                            cancel()
                        }
                    }
                }
            } catch(e: Exception) {
                // TODO: properly handle exceptions resulting from calls to requestJoinWorld()
                // TODO: properly handle exceptions resulting from beginLocationUpdates()
                // TODO: properly handle this exception.
                Timber.e(e)
                throw e
            }
        }
    }

    /**
     * Inform the server of the User's intent to start a new race. This function expects the location logged when the race's countdown was started, which will be used
     * to calculate deviation measurements for validating the circumstances of the race's start- serverside. This function is a suspend function as it is tied to the
     * lifecycle/scope of its caller.
     */
    @SuppressLint("MissingPermission")
    suspend fun startNewRace(trackUid: String, countdownStarted: Location) {
        // Begin by getting the current location, fresh to this instant.
        val location: Location = getCurrentLocation(
            CurrentLocationRequest.Builder()
                .setMaxUpdateAgeMillis(0)
                .build()
        )
        // Now, construct a start new race request.
        val startRaceRequestDto = StartRaceRequestDto(
            trackUid,
            location.latitude,
            location.longitude,
            location.bearing,
            location.speed,
            (location.time / 1000L).toInt(),
            PlayerLocationRequestDto(
                countdownStarted.latitude,
                countdownStarted.longitude,
                countdownStarted.bearing,
                countdownStarted.speed,
                (countdownStarted.time / 1000L).toInt()
            )
        )
        // Send this to the server.
        worldSocketSession.sendRaceRequest(startRaceRequestDto)
        // TODO: get a response from this request. If server has decided on a disqualification or false-start, raise an appropriate error here.
    }

    /**
     * Function responsible for leaving the world. This will attempt to communicate this to the remote server, as well as clearing all information on the
     * device about the world. This will also cease location updates being received.
     */
    fun leaveWorld() {
        Timber.d("Leaving the world...")
        // TODO: communicate to the server if isConnectedToGame is true.
        worldSocketSession.clearServerInfo()
        // Send out a LEFT status broadcast.
        // TODO: add reason as to why we left the world.
        broadcastWorldState(WorldStatus.LEFT, null)
    }

    /**
     *
     */
    fun stopWorldService() {
        Timber.d("Stopping world service...")
        // Leave the world.
        leaveWorld()
        // Stop receiving location updates.
        removeLocationUpdates()
        // Finally, stop the service.
        stopSelf()
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
            // With this location, send this from service.
            broadcastWorldState(WorldStatus.LOCATION, Bundle().apply {
                putParcelable(ARG_LOCATION, location)
            })
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

    /**
     * Commence location updates. This requests updates toward the location callback and performs initialisation operations.
     */
    @SuppressLint("MissingPermission") // TODO: suppressing MissingPermission warning, probably not a good idea.
    private fun beginLocationUpdates() {
        Timber.d("WorldService now beginning location updates...")
        try {
            // Now, request location updates.
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationReceived, serviceHandler.looper
            )
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
            // Remove location updates toward this callback.
            fusedLocationClient.removeLocationUpdates(locationReceived)
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
        // TODO: implement this properly.
        when(locationAvailability.isLocationAvailable) {
            true -> {
                Timber.d("Location availability to world service has changed to AVAILABLE!")
            }
            else -> {
                Timber.d("Location availability to world service has changed to NOT AVAILABLE!")
            }
        }
        // Update this service's awareness of location availability.
        currentLocationAvailability = locationAvailability.isLocationAvailable
        // TODO: we must still perform some work to inform the view of what has happened.
        // Send a communication to the server informing it of this change.
        // TODO: send a communication about this.
    }

    /**
     * Handle a new location being received from the device. This function will cause an update to be sent on the basis of the
     * given location result, if the location and current viewport is valid.
     */
    private fun newLocationReceived(locationResult: LocationResult) {
        val latestLocation: Location
        val latestViewport: Viewport
        try {
            // Get the latest viewport, and the location; and we should raise a npe if either is null.
            latestLocation = locationResult.lastLocation
                ?: throw NullPointerException()
            latestViewport = mutableLatestViewport.value
                ?: throw NullPointerException()
        } catch(npe: NullPointerException) {
            Timber.e("No player update sent - there is no last location received from updates, or there is no latest viewport.")
            // We'll return for now.
            return
        } catch(e: Exception) {
            throw e
        }
        // Now, with the location and viewport we will instantiate a new player update request.
        val playerUpdateRequest = PlayerUpdateRequestDto(
            latestViewport.minX,
            latestViewport.maxY,
            latestViewport.maxX,
            latestViewport.maxY,
            latestLocation.latitude,
            latestLocation.longitude,
            latestLocation.bearing,
            latestLocation.speed,
            (latestLocation.time / 1000L).toInt()
        )
        // Take this player update request and send it to the server now.
        worldSocketSession.sendPlayerUpdate(playerUpdateRequest)
        // Finally, we will broadcast this location to anyone who is listening.
        broadcastWorldState(WorldStatus.LOCATION, Bundle().apply {
            putParcelable(ARG_LOCATION, latestLocation)
        })
        /*if(locationResult.lastLocation != null && mutableLatestViewport.value != null) {
            Timber.d("WorldService has received a new location. Location: $locationResult")
            // Get the viewport.
            val currentViewport: Viewport = mutableLatestViewport.value
            // With this location result, instantiate a player update request.
            val playerUpdateRequest = PlayerUpdateRequestDto()
            // Now, use this service's scope to invoke a player update.
            scope.launch {
                playerUpdateUseCase(
                    PlayerUpdateRequest(
                        locationResult.lastLocation!!
                    )
                ).filter { it.status == Resource.Status.SUCCESS || it.status == Resource.Status.ERROR }.collect { worldUpdateResource ->
                    if(worldUpdateResource.status == Resource.Status.ERROR) {
                        handleErrorType(worldUpdateResource.resourceError)
                    } else {
                        // Build an intent indicating that we have received a world update.
                        val worldUpdateIntent = Intent(ACTION_WORLD_STATUS).apply {
                            putExtra(WorldStatus.UPDATE, ARG_WORLD_STATUS)
                            putExtra(ARG_WORLD_UPDATE, worldUpdateResource.data!!)
                        }
                        // Broadcast this intent.
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(worldUpdateIntent)
                    }
                }
            }
            broadcastWorldState(WorldStatus.LOCATION, Bundle().apply {
                putParcelable(ARG_LOCATION, locationResult.lastLocation!!)
            })
        } else {

        }*/
    }

    private fun handleErrorType(resourceError: ResourceError?) {
        // TODO: broadcast a LEFT status with this resource error or some clue as to its reason.
        Timber.e("HANDLE ERROR TYPE")
        throw NotImplementedError()
    }

    private fun broadcastWorldState(status: WorldStatus, desiredExtras: Bundle? = null) {
        val broadcastIntent = Intent(ACTION_WORLD_STATUS).apply {
            if(desiredExtras != null) {
                // Add all extras desired to the broadcast intent.
                putExtras(desiredExtras)
            }
            // Add an extra for the desired status.
            putExtra(status, ARG_WORLD_STATUS)
        }
        // Broadcast this intent.
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(broadcastIntent)
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

    inner class WorldServiceBinder: Binder() {
        fun getService(): WorldService = this@WorldService
    }

    companion object {
        fun newLocationRequest(): LocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        const val CHANNEL_ID = "com.vljx.hawkspeed.WorldService.NOTIFICATION"
        const val CHANNEL_NAME = "HawkSpeed"

        const val ONGOING_NOTIFICATION_ID = 8011997

        const val ACTION_WORLD_STATUS = "com.vljx.hawkspeed.WorldService.ACTION_WORLD_STATUS"
        const val ARG_WORLD_STATUS = "com.vljx.hawkspeed.WorldService.ARG_WORLD_STATUS"

        const val ARG_JOIN_WORLD = "com.vljx.hawkspeed.WorldService.ARG_JOIN_WORLD"
        const val ARG_WORLD_UPDATE = "com.vljx.hawkspeed.WorldService.ARG_WORLD_UPDATE"
        const val ARG_LOCATION = "com.vljx.hawkspeed.WorldService.ARG_LOCATION"
        const val ARG_LOCATION_AVAILABILITY = "com.vljx.hawkspeed.WorldService.ARG_LOCATION_AVAILABILITY"
        const val ARG_WORLD_ERROR = "com.vljx.hawkspeed.WorldService.ARG_WORLD_ERROR"

        const val ARG_STARTED_FROM_NOTIFICATION = "com.vljx.hawkspeed.WorldService.ARG_STARTED_FROM_NOTIFICATION"
    }
}