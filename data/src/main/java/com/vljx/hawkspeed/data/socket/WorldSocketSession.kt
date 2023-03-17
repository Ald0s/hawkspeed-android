package com.vljx.hawkspeed.data.socket

import android.location.Location
import com.google.gson.Gson
import com.vljx.hawkspeed.data.BuildConfig
import com.vljx.hawkspeed.data.mapper.race.RaceMapper
import com.vljx.hawkspeed.data.mapper.track.TrackMapper
import com.vljx.hawkspeed.data.network.mapper.race.RaceUpdateDtoMapper
import com.vljx.hawkspeed.data.network.mapper.track.TrackDtoMapper
import com.vljx.hawkspeed.data.network.models.race.RaceUpdateDto
import com.vljx.hawkspeed.data.socket.Extension.emit
import com.vljx.hawkspeed.data.socket.Extension.on
import com.vljx.hawkspeed.data.socket.models.*
import com.vljx.hawkspeed.data.socket.requests.ConnectAuthenticationRequestDto
import com.vljx.hawkspeed.data.socket.requests.PlayerUpdateRequestDto
import com.vljx.hawkspeed.data.socket.requests.StartRaceRequestDto
import com.vljx.hawkspeed.data.socket.requests.ViewportUpdateRequestDto
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.di.scope.ApplicationScope
import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.repository.RaceRepository
import com.vljx.hawkspeed.domain.repository.TrackRepository
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import kotlinx.coroutines.channels.BufferOverflow
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

@Singleton
class WorldSocketSession @Inject constructor(
    private val applicationScope: ApplicationScope,
    private val cookieJar: CookieJar,
    @Bridged
    private val trackRepository: TrackRepository,
    @Bridged
    private val raceRepository: RaceRepository,

    private val raceUpdateDtoMapper: RaceUpdateDtoMapper,
    private val raceMapper: RaceMapper,

    private val trackDtoMapper: TrackDtoMapper,
    private val trackMapper: TrackMapper
) {
    /**
     * A mutable state flow for the target server information that will be set by the service, when a request to join the world has been completed.
     * This forms half of the base requirements for connection to the game server.
     */
    private val mutableServerInfoState: MutableStateFlow<ServerInfoState> =
        MutableStateFlow(ServerInfoState.None)

    /**
     * A state flow that will observe all changes to the inner requirements for access to the world. If this changes to not satisfied,
     * the existing connection to the server will be closed.
     */
    private val worldRequirementsState: StateFlow<WorldRequirementsState> =
        flow {
            emit(WorldRequirementsState.Satisfied)
        }.stateIn(applicationScope, SharingStarted.Eagerly, WorldRequirementsState.NotSatisfied)

    /**
     * A mutable shared flow for the current state of the connection to the game server.
     */
    private val mutableWorldSocketState: MutableSharedFlow<WorldSocketState> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    /**
     * A state flow for the current world socket permission, which represents what the connection to the server is required to do. For instance, if this
     * emits CanJoinWorld, a connection to the socket server will be established. This will combine all requirements and emit the required action.
     */
    private val worldSocketPermissionState: StateFlow<WorldSocketPermissionState> =
        combine(
            mutableServerInfoState,
            worldRequirementsState
        ) { serverInfoState, requirementsState ->
            if(serverInfoState is ServerInfoState.Connect && requirementsState is WorldRequirementsState.Satisfied) {
                return@combine WorldSocketPermissionState.CanJoinWorld(serverInfoState.connectAuthenticationRequestDto)
            } else {
                return@combine WorldSocketPermissionState.CantJoinWorld
            }
        }.stateIn(applicationScope, SharingStarted.Eagerly, WorldSocketPermissionState.CantJoinWorld)

    /**
     * A state flow for the current location, or whichever was most recently sent.
     */
    private val mutableCurrentLocation: MutableStateFlow<Location?> = MutableStateFlow(null)

    /**
     * A state flow for the current state of the connection to the game server.
     */
    val worldSocketState: SharedFlow<WorldSocketState> =
        mutableWorldSocketState

    /**
     * The current location.
     */
    val currentLocation: StateFlow<Location?> =
        mutableCurrentLocation

    /**
     * A property that will return the current world socket permission state, which represents the most recent instruction given to the
     * world socket session.
     */
    val currentWorldSocketPermissionState: WorldSocketPermissionState
        get() = worldSocketPermissionState.value

    // Objects for the socket IO server.
    // The socket IO manager.
    private var socketManager: Manager? = null
    // The current client socket in use.
    private var socket: Socket? = null

    /**
     * Initialise this singleton instance.
     * In the application scope, we will launch a collection for the latest world permission state. The current state of this flow
     * will then inform whether we will connect/disconnect to the game server.
     */
    init {
        applicationScope.launch {
            worldSocketPermissionState.collect { permissionState ->
                when(permissionState) {
                    is WorldSocketPermissionState.CanJoinWorld -> openConnection(permissionState)
                    is WorldSocketPermissionState.CantJoinWorld -> closeConnection()
                }
            }
        }
    }

    /**
     * Update the permission granted for opening a connection to the game server, this is done by setting the server info for the target.
     * This is one of a few requirements before a connection will be established.
     */
    fun updateServerInfo(entryToken: String, gameServerInfo: String, connectAuthenticationRequestDto: ConnectAuthenticationRequestDto) {
        mutableServerInfoState.tryEmit(
            ServerInfoState.Connect(
                entryToken,
                gameServerInfo,
                connectAuthenticationRequestDto
            )
        )
    }

    /**
     * Clear the permission to be connected to the game server. This will close any existing connection.
     */
    fun clearServerInfo() {
        mutableServerInfoState.tryEmit(ServerInfoState.None)
    }

    /**
     * Update the current location for the device.
     */
    fun setCurrentLocation(location: Location?) {
        mutableCurrentLocation.tryEmit(location)
    }

    /**
     * Request a new race on the given race track, given a fresh location.
     */
    fun sendRaceRequest(startRaceRequest: StartRaceRequestDto) {
        if(socket == null || socket?.connected() != true) {
            // TODO: if we are not connected, handle this some way. Do we raise? Or do we just do nothing?
            throw NotImplementedError()
        }
        // Perform an emission toward the event name associated with the start race handler.
        socket!!.emit<StartRaceRequestDto, RaceStartedResponseDto>("start_race", startRaceRequest) { raceStartedResponse ->
            // TODO: properly handle the case in which race was NOT started properly. For now, simply throw an exc.
            if(!raceStartedResponse.isStarted) {
                throw NotImplementedError("Failed to handle raceStartedResponse - isStarted not handled yet.")
            }
            handleRaceStarted(raceStartedResponse)
        }
    }

    /**
     * Update the Player's position.
     */
    fun sendPlayerUpdate(playerUpdateRequest: PlayerUpdateRequestDto) {
        if(socket == null || socket?.connected() != true) {
            // TODO: if we are not connected, handle this some way. Do we raise? Or do we just do nothing?
            throw NotImplementedError()
        }
        // Perform an emission toward this event name.
        socket!!.emit<PlayerUpdateRequestDto, PlayerUpdateResponseDto>("player_update", playerUpdateRequest) { playerUpdateResponse ->
            handlePlayerUpdateResponse(playerUpdateResponse)
        }
    }

    /**
     * Update the Player's viewport, receiving all objects in their sight.
     */
    fun sendViewportUpdate(viewportUpdateRequestDto: ViewportUpdateRequestDto) {
        if(socket == null || socket?.connected() != true) {
            return
        }
        // Perform an emission toward the event name 'viewport_update'.
        socket!!.emit<ViewportUpdateRequestDto, ViewportUpdateResponseDto>("viewport_update", viewportUpdateRequestDto) { viewportUpdateResponse ->
            handleViewportUpdateResponse(viewportUpdateResponse)
        }
    }

    /**
     * Commence the connection-opening procedure.
     * This function will create a new socket manager, informed of the current session cookie set by previous logins. Then, a new socket will be created and opened
     * toward the game server.
     */
    private fun openConnection(canJoinWorld: WorldSocketPermissionState.CanJoinWorld) {
        // If we have a socket existing, and it is connected, close the connection now.
        if(socket?.connected() == true) {
            Timber.w("WorldSocketSession has been asked to open a connection to the game server, when one is already existing... Closing old one first...")
            closeConnection()
        }
        // Set our game server state to connecting.
        mutableWorldSocketState.tryEmit(WorldSocketState.Connecting)
        Timber.d("We have been granted permission to connect to the game server...")
        // Each time, instantiate a new socket manager pointing toward the given world game server's connection info.
        // TODO: for simplicity sake, we'll keep this identical.
        val serverInfo = "http://192.168.0.253:5000"
        // TODO: we will now get the HawkSpeed login cookie(s) used by the okhttpclient.
        val relevantCookies: List<String> = getRelevantCookies()
        // Create the manager now, with our injected options.
        socketManager = Manager(
            URI.create(serverInfo),
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
        val gson = Gson()
        // Convert the DTO model to a JSONObject.
        val dtoAsJSONObject: JSONObject = JSONObject(gson.toJson(canJoinWorld.connectAuthenticationRequestDto, ConnectAuthenticationRequestDto::class.java))
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
            on<RaceUpdateDto>("race-finished") { handleRaceFinished(it) }
            on<RaceUpdateDto>("race-disqualified") { handleRaceDisqualified(it) }
            on<RaceUpdateDto>("race-cancelled") { handleRaceCancelled(it) }
        }?.connect()
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
        Timber.d("We have been instructed to close the connection to the world.")
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
        if(connectAuthenticationResponseDto.viewportUpdate != null) {
            handleViewportUpdateResponse(connectAuthenticationResponseDto.viewportUpdate)
        }
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
     * Handle an acknowledgement response to the submission of a player update.
     */
    private fun handlePlayerUpdateResponse(playerUpdateResponse: PlayerUpdateResponseDto) {
        // If the viewport update within this response is not null, handle a viewport update response for it.
        if(playerUpdateResponse.viewportUpdate != null) {
            handleViewportUpdateResponse(playerUpdateResponse.viewportUpdate)
        }
        // TODO: handle other things from player update response.
    }

    /**
     * Handle an acknowledgement response to the submission of a viewport update.
     */
    private fun handleViewportUpdateResponse(viewportUpdateResponse: ViewportUpdateResponseDto) {
        applicationScope.launch {
            // Get the tracks and map them to model, then to domain.
            val tracks: List<Track> = viewportUpdateResponse.tracks
                    .map { trackDtoMapper.mapFromDto(it) }
                    .map { trackMapper.mapFromData(it) }
            // Upsert these tracks.
            trackRepository.cacheTracks(tracks)
        }
    }

    /**
     * Handle a race being successfully started.
     */
    private fun handleRaceStarted(raceStartedResponse: RaceStartedResponseDto) {
        if(!raceStartedResponse.isStarted) {
            throw NotImplementedError("handleRaceStarted is not supposed to ever handle a failed attempt to start a new race (isStarted == false)")
        }
        applicationScope.launch {
            // We can be sure the race has actually started. We can read the RaceUpdate from this and upsert it.
            upsertRaceUpdate(
                raceStartedResponse.race!!
            )
        }
    }

    /**
     * Handle the current race being finished successfully.
     */
    private fun handleRaceFinished(raceUpdate: RaceUpdateDto) {
        applicationScope.launch {
            // When we are notified of the race finishing, we must simply upsert the received race.
            upsertRaceUpdate(raceUpdate)
        }
    }

    /**
     * Handle the server alerting the device that a race we're currently in has been disqualified. This is already committed on the serverside, and as such,
     * this is just a reactive handle.
     */
    private fun handleRaceDisqualified(raceUpdate: RaceUpdateDto) {
        applicationScope.launch {
            // When we are notified of a disqualification, we must simply upsert the received race.
            upsertRaceUpdate(raceUpdate)
        }
    }

    /**
     * Handle the server alerting the device that a race we're currently in has been cancelled. This is already committed on the serverside, and as such,
     * this is just a reactive handle.
     */
    private fun handleRaceCancelled(raceUpdate: RaceUpdateDto) {
        applicationScope.launch {
            // When we are notified of a cancellation, we must simply upsert the received race.
            upsertRaceUpdate(raceUpdate)
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
        mutableWorldSocketState.tryEmit(
            WorldSocketState.Disconnected
        )
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
        mutableWorldSocketState.tryEmit(
            WorldSocketState.Disconnected
        )
    }

    /**
     * A function to reduce code duplication involved in updating a Race instance.
     */
    private suspend fun upsertRaceUpdate(raceUpdate: RaceUpdateDto) {
        // Map from DTO and from Model.
        val race: Race = raceMapper.mapFromData(
            raceUpdateDtoMapper.mapFromDto(raceUpdate)
        )
        // Upsert this.
        raceRepository.cacheRace(race)
    }
}