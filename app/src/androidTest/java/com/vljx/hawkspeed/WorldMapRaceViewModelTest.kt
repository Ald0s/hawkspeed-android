package com.vljx.hawkspeed

import com.vljx.hawkspeed.base.BaseTest
import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.di.module.CommonModule
import com.vljx.hawkspeed.data.di.module.DataModule
import com.vljx.hawkspeed.data.di.module.DatabaseModule
import com.vljx.hawkspeed.data.di.module.DomainModule
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.data.models.race.RaceModel
import com.vljx.hawkspeed.data.source.race.RaceLocalData
import com.vljx.hawkspeed.data.source.track.TrackPathLocalData
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestCreateVehicle
import com.vljx.hawkspeed.domain.usecase.race.GetCachedLeaderboardEntryForRaceUseCase
import com.vljx.hawkspeed.domain.usecase.race.GetRaceUseCase
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationAndOrientationUseCase
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendCancelRaceRequestUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendPlayerUpdateUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendStartRaceRequestUseCase
import com.vljx.hawkspeed.domain.usecase.track.GetTrackWithPathUseCase
import com.vljx.hawkspeed.domain.usecase.vehicle.CreateVehicleUseCase
import com.vljx.hawkspeed.domain.usecase.vehicle.GetOurVehiclesUseCase
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.StartLineState
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceUiState
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@UninstallModules(CommonModule::class, DatabaseModule::class, DataModule::class, DomainModule::class)
@HiltAndroidTest
class WorldMapRaceViewModelTest: BaseTest() {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var trackWithPathLocalData: TrackPathLocalData
    @Inject
    lateinit var raceLocalData: RaceLocalData

    @Inject
    lateinit var getCurrentLocationAndOrientationUseCase: GetCurrentLocationAndOrientationUseCase
    @Inject
    lateinit var getCurrentLocationUseCase: GetCurrentLocationUseCase
    @Inject
    lateinit var getRaceUseCase: GetRaceUseCase
    @Inject
    lateinit var getCachedLeaderboardEntryForRaceUseCase: GetCachedLeaderboardEntryForRaceUseCase
    @Inject
    lateinit var getTrackWithPathUseCase: GetTrackWithPathUseCase
    @Inject
    lateinit var getOurVehiclesUseCase: GetOurVehiclesUseCase
    @Inject
    lateinit var sendStartRaceRequestUseCase: SendStartRaceRequestUseCase
    @Inject
    lateinit var sendCancelRaceRequestUseCase: SendCancelRaceRequestUseCase

    @Inject
    lateinit var createVehicleUseCase: CreateVehicleUseCase
    @Inject
    lateinit var sendPlayerUpdateUseCase: SendPlayerUpdateUseCase

    private lateinit var worldMapRaceViewModel: WorldMapRaceViewModel

    @Inject
    @IODispatcher
    lateinit var testDispatcher: CoroutineDispatcher

    @Before
    fun setUp() {
        hiltRule.inject()
        appDatabase.clearAllTables()
        worldMapRaceViewModel = WorldMapRaceViewModel(
            getRaceUseCase, getCachedLeaderboardEntryForRaceUseCase, getOurVehiclesUseCase, getTrackWithPathUseCase, getCurrentLocationAndOrientationUseCase, sendStartRaceRequestUseCase, sendCancelRaceRequestUseCase, testDispatcher
        )
    }

    @After
    fun tearDown() {
        appDatabase.clearAllTables()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testMovePositionAndEnsureCountdownNotInterrupted() = runTest {
        // Import the example1 JSON.
        val example1Track = getTrackWithPathFromResource(R.raw.example1)
        // Build a list of all points we'll be using.
        val testPoints = listOf(
            RequestPlayerUpdate(-37.843652, 145.03001, 180f, 1f, System.currentTimeMillis()),
            RequestPlayerUpdate(-37.843675, 145.030150, 180f, 1f, System.currentTimeMillis()+500),
            RequestPlayerUpdate(-37.843659, 145.030036, 180f, 1f, System.currentTimeMillis()+1000),
        )
        // Insert this track into cache.
        trackWithPathLocalData.upsertTrackWithPath(example1Track)
        // Insert a new vehicle, belonging to us.
        createVehicleUseCase(RequestCreateVehicle("1994 Toyota Supra")).collect()
        // Setup a collection for the primary race UI state.
        val states = mutableListOf<WorldMapRaceUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            worldMapRaceViewModel.worldMapRaceUiState.toList(states)
        }

        // Send the initial point as the very first point in the test track.
        sendPlayerUpdateUseCase(testPoints[0])
        // Now, set the targeted track's UID in the view model.
        worldMapRaceViewModel.setTrackUid(example1Track.track.trackUid)
        // Wait for the states list to be 3 items in size. Ensure the last item is of type to be OnStartLine.
        states.waitForSize(this, 2)
        assertEquals(2, states.size)
        // Ensure latest is OnStartLine; should be Perfect.
        assert(states[1] is WorldMapRaceUiState.OnStartLine)
        assert((states[1] as WorldMapRaceUiState.OnStartLine).startLineState is StartLineState.Perfect)
        // TODO: implement tests for testing the bearing/orientation.
        // Now, send player update for the second test point, which is 12.6 meters away from the first point.
        sendPlayerUpdateUseCase(testPoints[1])
        // Ensure that states has grown to 3 in size, and that the latest is an OnStartLine, then confirm that the start line state is now Standby.
        states.waitForSize(this, 3)
        assertEquals(3, states.size)
        assert(states[2] is WorldMapRaceUiState.OnStartLine)
        assert((states[2] as WorldMapRaceUiState.OnStartLine).startLineState is StartLineState.Standby)
        // Move back to the very first point, which is perfect. Ensure we have 4 emissions, latest being perfect.
        sendPlayerUpdateUseCase(testPoints[0])
        states.waitForSize(this, 4)
        assertEquals(4, states.size)
        assert(states[3] is WorldMapRaceUiState.OnStartLine)
        assert((states[3] as WorldMapRaceUiState.OnStartLine).startLineState is StartLineState.Perfect)
        // Now, start a race.
        val onStartLine = states[3] as WorldMapRaceUiState.OnStartLine
        // Ensure on start line has one vehicle available for use.
        assertEquals(1, onStartLine.yourVehicles.size)
        val perfect = onStartLine.startLineState as StartLineState.Perfect
        // Use the track and track path and location.
        worldMapRaceViewModel.startRace(
            onStartLine.yourVehicles.first(),
            onStartLine.track,
            perfect.location
        )
        // Wait for states to grow to 7 in size, (0, 1, 2), then submit the third position as
        // an update, which is 2.4 meters away from the start point; still Perfect should the countdown should not be interrupted.
        states.waitForSize(this, 7)
        sendPlayerUpdateUseCase(testPoints[2])
        // Now, we will wait for states to grow by 3 more emissions; (3, 4, RACING)
        states.waitForSize(this, 10)
        assertEquals(10, states.size)
        // Get those states.
        val countdownStates = states.subList(4, 9)
        // Ensure that from the first to last, current second matches 0, 1, 2, 3, 4
        for(i in 0 until 5) {
            // Ensure its a countdown state.
            assert(countdownStates[i] is WorldMapRaceUiState.CountingDown)
            assertEquals(i, (countdownStates[i] as WorldMapRaceUiState.CountingDown).currentSecond)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testMovePositionAndEnsureCountdownCanBeInterrupted() = runTest {
        // Import the example1 JSON.
        val example1Track = getTrackWithPathFromResource(R.raw.example1)
        // Build a list of all points we'll be using.
        val testPoints = listOf(
            RequestPlayerUpdate(-37.843652, 145.03001, 180f, 1f, System.currentTimeMillis()),
            RequestPlayerUpdate(-37.843675, 145.030150, 180f, 1f, System.currentTimeMillis()+500)
        )
        // Insert this track into cache.
        trackWithPathLocalData.upsertTrackWithPath(example1Track)
        // Insert a new vehicle, belonging to us.
        createVehicleUseCase(RequestCreateVehicle("1994 Toyota Supra")).collect()
        // Setup a collection for the primary race UI state.
        val states = mutableListOf<WorldMapRaceUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            worldMapRaceViewModel.worldMapRaceUiState.toList(states)
        }

        // Send the initial point as the very first point in the test track.
        sendPlayerUpdateUseCase(testPoints[0])
        // Now, set the targeted track's UID in the view model.
        worldMapRaceViewModel.setTrackUid(example1Track.track.trackUid)
        // Wait for the states list to be 2 items in size. Ensure the last item is of type to be OnStartLine.
        states.waitForSize(this, 2)
        assertEquals(2, states.size)
        // Ensure latest is OnStartLine; should be Perfect.
        assert(states[1] is WorldMapRaceUiState.OnStartLine)
        assert((states[1] as WorldMapRaceUiState.OnStartLine).startLineState is StartLineState.Perfect)
        // Now, start a race.
        val onStartLine = states[1] as WorldMapRaceUiState.OnStartLine
        // Ensure on start line has one vehicle available for use.
        assertEquals(1, onStartLine.yourVehicles.size)
        val perfect = onStartLine.startLineState as StartLineState.Perfect
        // Use the track and track path and location.
        worldMapRaceViewModel.startRace(
            onStartLine.yourVehicles.first(),
            onStartLine.track,
            perfect.location
        )
        // Wait for states to grow to 5 in size, (0, 1, 2), then submit the second test position; this position is 12.6m away from the track's start line, which
        // will cause an emission of a Standby start line state. This will essentially cancel the countdown where is is.
        states.waitForSize(this, 5)
        sendPlayerUpdateUseCase(testPoints[1])
        // Now, we will wait for states to grow by 2 more emissions; for 7 in total. The first emission should be a loading state, the second should be a CountdownDisqualified state.
        states.waitForSize(this, 7)
        assertEquals(7, states.size)
        assert(states[5] is WorldMapRaceUiState.Loading)
        assert(states[6] is WorldMapRaceUiState.RaceStartFailed)
        // Move back to a perfect position.
        sendPlayerUpdateUseCase(testPoints[0])
        // Now, reset disqualified track.
        worldMapRaceViewModel.resetRaceIntent()
        // Wait for 9 states. The latest state should now be a Perfect on line state.
        states.waitForSize(this, 9)
        assertEquals(9, states.size)
        // Ensure latest is OnStartLine; should be Perfect.
        assert(states[8] is WorldMapRaceUiState.OnStartLine)
        assert((states[8] as WorldMapRaceUiState.OnStartLine).startLineState is StartLineState.Perfect)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testFinishRace() = runTest {
        // Import the example1 JSON.
        val example1Track = getTrackWithPathFromResource(R.raw.example1)
        // Build a list of all points we'll be using.
        val testPoints = listOf(
            RequestPlayerUpdate(-37.843652, 145.03001, 180f, 1f, System.currentTimeMillis())
        )
        // Insert this track into cache.
        trackWithPathLocalData.upsertTrackWithPath(example1Track)
        // Insert a new vehicle, belonging to us.
        createVehicleUseCase(RequestCreateVehicle("1994 Toyota Supra")).collect()
        // Setup a collection for the primary race UI state.
        val states = mutableListOf<WorldMapRaceUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            worldMapRaceViewModel.worldMapRaceUiState.toList(states)
        }

        // Send the initial point as the very first point in the test track.
        sendPlayerUpdateUseCase(testPoints[0])
        // Now, set the targeted track's UID in the view model.
        worldMapRaceViewModel.setTrackUid(example1Track.track.trackUid)
        // Wait for the states list to be 2 items in size. Ensure the last item is of type to be OnStartLine.
        states.waitForSize(this, 2)
        assertEquals(2, states.size)
        // Ensure latest is OnStartLine; should be Perfect.
        assert(states[1] is WorldMapRaceUiState.OnStartLine)
        assert((states[1] as WorldMapRaceUiState.OnStartLine).startLineState is StartLineState.Perfect)
        // Now, start a race.
        val onStartLine = states[1] as WorldMapRaceUiState.OnStartLine
        // Ensure on start line has one vehicle available for use.
        assertEquals(1, onStartLine.yourVehicles.size)
        val perfect = onStartLine.startLineState as StartLineState.Perfect
        // Use the track and track path and location.
        worldMapRaceViewModel.startRace(
            onStartLine.yourVehicles.first(),
            onStartLine.track,
            perfect.location
        )
        // Wait for states to grow to 8 in size, extra (0, 1, 2, 3, 4, RACING).
        states.waitForSize(this, 8)
        assertEquals(8, states.size)
        // Get states 2-7, these are the countdown second states.
        val countdownStates = states.subList(2, 7)
        // Ensure that from the first to last, current second matches 0, 1, 2, 3, 4
        for(i in 0 until 5) {
            // Ensure its a countdown state.
            assert(countdownStates[i] is WorldMapRaceUiState.CountingDown)
            assertEquals(i, (countdownStates[i] as WorldMapRaceUiState.CountingDown).currentSecond)
        }
        // Ensure the latest state is a Racing state.
        assert(states[7] is WorldMapRaceUiState.Racing)

        // Now, get the race from cache.
        val raceModel = raceLocalData.selectRace(
            RequestGetRace("RACE01")
        ).first()
        // Assert its not null.
        assertNotNull(raceModel)
        raceModel!!
        // Now, upsert another race model, almost copy of this one, but set the latest version as finished.
        raceLocalData.upsertRace(
            RaceModel(
                raceModel.raceUid,
                raceModel.trackUid,
                raceModel.started,
                System.currentTimeMillis(),
                false,
                null,
                false,
                50,
                null,
                100
            )
        )
        // Now, wait for our states list to grow to 9 in size.
        states.waitForSize(this, 9)
        // Assert the latest is a Finished state.
        assert(states[8] is WorldMapRaceUiState.Finished)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testCancelRace() = runTest {
        // Import the example1 JSON.
        val example1Track = getTrackWithPathFromResource(R.raw.example1)
        // Build a list of all points we'll be using.
        val testPoints = listOf(
            RequestPlayerUpdate(-37.843652, 145.03001, 180f, 1f, System.currentTimeMillis())
        )
        // Insert this track into cache.
        trackWithPathLocalData.upsertTrackWithPath(example1Track)
        // Insert a new vehicle, belonging to us.
        createVehicleUseCase(RequestCreateVehicle("1994 Toyota Supra")).collect()
        // Setup a collection for the primary race UI state.
        val states = mutableListOf<WorldMapRaceUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            worldMapRaceViewModel.worldMapRaceUiState.toList(states)
        }

        // Send the initial point as the very first point in the test track.
        sendPlayerUpdateUseCase(testPoints[0])
        // Now, set the targeted track's UID in the view model.
        worldMapRaceViewModel.setTrackUid(example1Track.track.trackUid)
        // Wait for the states list to be 2 items in size. Ensure the last item is of type to be OnStartLine.
        states.waitForSize(this, 2)
        assertEquals(2, states.size)
        // Ensure latest is OnStartLine; should be Perfect.
        assert(states[1] is WorldMapRaceUiState.OnStartLine)
        assert((states[1] as WorldMapRaceUiState.OnStartLine).startLineState is StartLineState.Perfect)
        // Now, start a race.
        val onStartLine = states[1] as WorldMapRaceUiState.OnStartLine
        // Ensure on start line has one vehicle available for use.
        assertEquals(1, onStartLine.yourVehicles.size)
        val perfect = onStartLine.startLineState as StartLineState.Perfect
        // Use the track and track path and location.
        worldMapRaceViewModel.startRace(
            onStartLine.yourVehicles.first(),
            onStartLine.track,
            perfect.location
        )
        // Wait for states to grow to 4 in size, extra (0, 1).
        states.waitForSize(this, 4)
        assertEquals(4, states.size)
        // Cancel the race right now, mid countdown.
        // Now, call out to the view model to cancel the current race.
        worldMapRaceViewModel.cancelRace()
        // Now, wait for our states list to grow to 6 in size.
        states.waitForSize(this, 6)
        // Ensure index 4 is a loading state.
        assert(states[4] is WorldMapRaceUiState.Loading)
        // Assert the latest is a Cancelled state.
        assert(states[5] is WorldMapRaceUiState.Cancelled)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testDisqualifyRace() = runTest {
        // Import the example1 JSON.
        val example1Track = getTrackWithPathFromResource(R.raw.example1)
        // Build a list of all points we'll be using.
        val testPoints = listOf(
            RequestPlayerUpdate(-37.843652, 145.03001, 180f, 1f, System.currentTimeMillis())
        )
        // Insert this track into cache.
        trackWithPathLocalData.upsertTrackWithPath(example1Track)
        // Insert a new vehicle, belonging to us.
        createVehicleUseCase(RequestCreateVehicle("1994 Toyota Supra")).collect()
        // Setup a collection for the primary race UI state.
        val states = mutableListOf<WorldMapRaceUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            worldMapRaceViewModel.worldMapRaceUiState.toList(states)
        }

        // Send the initial point as the very first point in the test track.
        sendPlayerUpdateUseCase(testPoints[0])
        // Now, set the targeted track's UID in the view model.
        worldMapRaceViewModel.setTrackUid(example1Track.track.trackUid)
        // Wait for the states list to be 2 items in size. Ensure the last item is of type to be OnStartLine.
        states.waitForSize(this, 2)
        assertEquals(2, states.size)
        // Ensure latest is OnStartLine; should be Perfect.
        assert(states[1] is WorldMapRaceUiState.OnStartLine)
        assert((states[1] as WorldMapRaceUiState.OnStartLine).startLineState is StartLineState.Perfect)
        // Now, start a race.
        val onStartLine = states[1] as WorldMapRaceUiState.OnStartLine
        // Ensure on start line has one vehicle available for use.
        assertEquals(1, onStartLine.yourVehicles.size)
        val perfect = onStartLine.startLineState as StartLineState.Perfect
        // Use the track and track path and location.
        worldMapRaceViewModel.startRace(
            onStartLine.yourVehicles.first(),
            onStartLine.track,
            perfect.location
        )
        // Wait for states to grow to 8 in size, extra (0, 1, 2, 3, 4, RACING).
        states.waitForSize(this, 8)
        assertEquals(8, states.size)
        // Get states 2-7, these are the countdown second states.
        val countdownStates = states.subList(2, 7)
        // Ensure that from the first to last, current second matches 0, 1, 2, 3, 4
        for(i in 0 until 5) {
            // Ensure its a countdown state.
            assert(countdownStates[i] is WorldMapRaceUiState.CountingDown)
            assertEquals(i, (countdownStates[i] as WorldMapRaceUiState.CountingDown).currentSecond)
        }
        // Ensure the latest state is a Racing state.
        assert(states[7] is WorldMapRaceUiState.Racing)

        // Now, get the race from cache.
        val raceModel = raceLocalData.selectRace(
            RequestGetRace("RACE01")
        ).first()
        // Assert its not null.
        assertNotNull(raceModel)
        raceModel!!
        // Now, upsert another race model, almost copy of this one, but set the latest version as disqualified.
        raceLocalData.upsertRace(
            RaceModel(
                raceModel.raceUid,
                raceModel.trackUid,
                raceModel.started,
                null,
                true,
                "moved-away",
                false,
                0,
                null,
                0
            )
        )
        // Now, wait for our states list to grow to 9 in size.
        states.waitForSize(this, 9)
        // Assert the latest is a Disqualified state.
        assert(states[8] is WorldMapRaceUiState.Disqualified)
    }
}