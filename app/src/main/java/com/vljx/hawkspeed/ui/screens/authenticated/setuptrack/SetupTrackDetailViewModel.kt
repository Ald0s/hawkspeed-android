package com.vljx.hawkspeed.ui.screens.authenticated.setuptrack

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.usecase.track.draft.GetTrackDraftUseCase
import com.vljx.hawkspeed.ui.component.InputValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SetupTrackDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getTrackDraftUseCase: GetTrackDraftUseCase
): ViewModel() {
    /**
     * A mutable state flow for the selected track draft's Id.
     */
    private val mutableSelectedTrackDraftId: MutableStateFlow<Long> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_TRACK_DRAFT_ID]))

    /**
     * A mutable shared flow, representing the current UI state for the setup track detail screen.
     */
    private val mutableSetupTrackDetailUiState: MutableSharedFlow<SetupTrackDetailUiState> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    /**
     * Setup the arguments we require for new tracks.
     */
    private val mutableTrackName: MutableStateFlow<String?> = MutableStateFlow(null)
    private val mutableTrackDescription: MutableStateFlow<String?> = MutableStateFlow(null)
    private val mutableTrackType: MutableStateFlow<Int?> = MutableStateFlow(null)

    /**
     * Publicise all arguments.
     */
    val trackName: StateFlow<String?> =
        mutableTrackName

    val trackDescription: StateFlow<String?> =
        mutableTrackDescription

    val trackType: StateFlow<Int?> =
        mutableTrackType

    /**
     * A validator result for the track name.
     */
    val validateTrackNameResult: StateFlow<InputValidationResult> =
        mutableTrackName.map { name ->
            // TODO: complete this validator. For now, will just return valid.
            InputValidationResult(true)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), InputValidationResult(false))

    /**
     * A validator result for the track description.
     */
    val validateTrackDescriptionResult: StateFlow<InputValidationResult> =
        mutableTrackDescription.map { description ->
            // TODO: complete this validator. For now, will just return valid.
            InputValidationResult(true)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), InputValidationResult(false))

    /**
     * A validator result for the track type.
     */
    val validateTrackTypeResult: StateFlow<InputValidationResult> =
        mutableTrackType.map { description ->
            // TODO: complete this validator. For now, will just return valid.
            InputValidationResult(true)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), InputValidationResult(false))

    /**
     * Flat map the latest selected track draft Id to the associated track draft, with all its points, from cache.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val selectedTrackDraft: Flow<TrackDraftWithPoints?> =
        mutableSelectedTrackDraftId.flatMapLatest { trackDraftId ->
            getTrackDraftUseCase(
                trackDraftId
            )
        }

    /**
     * A combination of the validators for track name, description and the selected track draft; that will control the
     * submit button's enabled status.
     */
    val canAttemptCreateTrack: StateFlow<Boolean> =
        combine(
            validateTrackNameResult,
            validateTrackDescriptionResult,
            validateTrackTypeResult,
            selectedTrackDraft
        ) { nameValid, descValid, trackType, trackDraft ->
            nameValid.isValid && descValid.isValid && trackType.isValid && trackDraft != null
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    /**
     * Publicise the UI state flow.
     */
    val setupTrackDetailUiState: SharedFlow<SetupTrackDetailUiState> =
        mutableSetupTrackDetailUiState

    /**
     * Perform a submission in an attempt to create this track.
     */
    fun createTrack() {
        // TODO: get all required arguments.
        // TODO: create a request for a new track, mapping arguments accordingly.
        // TODO: call emitAll on UI shared flow, on result of SubmitTrackUseCase, mapping result to appropriate UI state, if UI state is success, use deleteTrackDraftUseCase
        // TODO: to delete the track draft in question.
        throw NotImplementedError()
    }

    companion object {
        const val ARG_TRACK_DRAFT_ID = "trackDraftId"
    }
}