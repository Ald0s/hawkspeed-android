package com.vljx.hawkspeed

import com.vljx.hawkspeed.base.BaseTest
import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.di.module.CommonModule
import com.vljx.hawkspeed.data.di.module.DataModule
import com.vljx.hawkspeed.data.di.module.DatabaseModule
import com.vljx.hawkspeed.data.di.module.DomainModule
import com.vljx.hawkspeed.data.source.track.TrackPathLocalData
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendPlayerUpdateUseCase
import com.vljx.hawkspeed.domain.usecase.track.ClearTrackRatingUseCase
import com.vljx.hawkspeed.domain.usecase.track.DownvoteTrackUseCase
import com.vljx.hawkspeed.domain.usecase.track.GetTrackLatestCommentsUseCase
import com.vljx.hawkspeed.domain.usecase.track.GetTrackUseCase
import com.vljx.hawkspeed.domain.usecase.track.UpvoteTrackUseCase
import com.vljx.hawkspeed.ui.screens.dialogs.trackpreview.TrackPreviewUiState
import com.vljx.hawkspeed.ui.screens.dialogs.trackpreview.TrackPreviewViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@UninstallModules(CommonModule::class, DatabaseModule::class, DataModule::class, DomainModule::class)
@HiltAndroidTest
class TrackPreviewViewModelTest: BaseTest() {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var trackWithPathLocalData: TrackPathLocalData

    @Inject
    lateinit var getCurrentLocationUseCase: GetCurrentLocationUseCase
    @Inject
    lateinit var getTrackUseCase: GetTrackUseCase
    @Inject
    lateinit var upvoteTrackUseCase: UpvoteTrackUseCase
    @Inject
    lateinit var downvoteTrackUseCase: DownvoteTrackUseCase
    @Inject
    lateinit var clearTrackRatingUseCase: ClearTrackRatingUseCase
    @Inject
    lateinit var getTrackLatestCommentsUseCase: GetTrackLatestCommentsUseCase

    @Inject
    lateinit var sendPlayerUpdateUseCase: SendPlayerUpdateUseCase

    private lateinit var trackPreviewViewModel: TrackPreviewViewModel

    @Before
    fun setUp() {
        hiltRule.inject()
        appDatabase.clearAllTables()
        trackPreviewViewModel = TrackPreviewViewModel(
            getCurrentLocationUseCase, getTrackUseCase, upvoteTrackUseCase, downvoteTrackUseCase, clearTrackRatingUseCase, getTrackLatestCommentsUseCase
        )
    }

    @After
    fun tearDown() {
        appDatabase.clearAllTables()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testOpenTrackPreview() = runTest {
        // Import the example1 JSON.
        val example1Track = getTrackWithPathFromResource(R.raw.example1)
        // Build a list of all points we'll be using. First is ages away from track, second is 12.6 meters away, third is 2.4 meters away, fourth is the very first point.
        val testPoints = listOf(
            RequestPlayerUpdate(-37.757557, 144.958444, 180f, 1f, System.currentTimeMillis()),
            RequestPlayerUpdate(-37.843675, 145.030150, 180f, 1f, System.currentTimeMillis()+500),
            RequestPlayerUpdate(-37.843659, 145.030036, 180f, 1f, System.currentTimeMillis()+1000),
            RequestPlayerUpdate(-37.843652, 145.03001, 180f, 1f, System.currentTimeMillis())
        )
        // Insert this track into cache.
        trackWithPathLocalData.upsertTrackWithPath(example1Track)
        // Setup a collection for the track preview UI state.
        val states = mutableListOf<TrackPreviewUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            trackPreviewViewModel.trackPreviewUiState.toList(states)
        }

        // Send the initial point to ages away from the track.
        sendPlayerUpdateUseCase(testPoints[0])
        // Now, select our track in view model.
        trackPreviewViewModel.selectTrack(example1Track.track.trackUid)
        // Wait for states to grow to 2 in size.
        states.waitForSize(this, 2)
        // Now the first entry should be loading, the second should be a TrackPreview.
        assert(states[0] is TrackPreviewUiState.Loading)
        assert(states[1] is TrackPreviewUiState.TrackPreview)
    }
}