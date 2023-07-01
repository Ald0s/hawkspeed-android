package com.vljx.hawkspeed

import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.di.module.DataModule
import com.vljx.hawkspeed.data.di.module.DatabaseModule
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestLeaveWorld
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendPlayerUpdateUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.AddTrackPointDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.DeleteTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.GetTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.NewTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.ResetTrackDraftPointsUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.SaveTrackDraftUseCase
import com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack.WorldMapRecordTrackUiState
import com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack.WorldMapRecordTrackViewModel
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import timber.log.Timber
import javax.inject.Inject

@UninstallModules(DatabaseModule::class, DataModule::class)
@HiltAndroidTest
class WorldMapRecordTrackViewModelTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var getCurrentLocationUseCase: GetCurrentLocationUseCase
    @Inject
    lateinit var newTrackDraftUseCase: NewTrackDraftUseCase
    @Inject
    lateinit var getTrackDraftUseCase: GetTrackDraftUseCase
    @Inject
    lateinit var saveTrackDraftUseCase: SaveTrackDraftUseCase
    @Inject
    lateinit var deleteTrackDraftUseCase: DeleteTrackDraftUseCase
    @Inject
    lateinit var addTrackPointDraftUseCase: AddTrackPointDraftUseCase
    @Inject
    lateinit var resetTrackDraftPointsUseCase: ResetTrackDraftPointsUseCase

    @Inject
    lateinit var sendPlayerUpdateUseCase: SendPlayerUpdateUseCase

    private lateinit var worldMapRecordTrackViewModel: WorldMapRecordTrackViewModel

    @Before
    fun setUp() {
        hiltRule.inject()
        appDatabase.clearAllTables()
        worldMapRecordTrackViewModel = WorldMapRecordTrackViewModel(
            getCurrentLocationUseCase, newTrackDraftUseCase, getTrackDraftUseCase, saveTrackDraftUseCase, deleteTrackDraftUseCase, addTrackPointDraftUseCase, resetTrackDraftPointsUseCase
        )
    }

    @After
    fun tearDown() {
        appDatabase.clearAllTables()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testRecordTrackViewModel(): Unit = runTest {
        // Going to setup an await type function that checks for a new emission.
        suspend fun MutableList<WorldMapRecordTrackUiState>.waitForSize(waitFor: Int) {
            val waitForNext = async {
                while(size < waitFor) {
                    delay(200)
                }
            }
            waitForNext.await()
        }

        // Create a list of RequestPlayerUpdate.
        val playerUpdateRequests = listOf<RequestPlayerUpdate>(
            RequestPlayerUpdate(-37.843652, 145.03001, 179f, 70f, 0L),

            RequestPlayerUpdate(-37.84354, 145.029053, 179f, 70f, 0L),
            RequestPlayerUpdate(-37.84354, 145.029053, 179f, 70f, 0L),

            RequestPlayerUpdate(-37.843528, 145.028946, 179f, 70f, 0L),
            RequestPlayerUpdate(-37.843473, 145.028619, 179f, 70f, 0L)
        )

        // Start a collection of all states from the record track UI state flow, to a list.
        val states = mutableListOf<WorldMapRecordTrackUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            worldMapRecordTrackViewModel.recordTrackUiState.toList(states)
        }

        // Send the very first update.
        sendPlayerUpdateUseCase(playerUpdateRequests[0])
        // Now we'd have opened the record view model, and so the new track function is called.
        worldMapRecordTrackViewModel.newTrack()
        // Wait for 2 items.
        states.waitForSize(2)
        // Assert the second state is of type NewTrack.
        assert(states[1] is WorldMapRecordTrackUiState.NewTrack)
        // Now, call the record function to begin recording a track.
        worldMapRecordTrackViewModel.startRecording()
        // Wait for size 3, then ensure that it is of type Recording.
        states.waitForSize(3)
        assert(states[2] is WorldMapRecordTrackUiState.Recording)
        // Ensure there is a single point in the track draft.
        assertEquals(1, (states[2] as WorldMapRecordTrackUiState.Recording).trackDraftWithPoints.pointDrafts.size)
        // Now, send the second player update.
        sendPlayerUpdateUseCase(playerUpdateRequests[1])
        // Wait for size to grow to 4, the newest state should still be Recording.
        states.waitForSize(4)
        assertEquals(4, states.size)
        assert(states[3] is WorldMapRecordTrackUiState.Recording)
        // Ensure there are two points in the latest state.
        assertEquals(2, (states[3] as WorldMapRecordTrackUiState.Recording).trackDraftWithPoints.pointDrafts.size)
        // Emit the next update request, which is the same as the last.
        sendPlayerUpdateUseCase(playerUpdateRequests[2])
        // TODO: timeout test to confirm there were no new emissions, since location had not changed.
        // Now, stop the recording.
        worldMapRecordTrackViewModel.stopRecording()
        // Wait for an emission. Ensure there are now 5 emissions, and the latest is an instance of RecordedTrackOverview.
        states.waitForSize(5)
        assertEquals(5, states.size)
        assert(states[4] is WorldMapRecordTrackUiState.RecordedTrackOverview)
        // Now, reset the track.
        worldMapRecordTrackViewModel.resetTrack()
        // Ensure there are now 6 emissions, and the latest is an instance of NewTrack.
        states.waitForSize(6)
        assertEquals(6, states.size)
        assert(states[5] is WorldMapRecordTrackUiState.NewTrack)
        // Call the cancel function, deleting the track draft.
        worldMapRecordTrackViewModel.cancelRecording((states[5] as WorldMapRecordTrackUiState.NewTrack).trackDraftWithPoints, false)
        // Wait for two sizes higher; skipping the loading state. Ensure the latest state is recording cancelled.
        states.waitForSize(8)
        assertEquals(8, states.size)
        assert(states[7] is WorldMapRecordTrackUiState.RecordingCancelled)
    }
}