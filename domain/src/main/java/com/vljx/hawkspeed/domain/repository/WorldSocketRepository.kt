package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.models.race.CancelRaceResult
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerUpdateResult
import com.vljx.hawkspeed.domain.models.race.StartRaceResult
import com.vljx.hawkspeed.domain.models.world.DeviceOrientation
import com.vljx.hawkspeed.domain.models.world.Viewport
import com.vljx.hawkspeed.domain.models.world.ViewportUpdateResult
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

interface WorldSocketRepository {
    /**
     * Return the world socket's state shared flow. Use this to receive updates about the socket's connection.
     */
    val worldSocketState: SharedFlow<WorldSocketState>

    /**
     * Return the world socket's current network connectivity.
     */
    val networkConnectivity: StateFlow<Boolean>

    /**
     * Return the current game settings state flow. If User changes applicable settings, emissions can be viewed here.
     */
    val currentGameSettings: StateFlow<GameSettings?>

    /**
     * Return a state flow for the device's current location.
     */
    val currentLocation: StateFlow<PlayerPosition?>

    /**
     * Return a state flow for the most recent viewport sent by the world view model.
     */
    val latestViewport: StateFlow<Viewport?>

    /**
     * Return a flow for the most recent orientation angles.
     */
    val latestOrientationAngles: Flow<DeviceOrientation>

    /**
     * Update accelerometer readings to the most recent.
     */
    fun updateAccelerometerReadings(requestUpdateAccelerometerReadings: RequestUpdateAccelerometerReadings)

    /**
     * Update magnetometer readings to the most recent.
     */
    fun updateMagnetometerReadings(requestUpdateMagnetometerReadings: RequestUpdateMagnetometerReadings)

    /**
     * Update the network connectivity for the default network.
     */
    fun updateNetworkConnectivity(requestUpdateNetworkConnectivity: RequestUpdateNetworkConnectivity)

    /**
     * Inform the socket state of changes to location availability.
     */
    fun setLocationAvailability(available: Boolean)

    /**
     * Set whether activity transition updates are being monitored.
     */
    fun setActivityTransitionUpdate(activityTransitionUpdates: ActivityTransitionUpdates)

    /**
     * Set the current location rate being used.
     */
    fun setLocationUpdateRate(locationUpdateRate: LocationUpdateRate)

    /**
     * Put socket state in a position where User approves a connection, if one is possible. This will not actually perform connection
     * logic until socket state is satisfied all other requirements are met.
     */
    fun requestJoinWorld(requestJoinWorld: RequestJoinWorld)

    /**
     * Put socket state in a position where User or other presenter layer entity disapproves ongoing connection. This will result in
     * the disconnection procedure being invoked.
     */
    fun requestLeaveWorld(requestLeaveWorld: RequestLeaveWorld)

    /**
     * Send a request to start a new race.
     */
    suspend fun startRace(requestStartRace: RequestStartRace): StartRaceResult

    /**
     * Send a request to cancel the current race in progress. This will return a cancel race result.
     */
    suspend fun cancelRace(requestCancelRace: RequestCancelRace): CancelRaceResult

    /**
     * Send an update to the player's location.
     */
    suspend fun sendPlayerUpdate(requestPlayerUpdate: RequestPlayerUpdate): PlayerUpdateResult

    /**
     * Send an update to the player's viewport.
     */
    suspend fun sendViewportUpdate(requestViewportUpdate: RequestViewportUpdate): ViewportUpdateResult
}