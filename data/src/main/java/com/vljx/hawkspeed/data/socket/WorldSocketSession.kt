package com.vljx.hawkspeed.data.socket

import com.google.gson.Gson
import com.vljx.hawkspeed.data.BuildConfig
import com.vljx.hawkspeed.data.models.race.StartRaceResultModel
import com.vljx.hawkspeed.data.models.world.PlayerUpdateResultModel
import com.vljx.hawkspeed.data.models.world.ViewportUpdateResultModel
import com.vljx.hawkspeed.data.socket.models.RaceUpdateDto
import com.vljx.hawkspeed.data.socket.Extension.on
import com.vljx.hawkspeed.data.socket.Extension.sendMessage
import com.vljx.hawkspeed.data.socket.mapper.race.StartRaceResponseDtoMapper
import com.vljx.hawkspeed.data.socket.mapper.world.PlayerUpdateResultDtoMapper
import com.vljx.hawkspeed.data.socket.mapper.world.ViewportUpdateResultDtoMapper
import com.vljx.hawkspeed.data.socket.models.*
import com.vljx.hawkspeed.data.socket.requestmodels.RequestConnectAuthenticationDto
import com.vljx.hawkspeed.data.socket.requestmodels.RequestPlayerLocationDto
import com.vljx.hawkspeed.data.socket.requestmodels.RequestStartRaceDto
import com.vljx.hawkspeed.data.socket.requestmodels.RequestViewportDto
import com.vljx.hawkspeed.data.socket.requestmodels.RequestViewportUpdateDto
import com.vljx.hawkspeed.domain.di.scope.ApplicationScope
import com.vljx.hawkspeed.domain.di.scope.AuthenticationScope
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.Viewport
import com.vljx.hawkspeed.domain.requestmodels.race.RequestStartRace
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestViewportUpdate
import com.vljx.hawkspeed.domain.states.socket.WorldSocketState
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.internal.cookieToString
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.net.URI
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

    private val startRaceResponseDtoMapper: StartRaceResponseDtoMapper,
    private val playerUpdateResultDtoMapper: PlayerUpdateResultDtoMapper,
    private val viewportUpdateResultDtoMapper: ViewportUpdateResultDtoMapper
) {
    // Objects for the socket IO server.
    // The socket IO manager.
    private var socketManager: Manager? = null
    // The current client socket in use.
    private var socket: Socket? = null

    /**
     * A mutable state flow for the current state of the connection to the game server.
     */
    private val mutableWorldSocketState: MutableStateFlow<WorldSocketState> = MutableStateFlow(
        WorldSocketState.Disconnected())

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
                // TODO: we can place a validator on age of location here.
                // Otherwise, emit intent to join server.
                WorldSocketIntentState.CanJoinWorld(
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
        applicationScope.launch {
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
        // TODO: we will now get the HawkSpeed login cookie(s) used by the okhttpclient.
        val relevantCookies: List<String> = getRelevantCookies()
        // Create the manager now, with our injected options.
        socketManager = Manager(
            URI.create(canJoinWorld.gameServerInfo),
            IO.Options.builder()
                .setExtraHeaders(mapOf("Cookie" to relevantCookies))
                .build()
        ).apply {
            // TODO: supply proper handlers for manager events.
            on("reconnect") { response -> throw NotImplementedError("Handler for manager event 'reconnect' not set.") }
            on("reconnect_attempt") { response -> throw NotImplementedError("Handler for manager event 'reconnect_attempt' not set.") }
            on("reconnect_error") { response -> throw NotImplementedError("Handler for manager event 'reconnect_error' not set.") }
            on("reconnect_failed") { response -> throw NotImplementedError("Handler for manager event 'reconnect_failed' not set.") }
        }
        // Now, create a new socket from this manager, just toward default namespace.
        // We want to set the connect request dto passed with server info state as the auth dictionary.
        fun JSONObject.toMap(): Map<String, String> = keys().asSequence().associateWith {
            // TODO: come up with proprietary solution for this.
            /* https://stackoverflow.com/a/64002903 */
            when (val value = this[it])
            {
                is JSONArray -> throw NotImplementedError()
                is JSONObject -> throw NotImplementedError()
                JSONObject.NULL -> throw NotImplementedError()
                else -> value.toString()
            }
        }
        // Convert the DTO model to a JSONObject.
        val requestConnectAuthenticationDto = RequestConnectAuthenticationDto(
            canJoinWorld.location.latitude,
            canJoinWorld.location.longitude,
            canJoinWorld.location.rotation,
            canJoinWorld.location.speed,
            canJoinWorld.location.loggedAt
        )
        val dtoAsJSONObject: JSONObject = JSONObject(gson.toJson(requestConnectAuthenticationDto, RequestConnectAuthenticationDto::class.java))
        val dtoAsMap: Map<String, String> = dtoAsJSONObject.toMap()
        socket = socketManager?.socket("/",
            IO.Options.builder()
                .setAuth(dtoAsMap)
                .build()
        )?.apply {
            // Register all handlers for the basic connection.
            on("connect") {
                Timber.w("Connected to the desired SocketIO server, successfully!")
            }
            on("connect_error") { response -> handleConnectionError(response) }
            on("disconnect") { response -> handleDisconnection(response) }
            // Register all custom handlers.
            on<ConnectAuthenticationResponseDto>("welcome") { handleWelcomeToWorld(it) }
            on<SocketErrorDto>("kicked") { handleKicked(it) }
            on<RaceUpdateDto>("race-finished") { handleRaceFinished(it) }
            on<RaceUpdateDto>("race-disqualified") { handleRaceDisqualified(it) }
            on<RaceUpdateDto>("race-cancelled") { handleRaceCancelled(it) }
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
     * Send the given player update to the server, and receive an update model for the result.
     */
    suspend fun sendPlayerUpdate(requestPlayerUpdate: RequestPlayerUpdate): PlayerUpdateResultModel {
        // Set this location as the current location.
        mutableCurrentLocation.value = PlayerPosition(
            requestPlayerUpdate.latitude,
            requestPlayerUpdate.longitude,
            requestPlayerUpdate.rotation,
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
     * Handle an event in which the server has welcomed this client to the server. The resulting object will contain all preliminary data about the position
     * and surroundings for this client. This should be passed alongside the game server state update.
     */
    private fun handleWelcomeToWorld(connectAuthenticationResponseDto: ConnectAuthenticationResponseDto) {
        Timber.d("We have received a welcome-to-world message. We are now connected & joined.")
        // If we have been sent a viewport update response, handle a viewport update response.
        //if(connectAuthenticationResponseDto.viewportUpdate != null) {
        //    handleViewportUpdateResponse(connectAuthenticationResponseDto.viewportUpdate)
        //}
        // Update world game server state to reflect connected. (and joined?)
        mutableWorldSocketState.tryEmit(
            WorldSocketState.Connected(
                connectAuthenticationResponseDto.playerUid,
                connectAuthenticationResponseDto.latitude,
                connectAuthenticationResponseDto.longitude,
                connectAuthenticationResponseDto.rotation
            )
        )
    }

    /**
     * Handle the case where the socket server has decided to kick this client from service.
     */
    private fun handleKicked(socketError: SocketErrorDto) {
        throw NotImplementedError("handleKicked is not implemented.")
    }

    /**
     * Handle the current race being finished successfully.
     */
    private fun handleRaceFinished(raceUpdate: RaceUpdateDto) {
        applicationScope.launch {
            // When we are notified of the race finishing, we must simply upsert the received race.
            //upsertRaceUpdate(raceUpdate)
        }
    }

    /**
     * Handle the server alerting the device that a race we're currently in has been disqualified. This is already committed on the serverside, and as such,
     * this is just a reactive handle.
     */
    private fun handleRaceDisqualified(raceUpdate: RaceUpdateDto) {
        applicationScope.launch {
            // When we are notified of a disqualification, we must simply upsert the received race.
            //upsertRaceUpdate(raceUpdate)
        }
    }

    /**
     * Handle the server alerting the device that a race we're currently in has been cancelled. This is already committed on the serverside, and as such,
     * this is just a reactive handle.
     */
    private fun handleRaceCancelled(raceUpdate: RaceUpdateDto) {
        applicationScope.launch {
            // When we are notified of a cancellation, we must simply upsert the received race.
            //upsertRaceUpdate(raceUpdate)
        }
    }

    /**
     * Handle any connection errors raised while attempting connection to the server.
     */
    private fun handleConnectionError(response: Array<out Any>) {
        Timber.d("Failed to connect to SocketIO server.")
        response.forEach {
            Timber.d("Connerror: $it")
        }
        // TODO: we can add more information to the connection error event.
        throw NotImplementedError("Please implement handleConnectionError, we need to pass a ResourceError to Disconnected")
        //mutableWorldSocketState.tryEmit(
        //    WorldSocketState.Disconnected
        //)
    }

    /**
     * Handle a disconnection from the server.
     */
    private fun handleDisconnection(response: Array<out Any>) {
        Timber.d("Disconnected from SocketIO server.")
        response.forEach {
            Timber.d("Error: $it")
        }
        // TODO: we can add more information to the disconnection event.
        throw NotImplementedError("Please implement handleDisconnection, we need to pass a ResourceError to Disconnected")
        //mutableWorldSocketState.tryEmit(
        //    WorldSocketState.Disconnected()
        //)
    }

    /**
     * A function to reduce code duplication involved in updating a Race instance.
     */
    /*private suspend fun upsertRaceUpdate(raceUpdate: RaceUpdateDto) {
        // Map from DTO and from Model.
        val race: Race = raceMapper.mapFromData(
            raceUpdateDtoMapper.mapFromDto(raceUpdate)
        )
        // Upsert this.
        raceRepository.cacheRace(race)
    }*/
}