package com.vljx.hawkspeed.repository

import com.vljx.hawkspeed.data.mapper.race.CancelRaceResultMapper
import com.vljx.hawkspeed.data.mapper.race.StartRaceResultMapper
import com.vljx.hawkspeed.data.mapper.world.PlayerUpdateResultMapper
import com.vljx.hawkspeed.data.mapper.world.ViewportUpdateResultMapper
import com.vljx.hawkspeed.data.models.race.RaceModel
import com.vljx.hawkspeed.data.socket.WorldSocketIntentState
import com.vljx.hawkspeed.data.source.race.RaceLocalData
import com.vljx.hawkspeed.data.source.track.TrackLocalData
import com.vljx.hawkspeed.domain.di.scope.ApplicationScope
import com.vljx.hawkspeed.domain.models.race.CancelRaceResult
import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.models.race.RaceUpdate
import com.vljx.hawkspeed.domain.models.race.StartRaceResult
import com.vljx.hawkspeed.domain.models.world.DeviceOrientation
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerUpdateResult
import com.vljx.hawkspeed.domain.models.world.Viewport
import com.vljx.hawkspeed.domain.models.world.ViewportUpdateResult
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.requestmodels.race.RequestCancelRace
import com.vljx.hawkspeed.domain.requestmodels.race.RequestStartRace
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestJoinWorld
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestLeaveWorld
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateAccelerometerReadings
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateMagnetometerReadings
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateNetworkConnectivity
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestViewportUpdate
import com.vljx.hawkspeed.domain.states.socket.WorldSocketState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeWorldSocketRepositoryImpl @Inject constructor(
    private val applicationScope: ApplicationScope,

    private val trackLocalData: TrackLocalData,
    private val raceLocalData: RaceLocalData,

    private val startRaceResultMapper: StartRaceResultMapper,
    private val cancelRaceResultMapper: CancelRaceResultMapper,
    private val playerUpdateResultMapper: PlayerUpdateResultMapper,
    private val viewportUpdateResultMapper: ViewportUpdateResultMapper
): WorldSocketRepository {
    private val mutableWorldSocketState: SharedFlow<WorldSocketState> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )
    private val mutableCurrentGameSettings: MutableStateFlow<GameSettings?> = MutableStateFlow(null)
    private val mutableNetworkConnectivity: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val mutableCurrentLocation: MutableStateFlow<PlayerPosition?> = MutableStateFlow(null)
    private val mutableLatestViewport: MutableStateFlow<Viewport?> = MutableStateFlow(null)

    override val worldSocketState: SharedFlow<WorldSocketState>
        get() = mutableWorldSocketState
    override val networkConnectivity: StateFlow<Boolean>
        get() = mutableNetworkConnectivity
    override val currentGameSettings: StateFlow<GameSettings?>
        get() = mutableCurrentGameSettings
    override val currentLocation: StateFlow<PlayerPosition?>
        get() = mutableCurrentLocation
    override val latestViewport: StateFlow<Viewport?>
        get() = mutableLatestViewport

    override val latestOrientationAngles: Flow<DeviceOrientation>
        get() = TODO("Not yet implemented")

    override fun updateAccelerometerReadings(requestUpdateAccelerometerReadings: RequestUpdateAccelerometerReadings) {
        TODO("Not yet implemented")
    }

    override fun updateMagnetometerReadings(requestUpdateMagnetometerReadings: RequestUpdateMagnetometerReadings) {
        TODO("Not yet implemented")
    }

    override fun updateNetworkConnectivity(requestUpdateNetworkConnectivity: RequestUpdateNetworkConnectivity) {
        TODO("Not yet implemented")
    }

    override fun setLocationAvailability(available: Boolean) {
        TODO("Not yet implemented")
    }

    override fun requestJoinWorld(requestJoinWorld: RequestJoinWorld) {
        TODO("Not yet implemented")
    }

    override fun requestLeaveWorld(requestLeaveWorld: RequestLeaveWorld) {
        TODO("Not yet implemented")
    }

    override suspend fun startRace(requestStartRace: RequestStartRace): StartRaceResult {
        val startRaceResult = StartRaceResult(
            true,
            Race(
                "RACE01",
                requestStartRace.trackUid,
                System.currentTimeMillis()-250,
                null,
                false,
                null,
                false,
                0,
                0,
                0
            ),
            null
        )
        // Upsert a race model into cache.
        raceLocalData.upsertRace(
            RaceModel(
                "RACE01",
                requestStartRace.trackUid,
                System.currentTimeMillis()-250,
                null,
                false,
                null,
                false,
                0,
                0,
                0
            )
        )
        // Return start result.
        return startRaceResult
    }

    override suspend fun cancelRace(requestCancelRace: RequestCancelRace): CancelRaceResult {
        TODO("Not yet implemented")
    }

    override suspend fun sendPlayerUpdate(requestPlayerUpdate: RequestPlayerUpdate): PlayerUpdateResult {
        // Sending a player update will simply emit the latest location to the mutable state flow.
        mutableCurrentLocation.value = PlayerPosition(
            requestPlayerUpdate.latitude,
            requestPlayerUpdate.longitude,
            requestPlayerUpdate.bearing,
            requestPlayerUpdate.speed,
            requestPlayerUpdate.loggedAt
        )
        return PlayerUpdateResult(
            requestPlayerUpdate.latitude,
            requestPlayerUpdate.longitude,
            requestPlayerUpdate.bearing,
            null
        )
    }

    override suspend fun sendViewportUpdate(requestViewportUpdate: RequestViewportUpdate): ViewportUpdateResult {
        TODO("Not yet implemented")
    }

    /*private val mutableShouldConnect: MutableStateFlow<Boolean> = MutableStateFlow(false)

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

    init {
        applicationScope.launch {
            worldSocketIntentState.collect { intentState ->
                when(intentState) {
                    is WorldSocketIntentState.CanJoinWorld -> {

                    }
                    is WorldSocketIntentState.CantJoinWorld -> {

                    }
                }
            }
        }
    }*/
}