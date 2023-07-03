package com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestAddTrackPointDraft
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestTrackPointDraft
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.DeleteTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.GetTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.NewTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.ResetTrackDraftPointsUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.SaveTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.AddTrackPointDraftUseCase
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
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

    private val addTrackPointDraftUseCase: AddTrackPointDraftUseCase,
    private val resetTrackDraftPointsUseCase: ResetTrackDraftPointsUseCase
): ViewModel() {
    /**
     * A mutable shared flow for the required track draft state. Emit to this flow to control the track being edited/recorded.
     */
    private val mutableSelectedTrackDraftState: MutableSharedFlow<SelectTrackDraftState> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
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
    private val innerCurrentLocation: StateFlow<PlayerPosition?> =
        getCurrentLocationUseCase(Unit)

    /**
     * Flat map the latest emissions from our selected track draft flow. If the selected state is an intent to create a track draft, return a flow for a new track draft
     * with points, and emit this as a NewTrack intent state to the selected track draft state. Otherwise, simply emit a flow to query whichever track draft is selected
     * from cache.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val trackDraftWithPoints: Flow<TrackDraftWithPoints> =
        mutableSelectedTrackDraftState.flatMapLatest { selectTrackDraftState ->
            when(selectTrackDraftState) {
                is SelectTrackDraftState.CreateTrackDraft -> {
                    newTrackDraftUseCase(Unit).onEach {
                        mutableSelectedTrackDraftState.emit(SelectTrackDraftState.SelectTrackDraft(it.trackDraftId))
                        mutableWorldMapRecordTrackUiState.emit(
                            WorldMapRecordTrackUiState.NewTrack(it)
                        )
                    }
                }
                is SelectTrackDraftState.SelectTrackDraft -> {
                    // TODO: properly handle this track and its points no longer existing. For now, we'll just filter not null.
                    getTrackDraftUseCase(
                        selectTrackDraftState.trackDraftId
                    ).filterNotNull()
                }
            }
        }

    /**
     * A flow that will combine the track draft flow, 'is recording' state flow, and the current location state flow to save new points to the track
     * where appropriate and applicable. It's result is a flow of the latest track draft with the latest location added.
     */
    private val trackDraftWithRecordedPoint: Flow<TrackDraftWithPoints> =
        combineTransform(
            mutableIsRecording,
            innerCurrentLocation,
            trackDraftWithPoints
        ) { isRecording, location, trackDraft ->
            // If recording is false OR location is null, emit nothing as this flow is not required.
            if(!isRecording || location == null) {
                return@combineTransform
            }
            // Sure recording is true and location is not null. Determine if latest location is far enough away from last point. If not far away enough, emit
            // the existing track draft, to ensure we're in the Recording state.
            if(!trackDraft.shouldTakePosition(location)) {
                // Decided not to take this position, but we'll emit the existing track draft.
                emit(trackDraft)
                return@combineTransform
            }
            // Otherwise, we'll save the latest location to the track draft with points, receiving back the latest; which we'll return.
            val latestTrackDraft = addTrackPointDraftUseCase(
                RequestAddTrackPointDraft(
                    trackDraft.trackDraftId,
                    RequestTrackPointDraft(
                        location.latitude,
                        location.longitude,
                        location.loggedAt,
                        location.speed,
                        location.rotation
                    )
                )
            )
            emit(latestTrackDraft)
        }

    /**
     * A flow for the UI state of the track recorder. This will combine the 'is recording' flow and the track draft with the latest recorded point.
     */
    private val worldMapRecordTrackUiState: Flow<WorldMapRecordTrackUiState> =
        combine(
            mutableIsRecording,
            trackDraftWithRecordedPoint
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
                        Timber.d("Showing overview of track with ${latestTrackDraft.pointDrafts.size} recorded points.")
                        return@combine WorldMapRecordTrackUiState.RecordedTrackOverview(latestTrackDraft)
                    } else {
                        Timber.d("Starting up a new track.")
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
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(
            stopTimeoutMillis = 10000,
            replayExpirationMillis = 10000
        ), WorldMapRecordTrackUiState.Loading)

    /**
     * Publicise the current location.
     */
    val currentLocation: StateFlow<PlayerPosition?> =
        innerCurrentLocation

    /**
     * Create a new Track. This will call out to the new track use case, emitting the newly created track's id to the
     * selected track id mutable shared flow.
     */
    fun newTrack() {
        viewModelScope.launch {
            mutableSelectedTrackDraftState.emit(
                SelectTrackDraftState.CreateTrackDraft
            )
        }
    }

    /**
     * Edit an existing Track.
     */
    fun editTrack(trackDraftId: Long) {
        throw NotImplementedError("Editing a track is not yet allowed on record track.")
    }

    /**
     * Call when the User wishes to cancel the recording of this track. Optionally, the User can request to save their progress to the cache,
     * or delete it.
     */
    fun cancelRecording(trackDraftWithPoints: TrackDraftWithPoints, saveTrack: Boolean) {
        // Get the track draft with points.
        viewModelScope.launch {
            // Emit a loading state for the UI, to disable actions.
            mutableWorldMapRecordTrackUiState.tryEmit(WorldMapRecordTrackUiState.Loading)
            // On the view model scope, if User wants to save the track, invoke the save use case with the given track draft. Otherwise, invoke the
            // delete track draft use case.
            when(saveTrack) {
                true -> saveTrackDraftUseCase(trackDraftWithPoints)
                else -> deleteTrackDraftUseCase(trackDraftWithPoints.trackDraftId)
            }
            // Emit the cancelled state.
            mutableWorldMapRecordTrackUiState.emit(
                WorldMapRecordTrackUiState.RecordingCancelled(
                    trackDraftWithPoints,
                    saveTrack
                )
            )
        }
    }

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
            // Now, call out to reset track draft points use case and instruct points for that track draft to be cleared. This should result in the latest track draft
            // with points for this track draft to be emitted, updating UI state in any case, to new track.
            val trackDraftWithPoints = resetTrackDraftPointsUseCase(
                (recordTrackUiState.value as WorldMapRecordTrackUiState.RecordedTrackOverview).trackDraftWithPoints.trackDraftId
            )
            // Emit the received track draft with points, encapsulated in a new track state, to our mutable UI flow.
            mutableWorldMapRecordTrackUiState.emit(
                WorldMapRecordTrackUiState.NewTrack(trackDraftWithPoints)
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
}