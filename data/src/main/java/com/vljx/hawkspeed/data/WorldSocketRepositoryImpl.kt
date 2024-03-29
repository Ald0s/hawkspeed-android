package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.race.CancelRaceResultMapper
import com.vljx.hawkspeed.data.mapper.world.PlayerUpdateResultMapper
import com.vljx.hawkspeed.data.mapper.race.StartRaceResultMapper
import com.vljx.hawkspeed.data.mapper.world.ViewportUpdateResultMapper
import com.vljx.hawkspeed.data.socket.WorldSocketSession
import com.vljx.hawkspeed.data.source.race.RaceLocalData
import com.vljx.hawkspeed.data.source.track.TrackLocalData
import com.vljx.hawkspeed.domain.models.race.CancelRaceResult
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerUpdateResult
import com.vljx.hawkspeed.domain.models.race.StartRaceResult
import com.vljx.hawkspeed.domain.models.world.DeviceOrientation
import com.vljx.hawkspeed.domain.models.world.Viewport
import com.vljx.hawkspeed.domain.models.world.ViewportUpdateResult
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.requestmodels.race.RequestCancelRace
import com.vljx.hawkspeed.domain.requestmodels.race.RequestStartRace
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestJoinWorld
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestLeaveWorld
import com.vljx.hawkspeed.domain.models.world.LocationUpdateRate
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateAccelerometerReadings
import com.vljx.hawkspeed.domain.models.world.ActivityTransitionUpdates
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateMagnetometerReadings
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateNetworkConnectivity
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestViewportUpdate
import com.vljx.hawkspeed.domain.states.socket.WorldSocketState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class WorldSocketRepositoryImpl @Inject constructor(
    private val worldSocketSession: WorldSocketSession,

    private val trackLocalData: TrackLocalData,
    private val raceLocalData: RaceLocalData,

    private val startRaceResultMapper: StartRaceResultMapper,
    private val cancelRaceResultMapper: CancelRaceResultMapper,
    private val playerUpdateResultMapper: PlayerUpdateResultMapper,
    private val viewportUpdateResultMapper: ViewportUpdateResultMapper
): WorldSocketRepository {
    override val worldSocketState: SharedFlow<WorldSocketState>
        get() = worldSocketSession.worldSocketState

    override val networkConnectivity: StateFlow<Boolean>
        get() = worldSocketSession.networkConnectivity

    override val currentGameSettings: StateFlow<GameSettings?>
        get() = worldSocketSession.currentGameSettings

    override val currentLocation: StateFlow<PlayerPosition?>
        get() = worldSocketSession.currentLocation

    override val latestViewport: StateFlow<Viewport?>
        get() = worldSocketSession.latestViewport

    override val latestOrientationAngles: Flow<DeviceOrientation>
        get() = worldSocketSession.latestOrientationAngles

    override fun updateAccelerometerReadings(requestUpdateAccelerometerReadings: RequestUpdateAccelerometerReadings) =
        worldSocketSession.updateAccelerometerReadings(requestUpdateAccelerometerReadings)

    override fun updateMagnetometerReadings(requestUpdateMagnetometerReadings: RequestUpdateMagnetometerReadings) =
        worldSocketSession.updateMagnetometerReadings(requestUpdateMagnetometerReadings)

    override fun updateNetworkConnectivity(requestUpdateNetworkConnectivity: RequestUpdateNetworkConnectivity) =
        worldSocketSession.updateNetworkConnectivity(requestUpdateNetworkConnectivity)

    override fun setLocationAvailability(available: Boolean) =
        worldSocketSession.setLocationAvailability(available)

    override fun setActivityTransitionUpdate(activityTransitionUpdates: ActivityTransitionUpdates) =
        worldSocketSession.setActivityTransitionUpdate(activityTransitionUpdates)

    override fun setLocationUpdateRate(locationUpdateRate: LocationUpdateRate) =
        worldSocketSession.setLocationUpdateRate(locationUpdateRate)

    override fun requestJoinWorld(requestJoinWorld: RequestJoinWorld) =
        worldSocketSession.requestJoinWorld(
            requestJoinWorld.gameSettings,
            requestJoinWorld.location
        )

    override fun requestLeaveWorld(requestLeaveWorld: RequestLeaveWorld) =
        worldSocketSession.requestLeaveWorld(
            requestLeaveWorld.reason
        )

    override suspend fun startRace(requestStartRace: RequestStartRace): StartRaceResult {
        val startRaceResult = worldSocketSession.startRace(requestStartRace)
        // If race start was successful, and we received the race itself, cache it.
        if(startRaceResult.isStarted && startRaceResult.race != null) {
            raceLocalData.upsertRace(
                startRaceResult.race
            )
        }
        return startRaceResultMapper.mapFromData(startRaceResult)
    }

    override suspend fun cancelRace(requestCancelRace: RequestCancelRace): CancelRaceResult {
        val cancelRaceResult = worldSocketSession.cancelRace(requestCancelRace)
        // If we have a race instance given, cache it.
        if(cancelRaceResult.race != null) {
            raceLocalData.upsertRace(
                cancelRaceResult.race
            )
        }
        return cancelRaceResultMapper.mapFromData(cancelRaceResult)
    }

    override suspend fun sendPlayerUpdate(requestPlayerUpdate: RequestPlayerUpdate): PlayerUpdateResult {
        val playerUpdateResult = worldSocketSession.sendPlayerUpdate(requestPlayerUpdate)
        // Check if we have been sent any objects in proximity to us.
        playerUpdateResult.worldObjectUpdateResult?.let { worldObjectUpdate ->
            // Upsert all tracks we've been sent.
            trackLocalData.upsertTracks(worldObjectUpdate.tracks)
        }
        return playerUpdateResultMapper.mapFromData(playerUpdateResult)
    }

    override suspend fun sendViewportUpdate(requestViewportUpdate: RequestViewportUpdate): ViewportUpdateResult {
        val viewportUpdateResult = worldSocketSession.sendViewportUpdate(requestViewportUpdate)
        trackLocalData.upsertTracks(viewportUpdateResult.tracks)
        return viewportUpdateResultMapper.mapFromData(viewportUpdateResult)
    }
}