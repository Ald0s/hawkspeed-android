package com.vljx.hawkspeed.ui.screens.authenticated.trackdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.repository.TrackRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrack
import com.vljx.hawkspeed.domain.usecase.track.GetTrackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TrackDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getTrackUseCase: GetTrackUseCase
): ViewModel() {
    /**
     * Get the Track's UID from the saved state handle.
     */
    private val mutableSelectedTrackUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_TRACK_UID]))

    /**
     * Flat map the latest selected track UID to a query for the Track's detail as a resource.
     */
    private val trackResource: Flow<Resource<Track>> =
        mutableSelectedTrackUid.flatMapLatest { trackUid ->
            getTrackUseCase(
                RequestGetTrack(trackUid)
            )
        }

    /**
     * Map the emissions from the track resource flow to the most applicable UI state.
     */
    val trackDetailUiState: StateFlow<TrackDetailUiState> =
        trackResource.map { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> TrackDetailUiState.GotTrackDetail(resource.data!!)
                Resource.Status.LOADING -> TrackDetailUiState.Loading
                Resource.Status.ERROR -> TrackDetailUiState.Failed(resource.resourceError!!)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TrackDetailUiState.Loading)

    companion object {
        const val ARG_TRACK_UID = "uidTrack"
    }
}