package com.vljx.hawkspeed.ui.dialogs.trackpreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrack
import com.vljx.hawkspeed.domain.usecase.track.GetTrackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TrackPreviewViewModel @Inject constructor(
    private val getTrackUseCase: GetTrackUseCase,
): ViewModel() {
    /**
     * A mutable shared flow for the selected track's UID. Changing this will immediately query the desired track.
     */
    private val mutableSelectedTrackUid: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * A flow for the track's resource, flat mapping the latest selected track UID.
     */
    private val trackResource: Flow<Resource<Track>> =
        mutableSelectedTrackUid.flatMapLatest { trackUid ->
            getTrackUseCase(
                RequestGetTrack(trackUid)
            )
        }

    /**
     * Map the track resource to the most applicable track preview UI state for viewing and emit the UI state as a state flow.
     */
    val trackPreviewUiState: StateFlow<TrackPreviewUiState> =
        trackResource.map { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> TrackPreviewUiState.GotTrack(resource.data!!)
                Resource.Status.LOADING -> TrackPreviewUiState.Loading
                Resource.Status.ERROR -> TrackPreviewUiState.Failed(resource.resourceError!!)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TrackPreviewUiState.Loading)

    /**
     * Set the selected track's UID. This will cause the targeted track to be queried.
     */
    fun selectTrack(trackUid: String) {
        mutableSelectedTrackUid.tryEmit(trackUid)
    }
}