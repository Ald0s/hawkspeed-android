package com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.vljx.hawkspeed.Extension.prettyLength
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.world.CurrentPlayer
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestAddTrackPointDraft
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestNewTrackDraft
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestTrackPointDraft
import com.vljx.hawkspeed.domain.usecase.account.GetCachedAccountUseCase
import com.vljx.hawkspeed.domain.usecase.account.GetSettingsUseCase
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.AddTrackPointDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.DeleteTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.GetTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.NewTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.ResetTrackDraftPointsUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.SaveTrackDraftUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
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
    getCachedAccountUseCase: GetCachedAccountUseCase,
    getSettingsUseCase: GetSettingsUseCase,
    getCurrentLocationUseCase: GetCurrentLocationUseCase,

    private val newTrackDraftUseCase: NewTrackDraftUseCase,
    private val getTrackDraftUseCase: GetTrackDraftUseCase,
    private val saveTrackDraftUseCase: SaveTrackDraftUseCase,
    private val deleteTrackDraftUseCase: DeleteTrackDraftUseCase,
    private val addTrackPointDraftUseCase: AddTrackPointDraftUseCase,
    private val resetTrackDraftPointsUseCase: ResetTrackDraftPointsUseCase,

    private val savedStateHandle: SavedStateHandle,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
): ViewModel() {
    /**
     * A mutable state flow for the required track draft state. By default, this will attempt to locate a selected track draft given an Id passed in saved state handle, but failing
     * that, the no track draft state will be used, thus triggering the UI request the User for a new track type.
     */
    private val mutableSelectedTrackDraftState: MutableStateFlow<SelectTrackDraftState> = MutableStateFlow(
        savedStateHandle.get<Long>(ARG_EXISTING_TRACK_DRAFT_ID)?.let { existingTrackDraftId ->
            SelectTrackDraftState.SelectTrackDraft(existingTrackDraftId)
        } ?: SelectTrackDraftState.NoTrackDraft
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
     * Get the current account cached in storage. We'll configure this as a state flow.
     */
    private val currentCachedAccount: StateFlow<Account?> =
        getCachedAccountUseCase(Unit)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * A flow that will collect all game settings.
     */
    private val gameSettings: StateFlow<GameSettings?> =
        getSettingsUseCase(Unit)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Get changes for the device's current position. No need for orientation changes while recording tracks.
     */
    private val innerCurrentLocation: StateFlow<PlayerPosition?> =
        getCurrentLocationUseCase(Unit)

    /**
     * A state flow for the current player's complete state. This is where we'll package the User in use, their changing location (according to world socket state)
     * and any other game settings relevant to drawing them.
     */
    private val innerCurrentPlayer: StateFlow<CurrentPlayer?> =
        combine(
            currentCachedAccount,
            gameSettings,
            innerCurrentLocation
        ) { account, settings, playerPosition ->
            if(account != null && playerPosition != null && settings != null) {
                CurrentPlayer(
                    account,
                    settings,
                    playerPosition
                )
            } else {
                null
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Flat map the latest emissions from our selected track draft flow. If the selected state is an intent to create a track draft, return a flow for a new track draft
     * with points, and emit this as a NewTrack intent state to the selected track draft state. Otherwise, simply emit a flow to query whichever track draft is selected
     * from cache. If there is no track draft at all, emit null.
     *
     * WARNING: For now, relatively untested, this flow is configured as a state flow, so that we can access the currently recorded track without issue. When we introduce editing
     * tracks, this solution may introduce bugs when navigating back to record screen etc.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val trackDraftWithPoints: StateFlow<TrackDraftWithPoints?> =
        mutableSelectedTrackDraftState.flatMapLatest { selectTrackDraftState ->
            when(selectTrackDraftState) {
                is SelectTrackDraftState.CreateTrackDraft -> {
                    newTrackDraftUseCase(
                        RequestNewTrackDraft(
                            selectTrackDraftState.chosenTrackType
                        )
                    ).onEach {
                        // Whenever new track draft emits a value, we'll emit its track draft Id to our selected track draft state, and also emit  the new track itself as
                        // a NewTrack state to the manual UI state controller.
                        mutableSelectedTrackDraftState.emit(
                            SelectTrackDraftState.SelectTrackDraft(it.trackDraftId)
                        )
                        mutableWorldMapRecordTrackUiState.emit(
                            WorldMapRecordTrackUiState.NewTrack(it)
                        )
                    }
                }
                is SelectTrackDraftState.SelectTrackDraft -> {
                    getTrackDraftUseCase(
                        selectTrackDraftState.trackDraftId
                    ) // Can simply filter null since this will never be null.
                }
                is SelectTrackDraftState.NoTrackDraft ->
                    flow { emit(null) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * A flow that will combine the track draft flow, 'is recording' state flow, and the current location state flow to save new points to the track
     * where appropriate and applicable. It's result is a flow of the latest track draft with the latest location added. If there is no track draft,
     * this flow will simply pass null along.
     */
    private val trackDraftWithRecordedPoint: SharedFlow<TrackDraftWithPoints?> =
        combineTransform(
            mutableIsRecording,
            innerCurrentLocation,
            trackDraftWithPoints
        ) { isRecording, location, trackDraft ->
            // If track draft is null, pass null along.
            if(trackDraft == null) {
                emit(null)
                return@combineTransform
            }
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
                    RequestTrackPointDraft(location)
                )
            )
            emit(latestTrackDraft)
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    /**
     * A flow for the UI state of the track recorder. This will combine the 'is recording' flow and the track draft with the latest recorded point. If the track draft with
     * recorded point(s) is null, throw an unsupported operation exception, which will request that the User select a track type and then create a new track draft.
     */
    private val worldMapRecordTrackUiState: SharedFlow<WorldMapRecordTrackUiState> =
        combine(
            mutableIsRecording,
            trackDraftWithRecordedPoint
        ) { isRecording, latestTrackDraft ->
            // Ensure latest track draft is not null, throw exc otherwise.
            latestTrackDraft
                ?: throw UnsupportedOperationException()
            // Calculate total length of the track draft so far.
            val totalLength: String = latestTrackDraft.pointDrafts
                .map { LatLng(it.latitude, it.longitude) }
                .prettyLength()
            // Now, return the most appropriate UI states.
            when(isRecording) {
                /**
                 * If we are set to recording, emit the recording state along with latest track draft.
                 */
                true -> WorldMapRecordTrackUiState.Recording(
                    latestTrackDraft,
                    totalLength
                )

                /**
                 * Otherwise, if we're not recording, emit recorded track overview if the latest track draft has a valid track recorded. Otherwise, emit
                 * new track; since it should be an empty track.
                 */
                false -> {
                    if(latestTrackDraft.hasRecordedTrack) {
                        Timber.d("Showing overview of track with ${latestTrackDraft.pointDrafts.size} recorded points.")
                        return@combine WorldMapRecordTrackUiState.RecordedTrackOverview(
                            latestTrackDraft,
                            totalLength
                        )
                    } else {
                        Timber.d("Starting up a new track.")
                        return@combine WorldMapRecordTrackUiState.NewTrack(latestTrackDraft)
                    }
                }
            }
        }.catch { exception ->
            if(exception is UnsupportedOperationException) {
                // If exception is an unsupported operation exception, we'll simply emit the MustCreateNewTrack state.
                emit(WorldMapRecordTrackUiState.MustCreateNewTrack)
            } else {
                // Otherwise, re-throw.
                throw exception
            }
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

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
     * Publicise the current player.
     */
    val currentPlayer: StateFlow<CurrentPlayer?> =
        innerCurrentPlayer

    /**
     * Create a new Track. This will call out to the new track use case, emitting the newly created track's id to the selected track id mutable shared flow.
     */
    fun newTrack(trackType: TrackType) {
        viewModelScope.launch(ioDispatcher) {
            mutableSelectedTrackDraftState.emit(
                SelectTrackDraftState.CreateTrackDraft(trackType)
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
        viewModelScope.launch(ioDispatcher) {
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
        Timber.d("Starting record of track now...")
        mutableIsRecording.tryEmit(true)
    }

    /**
     * Reset the currently recorded track.
     */
    fun resetTrack() {
        // Get current track draft with points.
        val trackDraft = trackDraftWithPoints.value
            ?: // TODO: handle this properly.
            throw NotImplementedError("Failed to reset track. UI state must be recorded track overview for this to be possible. Also, this is not handled.")
        viewModelScope.launch(ioDispatcher) {
            // Now, call out to reset track draft points use case and instruct points for that track draft to be cleared. This should result in the latest track draft
            // with points for this track draft to be emitted, updating UI state in any case, to new track.
            val trackDraftWithPoints = resetTrackDraftPointsUseCase(trackDraft.trackDraftId)
            // Emit the received track draft with points, encapsulated in a new track state, to our mutable UI flow.
            mutableWorldMapRecordTrackUiState.emit(
                WorldMapRecordTrackUiState.NewTrack(trackDraftWithPoints)
            )
        }
    }

    /**
     * Stop the recording. When this is triggered, the currently recorded track will be accessed, if an insufficient track has been recorded, this function will instead
     * act as a pseudo-reset function, and will clear the recorded track.
     */
    fun stopRecording() {
        viewModelScope.launch(ioDispatcher) {
            Timber.d("Stopping record of track now.")
            mutableIsRecording.value = false
            // Now, get the currently recorded track.
            val trackDraft = trackDraftWithPoints.value
            // If track draft not null, we will check to see if a reset is required.
            if(trackDraft != null && trackDraft.hasRecordedTrack && trackDraft.numRecordedPoints < MIN_POINTS_BE_RECORDED) {
                Timber.d("After recording of new track $trackDraft was stopped, we determined there aren't enough points to justify a recorded track (${trackDraft.numRecordedPoints}<${MIN_POINTS_BE_RECORDED}). The track has been cleared.")
                // Now simply clear all points in the track draft.
                val resetTrackDraftWithPoints = resetTrackDraftPointsUseCase(trackDraft.trackDraftId)
                // Emit the received track draft with points, encapsulated in a new track state, to our mutable UI flow.
                mutableWorldMapRecordTrackUiState.emit(
                    WorldMapRecordTrackUiState.NewTrack(resetTrackDraftWithPoints)
                )
            }
        }
    }

    /**
     * To be called when the User is happy with their recorded track, and wishes to fill out its details now.
     */
    fun recordingComplete(trackDraftWithPoints: TrackDraftWithPoints) {
        // Emit loading to the mutable UI state, to recompose to loading.
        mutableWorldMapRecordTrackUiState.tryEmit(WorldMapRecordTrackUiState.Loading)
        // Launch a coroutine scope.
        viewModelScope.launch(ioDispatcher) {
            // Now, save this recorded track into cache.
            // TODO: save recorded track as it is. Though, is this necessary?
            // Now that we've saved the track, we'll get back our saved version.
            val savedTrackDraft = trackDraftWithPoints
            // Wit this, we'll emit a recording complete UI state.
            mutableWorldMapRecordTrackUiState.emit(
                WorldMapRecordTrackUiState.RecordingComplete(savedTrackDraft)
            )
        }
    }

    companion object {
        const val MIN_POINTS_BE_RECORDED = 0

        const val ARG_UID_TRACK_CREATED = "trackUid"
        const val ARG_EXISTING_TRACK_DRAFT_ID = "trackDraftId"
    }
}