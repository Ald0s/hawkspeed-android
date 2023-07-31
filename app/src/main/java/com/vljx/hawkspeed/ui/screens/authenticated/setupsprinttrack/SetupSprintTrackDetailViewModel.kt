package com.vljx.hawkspeed.ui.screens.authenticated.setupsprinttrack

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack
import com.vljx.hawkspeed.domain.usecase.track.SubmitTrackUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.DeleteTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.GetTrackDraftUseCase
import com.vljx.hawkspeed.domain.usecase.track.draft.SaveTrackDraftUseCase
import com.vljx.hawkspeed.ui.component.InputValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupSprintTrackDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getTrackDraftUseCase: GetTrackDraftUseCase,
    private val submitTrackUseCase: SubmitTrackUseCase,
    private val saveTrackDraftUseCase: SaveTrackDraftUseCase,
    private val deleteTrackDraftUseCase: DeleteTrackDraftUseCase,

    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
): ViewModel() {
    /**
     * A mutable state flow for the selected track draft's Id.
     */
    private val mutableSelectedTrackDraftId: MutableStateFlow<Long> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_TRACK_DRAFT_ID]))

    /**
     * A mutable shared flow, representing the current UI state for the setup track detail screen.
     */
    private val mutableSetupSprintTrackDetailUiState: MutableSharedFlow<SetupSprintTrackDetailUiState> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    /**
     * Setup the arguments we require for new tracks.
     */
    private val mutableTrackName: MutableStateFlow<String?> = MutableStateFlow(null)
    private val mutableTrackDescription: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * Publicise both argument flows for UI updates.
     */
    val trackNameState: StateFlow<String?> = mutableTrackName
    val trackDescriptionState: StateFlow<String?> = mutableTrackDescription

    /**
     * A validator result for the track name.
     */
    private val validateTrackNameResult: StateFlow<InputValidationResult> =
        mutableTrackName.map { name ->
            // TODO: complete this validator. For now, will just return valid.
            InputValidationResult(true)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InputValidationResult(false))

    /**
     * A validator result for the track description.
     */
    private val validateTrackDescriptionResult: StateFlow<InputValidationResult> =
        mutableTrackDescription.map { description ->
            // TODO: complete this validator. For now, will just return valid.
            InputValidationResult(true)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InputValidationResult(false))

    /**
     * Flat map the latest selected track draft Id to the associated track draft, with all its points, from cache.
     * We'll also configure this as a hot flow, so we can get its current value.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val selectedTrackDraft: StateFlow<TrackDraftWithPoints?> =
        mutableSelectedTrackDraftId.flatMapLatest { trackDraftId ->
            getTrackDraftUseCase(
                trackDraftId
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * A combination of the validators for track name, description and the selected track draft; that will control the
     * submit button's enabled status.
     */
    private val canAttemptCreateTrack: StateFlow<Boolean> =
        combine(
            validateTrackNameResult,
            validateTrackDescriptionResult,
            selectedTrackDraft
        ) { nameValid, descValid, trackDraft ->
            nameValid.isValid && descValid.isValid && trackDraft != null
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Combine the latest track name, description and whether we can attempt to create the track being completed into a UI state for the form in
     * its current position.
     */
    private val setupSprintTrackDetailFormUiState: SharedFlow<SetupSprintTrackDetailFormUiState> =
        combine(
            validateTrackNameResult,
            validateTrackDescriptionResult,
            canAttemptCreateTrack
        ) { name, description, canAttempt ->
            SetupSprintTrackDetailFormUiState.SprintTrackDetailForm(
                name,
                description,
                canAttempt
            )
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    /**
     * Combine the latest state of the detail's form with the track draft currently selected. Be advised, upon track creation, the selected track draft
     * will be deleted from cache, which will trigger this flow to once again emit the Loading state. This isn't a big deal since the track created state
     * will cause the detail form to exit anyway.
     */
    private val innerSetupSprintTrackDetailUiState: Flow<SetupSprintTrackDetailUiState> =
        combine(
            setupSprintTrackDetailFormUiState,
            selectedTrackDraft
        ) { formUiState, trackDraftWithPoints ->
            when (trackDraftWithPoints) {
                null ->
                    SetupSprintTrackDetailUiState.Loading
                else -> SetupSprintTrackDetailUiState.ShowSprintDetailForm(
                    trackDraftWithPoints,
                    formUiState
                )
            }
        }

    /**
     * Publicise the track detail's UI state.
     */
    val setupSprintTrackDetailUiState: StateFlow<SetupSprintTrackDetailUiState> =
        merge(
            innerSetupSprintTrackDetailUiState,
            mutableSetupSprintTrackDetailUiState
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SetupSprintTrackDetailUiState.Loading)

    /**
     * Perform a submission in an attempt to create this track.
     */
    fun createTrack() {
        // If we can't attempt to create the track, throw an illegal state exception.
        if(!canAttemptCreateTrack.value) {
            throw IllegalStateException()
        }
        viewModelScope.launch(ioDispatcher) {
            // Get all required arguments.
            val trackName: String = mutableTrackName.value
                ?: throw NotImplementedError()
            val trackDescription: String = mutableTrackDescription.value
                ?: throw NotImplementedError()
            val selectedTrackDraft: TrackDraftWithPoints = selectedTrackDraft.value
                ?: throw NotImplementedError()
            // Now, create a new request for a track.
            val requestSubmitTrack = RequestSubmitTrack(
                trackName,
                trackDescription,
                selectedTrackDraft
            )
            // Now, collect a flow for the submit track use case; mapping it to a setup track detail UI state, but inside a call to emit all, with the target
            // being the manual UI state.
            mutableSetupSprintTrackDetailUiState.emitAll(
                submitTrackUseCase(requestSubmitTrack)
                    .flowOn(ioDispatcher)
                    .map { trackResource ->
                        when (trackResource.status) {
                            Resource.Status.SUCCESS -> {
                                // Successfully created the track on the server. Delete the draft.
                                deleteTrackDraftUseCase(selectedTrackDraft.trackDraftId)
                                // Return the created track with optional path.
                                SetupSprintTrackDetailUiState.SprintTrackCreated(trackResource.data!!)
                            }
                            Resource.Status.LOADING -> SetupSprintTrackDetailUiState.ShowSprintDetailForm(
                                selectedTrackDraft,
                                SetupSprintTrackDetailFormUiState.Submitting
                            )
                            Resource.Status.ERROR -> SetupSprintTrackDetailUiState.ShowSprintDetailForm(
                                selectedTrackDraft,
                                SetupSprintTrackDetailFormUiState.ServerRefused(
                                    trackResource.resourceError!!
                                )
                            )
                        }
                    }
            )
        }
    }

    /**
     * Update the desired track name.
     */
    fun updateTrackName(name: String) {
        mutableTrackName.tryEmit(name)
    }

    /**
     * Update the desired track description.
     */
    fun updateTrackDescription(description: String) {
        mutableTrackDescription.tryEmit(description)
    }

    companion object {
        const val ARG_TRACK_DRAFT_ID = "trackDraftId"
    }
}