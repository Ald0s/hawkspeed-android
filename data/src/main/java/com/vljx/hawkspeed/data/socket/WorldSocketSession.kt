package com.vljx.hawkspeed.data.socket

import android.hardware.SensorManager
import com.google.gson.Gson
import com.vljx.hawkspeed.data.BuildConfig
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.data.models.race.CancelRaceResultModel
import com.vljx.hawkspeed.data.models.race.StartRaceResultModel
import com.vljx.hawkspeed.data.models.world.PlayerUpdateResultModel
import com.vljx.hawkspeed.data.models.world.ViewportUpdateResultModel
import com.vljx.hawkspeed.data.socket.Extension.on
import com.vljx.hawkspeed.data.socket.Extension.sendMessage
import com.vljx.hawkspeed.data.socket.Extension.toMap
import com.vljx.hawkspeed.data.socket.mapper.race.CancelRaceResponseDtoMapper
import com.vljx.hawkspeed.data.socket.mapper.race.RaceDisqualifiedDtoMapper
import com.vljx.hawkspeed.data.socket.mapper.race.RaceFinishedDtoMapper
import com.vljx.hawkspeed.data.socket.mapper.race.RaceProgressDtoMapper
import com.vljx.hawkspeed.data.socket.mapper.race.StartRaceResponseDtoMapper
import com.vljx.hawkspeed.data.socket.mapper.world.PlayerUpdateResultDtoMapper
import com.vljx.hawkspeed.data.socket.mapper.world.ViewportUpdateResultDtoMapper
import com.vljx.hawkspeed.data.socket.models.*
import com.vljx.hawkspeed.data.socket.models.race.CancelRaceResponseDto
import com.vljx.hawkspeed.data.socket.models.race.RaceDisqualifiedDto
import com.vljx.hawkspeed.data.socket.models.race.RaceFinishedDto
import com.vljx.hawkspeed.data.socket.models.race.RaceProgressDto
import com.vljx.hawkspeed.data.socket.models.race.StartRaceResponseDto
import com.vljx.hawkspeed.data.socket.models.world.ConnectAuthenticationResponseDto
import com.vljx.hawkspeed.data.socket.models.world.PlayerUpdateResponseDto
import com.vljx.hawkspeed.data.socket.models.world.ViewportUpdateResponseDto
import com.vljx.hawkspeed.data.socket.requestmodels.RequestCancelRaceDto
import com.vljx.hawkspeed.data.socket.requestmodels.RequestConnectAuthenticationDto
import com.vljx.hawkspeed.data.socket.requestmodels.RequestPlayerLocationDto
import com.vljx.hawkspeed.data.socket.requestmodels.RequestStartRaceDto
import com.vljx.hawkspeed.data.socket.requestmodels.RequestViewportDto
import com.vljx.hawkspeed.data.socket.requestmodels.RequestViewportUpdateDto
import com.vljx.hawkspeed.data.source.account.AccountRemoteData
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.di.scope.ApplicationScope
import com.vljx.hawkspeed.domain.models.world.DeviceOrientation
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.Viewport
import com.vljx.hawkspeed.domain.requestmodels.race.RequestCancelRace
import com.vljx.hawkspeed.domain.requestmodels.race.RequestStartRace
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateAccelerometerReadings
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateMagnetometerReadings
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestViewportUpdate
import com.vljx.hawkspeed.domain.states.socket.WorldSocketState
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.internal.cookieToString
import org.json.JSONObject
import timber.log.Timber
import java.net.URI
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The world socket session class that centralises communication with the HawkSpeed game server.
 */
@Singleton
class WorldSocketSession @Inject constructor(
    private val applicationScope: ApplicationScope,
    private val cookieJar: CookieJar,
    private val gson: Gson,

    private val accountRemoteData: AccountRemoteData,

    private val startRaceResponseDtoMapper: StartRaceResponseDtoMapper,
    private val cancelRaceResponseDtoMapper: CancelRaceResponseDtoMapper,
    private val raceFinishedDtoMapper: RaceFinishedDtoMapper,
    private val raceDisqualifiedDtoMapper: RaceDisqualifiedDtoMapper,
    private val raceProgressDtoMapper: RaceProgressDtoMapper,
    private val playerUpdateResultDtoMapper: PlayerUpdateResultDtoMapper,
    private val viewportUpdateResultDtoMapper: ViewportUpdateResultDtoMapper,

    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
) {
    // Objects for the socket IO server.
    // The socket IO manager.
    private var socketManager: Manager? = null
    // The current client socket in use.
    private var socket: Socket? = null

    /**
     * A mutable state flow for the current state of the connection to the game server.
     */
    private val mutableWorldSocketState: MutableStateFlow<WorldSocketState> = MutableStateFlow(WorldSocketState.Disconnected())

    /**
     * A state flow for the mechanical gatekeeper to game server connection. This boolean should be used to carefully control when, though all arguments are
     * valid for a connection, should a connection be attempted.
     */
    private val mutableShouldConnect: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * A state flow for the current game settings this client is configured to use while connecting to the socket server.
     */
    private val mutableGameSettings: MutableStateFlow<GameSettings?> = MutableStateFlow(null)

    /**
     * A state flow for the current location, or whichever was most recently sent.
     */
    private val mutableCurrentLocation: MutableStateFlow<PlayerPosition?> = MutableStateFlow(null)

    /**
     * A state flow for the current location availability.
     */
    private val mutableLocationAvailability: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * A state flow for the latest viewport.
     */
    private val mutableLatestViewport: MutableStateFlow<Viewport?> = MutableStateFlow(null)

    /**
     * A mutable shared flow for the latest accelerometer readings. Configured to reply 1 value.
     */
    private val mutableLatestAccelerometerReadings: MutableSharedFlow<RequestUpdateAccelerometerReadings> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * A mutable shared flow for the latest magnetometer readings. Configured to reply 1 value.
     */
    private val mutableLatestMagnetometerReadings: MutableSharedFlow<RequestUpdateMagnetometerReadings> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * A state flow for the current state of the connection to the game server.
     */
    val worldSocketState: SharedFlow<WorldSocketState> =
        mutableWorldSocketState

    /**
     * The current location.
     */
    val currentLocation: StateFlow<PlayerPosition?> =
        mutableCurrentLocation

    /**
     * The current game settings.
     */
    val currentGameSettings: StateFlow<GameSettings?> =
        mutableGameSettings

    /**
     * The current viewport.
     */
    val latestViewport: StateFlow<Viewport?> =
        mutableLatestViewport

    /**
     * A flow, which is a zip of the latest accelerometer and magnetometer readings used to calculate a rotation matrix. We will utilise zip since we wish a new
     * rotation matrix emitted only when we have new readings from both accelerometer and magnetometer.
     */
    private val latestRotationMatrix: Flow<FloatArray> =
        mutableLatestAccelerometerReadings.zip(mutableLatestMagnetometerReadings) { accelerometerReadings, magnetometerReadings ->
            // Make a new float array, 9 in size.
            val rotationMatrixResult = FloatArray(9)
            // Now use sensor manager to actually calculate the rotation matrix. I'm not sure if this breaks clean arch because we reference Android here, but its
            // static so surely I'm good?
            SensorManager.getRotationMatrix(
                rotationMatrixResult,
                null,
                accelerometerReadings.latestReadings,
                magnetometerReadings.latestReadings
            )
            rotationMatrixResult
        }

    /**
     * A flow which maps the latest rotation matrix to the actual orientation angles. This is a public flow, and is not shared but upstream seems
     * relatively inexpensive.
     */
    val latestOrientationAngles: Flow<DeviceOrientation> =
        latestRotationMatrix.map { rotationMatrix ->
            // Create a float array of 3 values.
            val orientationAngles = FloatArray(3)
            // Now use sensor manager to actually calculate them, with given rotation matrix.
            SensorManager.getOrientation(
                rotationMatrix,
                orientationAngles
            )
            // Create and return a device orientation container for the angles.
            DeviceOrientation(orientationAngles)
        }

    /**
     * A combined flow of current game settings and current location. This flow will determine the overall intent to connect to the game server and
     * will emit the appropriate outcome state. If either settings or location are null, user has configured client against connecting, or the game
     * server is not available, this will emit intent against. Otherwise, intent for.
     */
    private val worldSocketIntentState: Flow<WorldSocketIntentState> =
        combine(
            mutableShouldConnect,
            currentGameSettings,
            currentLocation
        ) { shouldConnect, settings, location ->
            // If either are null, emit can't join.
            if(!shouldConnect || location == null || settings == null) {
                WorldSocketIntentState.CantJoinWorld
            } else {
                // Otherwise, if settings informs us either that game is configured to not connect, or server is unavailable, same result.
                if(!settings.canConnectGame || !settings.isServerAvailable) {
                    WorldSocketIntentState.CantJoinWorld
                }
                /**
                 * TODO: we require a device identifier to be passed here. Depending on how firebase fits in, we may be able to retrieve the installation ID from a view
                 * TODO: model, and pass it all the way up here through GameSettings. For now, we'll just use a random UUID.
                 */
                val deviceIdentifier = UUID.randomUUID().toString()
                // TODO: we can place a validator on age of location here.
                // Otherwise, emit intent to join server.
                WorldSocketIntentState.CanJoinWorld(
                    deviceIdentifier,
                    settings.entryToken!!,
                    settings.gameServerInfo!!,
                    location
                )
            }
        }

    /**
     * Initialise this singleton instance. In the application scope, we will launch a collection for the latest world intent state.
     * The current state of this flow will then inform whether we will connect/disconnect to the game server.
     */
    init {
        applicationScope.launch(ioDispatcher) {
            worldSocketIntentState.collect { intentState ->
                when(intentState) {
                    is WorldSocketIntentState.CanJoinWorld -> ensureConnectionOpen(intentState)
                    is WorldSocketIntentState.CantJoinWorld -> closeConnection()
                }
            }
        }
    }

    /**
     * Place the world socket session into a state totally receptive of a connection to game server. The given game settings will be updated,
     * the location given will be used (as long as it is newer than the current location stored) and the connection gatekeeper will be disabled.
     */
    fun requestJoinWorld(gameSettings: GameSettings, location: PlayerPosition) {
        Timber.d("APPROVING intent to join world.")
        // Set our newest game settings.
        setGameSettings(gameSettings)
        // If current location is null or older than given location, set newest location.
        if(currentLocation.value == null || currentLocation.value!!.loggedAt < location.loggedAt) {
            mutableCurrentLocation.tryEmit(location)
        }
        // Finally, enable our gatekeeper switch.
        mutableShouldConnect.tryEmit(true)
    }

    /**
     * Place the world socket session into a state that indicates a connection to the game server should no longer be allowed. This function will
     * enable the gatekeeper, which will trigger a chain reaction that will eventually disconnect the client and clear all previous connection
     * information such as game settings.
     */
    fun requestLeaveWorld(reason: String? = null) {
        Timber.d("REVOKING intent to join world.")
        // TODO: if we are currently connected, send a message to server letting it know we're leaving, optionally with reason.
        // Simply set gatekeeper to false.
        mutableShouldConnect.tryEmit(false)
    }

    /**
     * Update latest accelerometer readings.
     */
    fun updateAccelerometerReadings(requestUpdateAccelerometerReadings: RequestUpdateAccelerometerReadings) {
        mutableLatestAccelerometerReadings.tryEmit(
            requestUpdateAccelerometerReadings
        )
    }

    /**
     * Update latest magnetometer readings.
     */
    fun updateMagnetometerReadings(requestUpdateMagnetometerReadings: RequestUpdateMagnetometerReadings) {
        mutableLatestMagnetometerReadings.tryEmit(
            requestUpdateMagnetometerReadings
        )
    }

    /**
     * Update the current location availability.
     */
    fun setLocationAvailability(availability: Boolean) {
        mutableLocationAvailability.tryEmit(availability)
    }

    /**
     * Update the current game settings to be used on next connection cycle.
     */
    private fun setGameSettings(gameSettings: GameSettings) {
        mutableGameSettings.tryEmit(gameSettings)
    }

    /**
     * Ensure the connection to the server is open, given the provided intent state. This function will always return if the socket is currently connecting or
     * connected, and will only obey a new configuration after a reconnect.
     */
    private fun ensureConnectionOpen(canJoinWorld: WorldSocketIntentState.CanJoinWorld) {
        if(mutableWorldSocketState.value is WorldSocketState.Connecting || mutableWorldSocketState.value is WorldSocketState.Connected) {
            // TODO: If socket is currently connected, we'll check whether the difference between the data in the given intent and the data currently used in connection
            // TODO: is sufficiently different to warrant a disruptive reconnection. We're satisfied that this is the case if the IP address has changed. For now, we'll return.
            return
        }
        // Otherwise emit a new state; connecting.
        mutableWorldSocketState.tryEmit(WorldSocketState.Connecting)
        Timber.d("Received intent to connect to game server.")
        // Get all applicable cookies to the HawkSpeed service URL and map them to a list of strings.
        // TODO: change this back to use account remote data, get applicable cookies once you figure out the issue here.
        //val hawkSpeedCookies: List<String> = accountRemoteData.getApplicableCookies().map { cookie ->
        //    cookie.value
        //}
        val hawkSpeedCookies: List<String> = getRelevantCookies()
        // Create the manager now, with our injected options.
        socketManager = Manager(
            URI.create(canJoinWorld.gameServerInfo),
            IO.Options.builder()
                .setExtraHeaders(mapOf("Cookie" to hawkSpeedCookies))
                .build()
        ).apply {
            // Register all connection handlers for the reconnection flow.
            attachReconnectionHandlers(this)
        }
        // Convert the request to connect to game server to a JSON object, then map that JSON object to a map of string to string.
        val dtoAsJsonString = JSONObject(
            gson.toJson(
                RequestConnectAuthenticationDto(
                    canJoinWorld.deviceIdentifier,
                    canJoinWorld.location.latitude,
                    canJoinWorld.location.longitude,
                    canJoinWorld.location.bearing,
                    canJoinWorld.location.speed,
                    canJoinWorld.location.loggedAt
                ),
                RequestConnectAuthenticationDto::class.java
            )
        ).toMap()
        // Setup the socket given the socket manager, and set authentication parameters as our string above.
        socket = socketManager?.socket("/",
            IO.Options.builder()
                .setAuth(dtoAsJsonString)
                .build()
        )?.apply {
            // Register all handlers for the basic connection.
            attachConnectionHandlers(this)
            // Register handlers for welcoming Player to world, and when Player is kicked.
            on<ConnectAuthenticationResponseDto>("welcome") { handleWelcomeToWorld(it) }
            on<SocketErrorWrapperDto>("kicked") { handleKicked(it) }
            // Register all race handlers.
            attachRaceHandlers(this)
        }?.connect()
    }

    /**
     * Request a new race on the given race track, given a fresh location.
     */
    suspend fun startRace(requestStartRace: RequestStartRace): StartRaceResultModel {
        if(socket == null || socket?.connected() != true) {
            // TODO: if we are not connected, handle this some way. Do we raise? Or do we just do nothing?
            throw NotImplementedError()
        }
        // Build a DTO for our request to start a race.
        val requestStartRaceDto = RequestStartRaceDto(requestStartRace)
        // Use send message to send this request, and receive back the result.
        val startRaceResponseDto: StartRaceResponseDto = socket!!.sendMessage("start_race", requestStartRaceDto)
        // Finally, map this to model and return.
        return startRaceResponseDtoMapper.mapFromDto(startRaceResponseDto)
    }

    /**
     * Send a message to cancel the current race. This will return a cancel race result object.
     */
    suspend fun cancelRace(requestCancelRace: RequestCancelRace): CancelRaceResultModel {
        if(socket == null || socket?.connected() != true) {
            // TODO: if we are not connected, handle this some way. Do we raise? Or do we just do nothing?
            throw NotImplementedError()
        }
        // Build a DTO for our request to cancel a race.
        val requestCancelRaceDto = RequestCancelRaceDto(requestCancelRace)
        // Use send message to send this request and receive back the result.
        val cancelRaceResponseDto: CancelRaceResponseDto = socket!!.sendMessage("cancel_race", requestCancelRaceDto)
        // Finally, map this to model and return.
        return cancelRaceResponseDtoMapper.mapFromDto(cancelRaceResponseDto)
    }

    /**
     * Send the given player update to the server, and receive an update model for the result.
     */
    suspend fun sendPlayerUpdate(requestPlayerUpdate: RequestPlayerUpdate): PlayerUpdateResultModel {
        // Set this location as the current location.
        mutableCurrentLocation.value = PlayerPosition(
            requestPlayerUpdate.latitude,
            requestPlayerUpdate.longitude,
            requestPlayerUpdate.bearing,
            requestPlayerUpdate.speed,
            requestPlayerUpdate.loggedAt
        )

        if(socket == null || socket?.connected() != true) {
            throw NotImplementedError("Handle this properly.")
        }
        // Construct the required DTO for now.
        val requestPlayerUpdateDto = RequestPlayerLocationDto(requestPlayerUpdate)
        // Call send message, receiving a DTO for this.
        val playerUpdateResponse: PlayerUpdateResponseDto = socket!!.sendMessage("player_update", requestPlayerUpdateDto)
        // Finally, map and return the result.
        return playerUpdateResultDtoMapper.mapFromDto(playerUpdateResponse)
    }

    /**
     * Sets this viewport as the latest stored viewport in the session, then sends the viewport as an update
     * to the server, receiving all objects in that view in response.
     */
    suspend fun sendViewportUpdate(requestViewportUpdate: RequestViewportUpdate): ViewportUpdateResultModel {
        // Set viewport to the one provided.
        mutableLatestViewport.value = Viewport(
            requestViewportUpdate.viewportMinX,
            requestViewportUpdate.viewportMinY,
            requestViewportUpdate.viewportMaxX,
            requestViewportUpdate.viewportMaxY,
            requestViewportUpdate.zoom
        )

        if(socket == null || socket?.connected() != true) {
            // TODO: if we are not connected, handle this some way. Do we raise? Or do we just do nothing?
            throw NotImplementedError()
        }
        // Construct a request for viewport update.
        val requestViewportUpdateDto = RequestViewportUpdateDto(
            RequestViewportDto(
                requestViewportUpdate.viewportMinX,
                requestViewportUpdate.viewportMinY,
                requestViewportUpdate.viewportMaxX,
                requestViewportUpdate.viewportMaxY,
                requestViewportUpdate.zoom
            )
        )
        // Perform a send message toward the event name 'viewport_update'.
        val viewportUpdateResponse: ViewportUpdateResponseDto = socket!!.sendMessage("viewport_update", requestViewportUpdateDto)
        // Finally, map and return result.
        return viewportUpdateResultDtoMapper.mapFromDto(viewportUpdateResponse)
    }

    /**
     * Close the socket's connection, then remove all handlers from the socket and the socket manager. This function will be invoked when the session is notified that
     * permission to be in the world has been withdrawn. This function will invoke the handle disconnection.
     */
    private fun closeConnection() {
        Timber.d("We have been told connection to server should be closed, or is not currently allowed.")
        socket?.close()
        socket?.off()
        socketManager?.off()
    }

    /**
     * Attach all handlers related to connections to the given socket.
     */
    private fun attachConnectionHandlers(socket: Socket) {
        socket.apply {
            /**
             * Called when socket successfully connects to the server.
             */
            on("connect") {
                Timber.d("Connected to the desired SocketIO server, successfully!")
            }
            /**
             * Called when the socket has failed to connect to the server, response could contain almost anything, but we must be sure to check for instances of socket
             * error wrappers being sent, which indicate a refusal by the server.
             */
            on<SocketErrorWrapperDto>("connect_error", { socketErrorWrapperDto ->
                // Successfully read a socket error from the response, which means we were rejected from the server for a HawkSpeed reason.
                Timber.e("Failed to connect to the world because the game server actively refused our join attempt.")
                /**
                 * TODO: when we get a connect error, we should analyse the cause in an effort to determine exactly why the join failed, then we should inform
                 * TODO: the user of the reason and some corrective action.
                 */
                // We will create a socket type resource error from the wrapper dto, then pass this to a connection refused state.
                mutableWorldSocketState.tryEmit(WorldSocketState.ConnectionRefused(ResourceError.SocketError(socketErrorWrapperDto)))
            }, { response ->
                Timber.e("Failed to connect to the world for a non-HawkSpeed reason.")
                /**
                 * TODO: connect failed for some other reason; perhaps internet dropping, permission revoked, server dying... etc
                 */
                // For now, just print out all errors in response.
                response.forEach {
                    Timber.e("Non-HawkSpeed world join error: $it")
                }
                // Simply pass a disconnected state here for now.
                mutableWorldSocketState.tryEmit(WorldSocketState.Disconnected())
            })
            /**
             * Called when the socket has been disconnected.
             */
            on("disconnect") { response ->
                Timber.d("Disconnected from SocketIO server.")
                /**
                 * TODO: disconnected from the server, this is not necessarily an issue, we just need to collect cases for this.
                 */
                response.forEach {
                    Timber.e("Disconnect response line: $it")
                }
                // For now, all we'll do is put our socket state into disconnected mode.
                mutableWorldSocketState.tryEmit(WorldSocketState.Disconnected())
            }
        }
    }

    /**
     * Handle an event in which the server has welcomed this client to the server. The resulting object will contain all preliminary data about the position
     * and surroundings for this client. This should be passed alongside the game server state update.
     */
    private fun handleWelcomeToWorld(connectAuthenticationResponseDto: ConnectAuthenticationResponseDto) {
        Timber.d("We have received a welcome-to-world message. We are now connected & joined.")
        mutableWorldSocketState.tryEmit(
            WorldSocketState.Connected(
                connectAuthenticationResponseDto.playerUid,
                connectAuthenticationResponseDto.latitude,
                connectAuthenticationResponseDto.longitude,
                connectAuthenticationResponseDto.bearing
            )
        )
    }

    /**
     * Handle the case where the socket server has decided to kick this client from service.
     */
    private fun handleKicked(socketError: SocketErrorWrapperDto) {
        throw NotImplementedError("handleKicked is not implemented.")
    }

    /**
     * Attach all handlers related to reconnections to the given manager.
     */
    private fun attachReconnectionHandlers(manager: Manager) {
        manager.apply {
            /**
             *
             */
            on("reconnect") { response -> throw NotImplementedError("Handler for manager event 'reconnect' not set.") }
            /**
             *
             */
            on("reconnect_attempt") { response -> throw NotImplementedError("Handler for manager event 'reconnect_attempt' not set.") }
            /**
             *
             */
            on("reconnect_error") { response -> throw NotImplementedError("Handler for manager event 'reconnect_error' not set.") }
            /**
             *
             */
            on("reconnect_failed") { response -> throw NotImplementedError("Handler for manager event 'reconnect_failed' not set.") }
        }
    }

    /**
     * Attach all handlers related to races and their messages to the given socket.
     */
    private fun attachRaceHandlers(socket: Socket) {
        socket.apply {
            /**
             * Event will be invoked when the server determines the client has successfully reached the end of a race track.
             */
            on<RaceFinishedDto>("race-finished") { raceFinished ->
                Timber.e("Handling a race-finished message is not yet implemented.")
                /**
                 * TODO: race finished must also contain the leaderboard entry for the finished race.
                 * With this, we must upsert the leaderboard entry and latest race update into cache; ideally in a single transaction. This will then trigger race UI
                 * to allow the race to be finished.
                 */
                throw NotImplementedError()
            }
            /**
             * Event will be invoked each time the Player has an ongoing race, and submits a location update.
             */
            on<RaceProgressDto>("race-progress") { raceProgress ->
                Timber.e("Handling a race-progress message is not yet implemented.")
                /**
                 * TODO: race progress has been received, we must upsert this into cache.
                 */
                throw NotImplementedError()
            }
            /**
             * Event will be invoked if the server determines the race should be disqualified.
             */
            on<RaceDisqualifiedDto>("race-disqualified") { raceDisqualified ->
                Timber.e("Handling a race-disqualified message is not yet implemented.")
            }
        }
    }

    /**
     * Query for, and return a list of all cookies associated with the HawkSpeed game server.
     */
    private fun getRelevantCookies(): List<String> {
        // Load all cookies for the HawkSpeed server from our cookie jar.
        // TODO: Get a proper service URL here. We are getting all cookies from the service URL set in build config, which refers to the API, aka, central server.
        val targetUrl: HttpUrl = BuildConfig.SERVICE_URL.toHttpUrl()
        val cookies = cookieJar.loadForRequest(targetUrl)
        // From the cookie jar, map all cookies to a string and return the result.
        return cookies.map {
            cookieToString(it, false)
        }
    }
}