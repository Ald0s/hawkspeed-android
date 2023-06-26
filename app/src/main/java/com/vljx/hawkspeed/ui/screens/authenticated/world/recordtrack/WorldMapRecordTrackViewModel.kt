package com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSaveTrackPointDraft
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationUseCase
import com.vljx.hawkspeed.domain.usecase.track.DeleteTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.GetTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.NewTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.ResetTrackDraftPointsUseCase
import com.vljx.hawkspeed.domain.usecase.track.SaveTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.SaveTrackPointDraftUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * TODO: allow editing at some stage.
 * For now, only new tracks, and deleting tracks if cancelling is allowed.
 */
@HiltViewModel
class WorldMapRecordTrackViewModel @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val newTrackDraftUseCase: NewTrackDraftUseCase,
    private val getTrackDraftUseCase: GetTrackDraftUseCase,
    private val saveTrackDraftUseCase: SaveTrackDraftUseCase,
    private val deleteTrackDraftUseCase: DeleteTrackDraftUseCase,

    private val saveTrackPointDraftUseCase: SaveTrackPointDraftUseCase,
    private val resetTrackDraftPointsUseCase: ResetTrackDraftPointsUseCase
): ViewModel() {
    /**
     * The Id for the currently selected track draft. This is configured such that emitting a null value to this flow will create a new track draft and
     * use that for any incoming recordings.
     */
    private val mutableSelectedTrackDraftId: MutableSharedFlow<Long?> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    /**
     * A mutable shared flow, configured to refrain from replays, for an external input for controlling the world map record track UI state.
     */
    private val mutableWorldMapRecordTrackUiState: MutableSharedFlow<WorldMapRecordTrackUiState> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    /**
     * A mutable state flow for a boolean that indicates whether we are recording or not. True is recording, false otherwise.
     */
    private val mutableIsRecording: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Get the current location from the world socket state. We'll use emissions from this flow to update our draft track.
     */
    private val currentLocation: StateFlow<PlayerPosition?> =
        getCurrentLocationUseCase(Unit)

    /**
     * Flat map the latest selected track draft's Id and attempt to locate the associated track draft from cache, or, if null was emitted to selected track
     * draft Id, create and use a brand new track draft.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val trackDraftWithPoints: Flow<TrackDraftWithPoints> =
        mutableSelectedTrackDraftId.flatMapLatest { selectedTrackDraftId ->
            if(selectedTrackDraftId == null) {
                newTrackDraftUseCase(Unit).map {
                    // TODO: delete debug message here.
                    Timber.d("Created new track with id: ${it.trackDraftId}")
                    it
                }
            } else {
                throw NotImplementedError("Editing a track is not yet supported.")
            }
        }

    /**
     * A flow that will combine the track draft flow, 'is recording' state flow, and the current location state flow to save new points to the track
     * where appropriate and applicable. It's result is a flow of the latest track draft with the latest location added.
     */
    private val latestTrackDraftWithPoints: Flow<TrackDraftWithPoints> =
        combineTransform(
            mutableIsRecording,
            currentLocation,
            trackDraftWithPoints
        ) { isRecording, location, trackDraft ->
            // If recording is false OR location is null, emit nothing, since nothing changed.
            if(!isRecording || location == null) {
                // Emit nothing.
                return@combineTransform
            }
            // Sure recording is true and location is not null. Determine if latest location is far enough away from last point. If not far away enough, emit
            // nothing, since nothing changed.
            if(!trackDraft.shouldTakePosition(location)) {
                // Emit nothing.
                return@combineTransform
            }
            // Otherwise, we'll save the latest location to the track draft with points, receiving back the latest; which we'll return.
            val latestTrackDraft = saveTrackPointDraftUseCase(
                RequestSaveTrackPointDraft(
                    trackDraft.trackDraftId,
                    location.latitude,
                    location.longitude,
                    location.loggedAt,
                    location.speed,
                    location.rotation
                )
            )
            // TODO: remove debug message.
            Timber.d("Saved point to track draft Id ${latestTrackDraft.trackDraftId}, now contains ${latestTrackDraft.pointDrafts.size} points.")
            emit(latestTrackDraft)
        }

    /**
     * The UI state for the track recording. This is a combination of the track draft, recording indicator and the current location. This flow will combine
     * the is recording flow, the latest track draft with points flow.
     */
    private val worldMapRecordTrackUiState: Flow<WorldMapRecordTrackUiState> =
        combine(
            mutableIsRecording,
            latestTrackDraftWithPoints
        ) { isRecording, latestTrackDraft ->
            // Now, return the most appropriate UI states.
            when(isRecording) {
                /**
                 * If we are set to recording, emit the recording state along with latest track draft.
                 */
                true -> WorldMapRecordTrackUiState.Recording(latestTrackDraft)

                /**
                 * Otherwise, if we're not recording, emit recorded track overview if the latest track draft has a valid track recorded. Otherwise, emit
                 * new track; since it should be an empty track.
                 */
                false -> {
                    if(latestTrackDraft.hasRecordedTrack) {
                        return@combine WorldMapRecordTrackUiState.RecordedTrackOverview(latestTrackDraft)
                    } else {
                        return@combine WorldMapRecordTrackUiState.NewTrack(latestTrackDraft)
                    }
                }
            }
        }

    /**
     * The public world map record track UI state, a merge of the internal version and the mutable shared flow. So the most recent emission from those
     * flows are considered. The default state will be loading.
     */
    val recordTrackUiState: StateFlow<WorldMapRecordTrackUiState> =
        merge(
            worldMapRecordTrackUiState,
            mutableWorldMapRecordTrackUiState
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WorldMapRecordTrackUiState.Loading)

    /**
     * Create a new Track. This will emit null to the selected track draft Id, to have that mapped to an actual blank track draft with points.
     */
    fun newTrack() {
        viewModelScope.launch {
            mutableSelectedTrackDraftId.emit(null)
        }
    }

    /**
     * Edit an existing Track.
     */
    fun editTrack(trackDraftId: Long) {
        throw NotImplementedError("Editing a track is not yet allowed on record track.")
    }

    /**
     * To be called when the User is happy with their recorded track, and wishes to fill out its details now.
     */
    fun recordingComplete() {
        // Ensure the current UI state is a recording overview, which means we have a valid recording. Fail otherwise.
        if(recordTrackUiState.value !is WorldMapRecordTrackUiState.RecordedTrackOverview) {
            // TODO: handle this properly.
            throw NotImplementedError("Failed to complete track recording. UI state must be recorded track overview for this to be possible. Also, this is not handled.")
        }
        // Get the UI state as a recorded track overview.
        val recordedTrackOverview = recordTrackUiState.value as WorldMapRecordTrackUiState.RecordedTrackOverview
        // Emit loading to the mutable UI state, to recompose to loading.
        mutableWorldMapRecordTrackUiState.tryEmit(WorldMapRecordTrackUiState.Loading)
        // Launch a coroutine scope.
        viewModelScope.launch {
            // Now, save this recorded track into cache.
            // TODO: save recorded track as it is. Though, is this necessary?
            // Now that we've saved the track, we'll get back our saved version.
            val savedTrackDraft = recordedTrackOverview.trackDraftWithPoints
            // Wit this, we'll emit a recording complete UI state.
            mutableWorldMapRecordTrackUiState.emit(
                WorldMapRecordTrackUiState.RecordingComplete(savedTrackDraft)
            )
        }
    }

    /**
     * Saves the track draft, as it is, to cache, then submits
     */
    /**
     * Start the recording of the current track.
     */
    fun startRecording() {
        // TODO: delete debug message.
        Timber.d("Starting record of track now...")
        mutableIsRecording.tryEmit(true)
    }

    /**
     * Reset the currently recorded track. For this function to do anything, the current UI state must be in the recorded overview position.
     */
    fun resetTrack() {
        if(recordTrackUiState.value !is WorldMapRecordTrackUiState.RecordedTrackOverview) {
            // TODO: handle this properly.
            throw NotImplementedError("Failed to reset track. UI state must be recorded track overview for this to be possible. Also, this is not handled.")
        }
        viewModelScope.launch {
            // TODO: emitting loading has been removed since it would actually recompose the entire screen. Maybe we don't actually want this.
            // Emit loading to the mutable shared flow.
            //mutableWorldMapRecordTrackUiState.tryEmit(WorldMapRecordTrackUiState.Loading)

            // Now, call out to reset track draft points use case and instruct points for that track draft to be cleared. This should result in the latest track draft
            // with points for this track draft to be emitted, updating UI state in any case, to new track.
            resetTrackDraftPointsUseCase(
                (recordTrackUiState.value as WorldMapRecordTrackUiState.RecordedTrackOverview).trackDraftWithPoints.trackDraftId
            )
        }
    }

    /**
     * Stop the recording.
     */
    fun stopRecording() {
        // TODO: delete debug message.
        Timber.d("Stopping record of track now.")
        mutableIsRecording.tryEmit(false)
    }
}