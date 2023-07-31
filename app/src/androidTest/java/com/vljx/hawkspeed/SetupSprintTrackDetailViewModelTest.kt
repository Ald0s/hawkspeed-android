package com.vljx.hawkspeed

import androidx.lifecycle.SavedStateHandle
import com.vljx.hawkspeed.base.BaseTest
import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.di.module.CommonModule
import com.vljx.hawkspeed.data.di.module.DataModule
import com.vljx.hawkspeed.data.di.module.DatabaseModule
import com.vljx.hawkspeed.data.di.module.DomainModule
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestAddTrackPointDraft
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestNewTrackDraft
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestTrackPointDraft
import com.vljx.hawkspeed.domain.usecase.track.SubmitTrackUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.AddTrackPointDraftExUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.DeleteTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.GetTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.NewTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.SaveTrackDraftUseCase
import com.vljx.hawkspeed.ui.screens.authenticated.setupsprinttrack.SetupSprintTrackDetailFormUiState
import com.vljx.hawkspeed.ui.screens.authenticated.setupsprinttrack.SetupSprintTrackDetailUiState
import com.vljx.hawkspeed.ui.screens.authenticated.setupsprinttrack.SetupSprintTrackDetailViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class SetupSprintTrackDetailViewModelTest: BaseTest() {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var newTrackDraftUseCase: NewTrackDraftUseCase
    @Inject
    lateinit var getTrackDraftUseCase: GetTrackDraftUseCase
    @Inject
    lateinit var saveTrackDraftUseCase: SaveTrackDraftUseCase
    @Inject
    lateinit var addTrackPointDraftUseCaseEx: AddTrackPointDraftExUseCase
    @Inject
    lateinit var submitTrackUseCase: SubmitTrackUseCase
    @Inject
    lateinit var deleteTrackDraftUseCase: DeleteTrackDraftUseCase

    @Inject
    lateinit var appDatabase: AppDatabase

    private lateinit var setupSprintTrackDetailViewModel: SetupSprintTrackDetailViewModel

    @Inject
    @IODispatcher
    lateinit var testDispatcher: CoroutineDispatcher

    @Before
    fun setUp() {
        hiltRule.inject()
        appDatabase.clearAllTables()
    }

    @After
    fun tearDown() {
        appDatabase.clearAllTables()
    }

    suspend fun setupTrackDraftWithPoints(
        trackName: String? = null,
        trackDescription: String? = null,
        requestedPointDrafts: List<RequestTrackPointDraft>? = null
    ): TrackDraftWithPoints {
        // Create a new blank sprint type track.
        val requestNewTrackDraft = RequestNewTrackDraft(TrackType.SPRINT)
        var trackDraftWithPoints = newTrackDraftUseCase(requestNewTrackDraft).first()
        // Now, if track name, description or requested point drafts are provided, we will apply these and request an update.
        if(trackName != null || trackDescription != null || requestedPointDrafts != null) {
            var newTrackDraftWithPoints = TrackDraftWithPoints(
                trackDraftWithPoints.trackDraftId,
                trackDraftWithPoints.trackType,
                trackName,
                trackDescription,
                requestedPointDrafts?.map { requestTrackPointDraft ->
                    // We must save all of these points to the cache as belonging to our new track draft.
                    addTrackPointDraftUseCaseEx(
                        RequestAddTrackPointDraft(
                            trackDraftWithPoints.trackDraftId,
                            requestTrackPointDraft
                        )
                    )
                } ?: listOf()
            )
            // After setting optional args, save the new version to cache, overwriting existing track draft with points.
            saveTrackDraftUseCase(newTrackDraftWithPoints)
            trackDraftWithPoints = getTrackDraftUseCase(trackDraftWithPoints.trackDraftId).first()
                ?: throw NotImplementedError("Failed to continue with test; after updating new track draft with arguments, no actual value was returned!")
        }
        // Now, setup the setup track detail view model for this track draft.
        setupSprintTrackDetailViewModel = SetupSprintTrackDetailViewModel(
            SavedStateHandle(mapOf(SetupSprintTrackDetailViewModel.ARG_TRACK_DRAFT_ID to trackDraftWithPoints.trackDraftId)),
            getTrackDraftUseCase,
            submitTrackUseCase,
            saveTrackDraftUseCase,
            deleteTrackDraftUseCase,
            testDispatcher
        )
        // Return this.
        return trackDraftWithPoints
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSetupTrackDetailBasics() = runTest {
        // Import the example1 JSON.
        val example1Track = getTrackWithPathFromResource(R.raw.example1)
        // Setup a track draft with points, only set requested points.
        val trackDraftWithPoints = setupTrackDraftWithPoints(
            requestedPointDrafts = example1Track.trackPathWithPoints!!.points.mapIndexed { index, trackPointModel ->
                // Map each track point model to a requested point draft.
                RequestTrackPointDraft(trackPointModel.latitude, trackPointModel.longitude, System.currentTimeMillis() + (index * 500), 70f, 180f)
            }
        )

        // Setup a collection for the UI state
        val states = mutableListOf<SetupSprintTrackDetailUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            setupSprintTrackDetailViewModel.setupSprintTrackDetailUiState.toList(states)
        }
        // Wait for the states collection to grow to 1 in length.
        states.waitForSize(this, 1)
        // Ensure its loading.
        assert(states[0] is SetupSprintTrackDetailUiState.Loading)

        // Wait for the states collection to grow to 3 in length.
        states.waitForSize(this, 3)
        // Ensure the second is a ShowDetailForm, the third and fourth will be emissions for track name and description being null..
        assert(states[1] is SetupSprintTrackDetailUiState.ShowDetailFormSprint)
        val showDetailForm = states[1] as SetupSprintTrackDetailUiState.ShowDetailFormSprint
        // Confirm the identity of track draft with points matches that at the top, and confirm there are more than 0 points.
        assertEquals(trackDraftWithPoints.trackDraftId, showDetailForm.trackDraftWithPoints.trackDraftId)
        assert(trackDraftWithPoints.pointDrafts.isNotEmpty())
        // Confirm the inner setup track detail form state is TrackDetailForm.
        assert(showDetailForm.setupSprintTrackDetailFormUiState is SetupSprintTrackDetailFormUiState.SprintTrackDetailForm)

        // Now update both track name and track description.
        setupSprintTrackDetailViewModel.updateTrackName("New Track")
        setupSprintTrackDetailViewModel.updateTrackDescription("A cool track.")
        // TODO: add tests here for ensuring validation on both name & description work.

        // Wait for the states collection to grow to 5 in length.
        states.waitForSize(this, 5)
        // Ensure top is a ShowDetailForm.
        assert(states[4] is SetupSprintTrackDetailUiState.ShowDetailFormSprint)

        // We are ready to submit. So call view model to make that happen.
        setupSprintTrackDetailViewModel.createTrack()
        // Wait for the states collection to grow to 5, this should be a ShowDetailForm, but within that show detail form, the form state should be Submitting.
        states.waitForSize(this, 6)
        // Ensure top is a ShowDetailForm.
        assert(states[5] is SetupSprintTrackDetailUiState.ShowDetailFormSprint)
        assert((states[5] as SetupSprintTrackDetailUiState.ShowDetailFormSprint).setupSprintTrackDetailFormUiState is SetupSprintTrackDetailFormUiState.Submitting)
        // Wait for it to grow to 7, and ensure this state is a TrackCreated.
        states.waitForSize(this, 7)
        // Ensure top is a ShowDetailForm.
        assert(states[6] is SetupSprintTrackDetailUiState.SprintTrackCreated)
    }

    // TODO: write tests for serverrefused.
}