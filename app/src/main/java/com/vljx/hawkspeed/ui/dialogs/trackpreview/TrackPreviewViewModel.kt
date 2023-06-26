package com.vljx.hawkspeed.ui.dialogs.trackpreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrack
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationUseCase
import com.vljx.hawkspeed.domain.usecase.track.ClearTrackRatingUseCase
import com.vljx.hawkspeed.domain.usecase.track.DownvoteTrackUseCase
import com.vljx.hawkspeed.domain.usecase.track.GetTrackLatestCommentsUseCase
import com.vljx.hawkspeed.domain.usecase.track.GetTrackUseCase
import com.vljx.hawkspeed.domain.usecase.track.UpvoteTrackUseCase
import com.vljx.hawkspeed.ui.screens.authenticated.trackdetail.TrackRatingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TrackPreviewViewModel @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getTrackUseCase: GetTrackUseCase,
    private val upvoteTrackUseCase: UpvoteTrackUseCase,
    private val downvoteTrackUseCase: DownvoteTrackUseCase,
    private val clearTrackRatingUseCase: ClearTrackRatingUseCase,
    private val getTrackLatestCommentsUseCase: GetTrackLatestCommentsUseCase
): ViewModel() {
    /**
     * A mutable shared flow for the selected track's UID. Changing this will immediately query the desired track.
     */
    private val mutableSelectedTrackUid: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * A flow for the track's resource, flat mapping the latest selected track UID. We'll share this in the view model scope, but we want to map this
     * multiple times and want to avoid duplicate calls to get track.
     */
    private val trackResource: SharedFlow<Resource<Track>> =
        mutableSelectedTrackUid.flatMapLatest { trackUid ->
            getTrackUseCase(
                RequestGetTrack(trackUid)
            )
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    /**
     * Get the current location as reported by our world socket state.
     */
    private val currentLocation: StateFlow<PlayerPosition?> =
        getCurrentLocationUseCase(Unit)

    /**
     * Combine flows of track resource and current location to ultimately retrieve a race prompt ui state. This will show that the User can race only
     * when, firstly, the track resource is in a state where it can provide information on the track, then, the player's location must be such that
     * they are close enough to, and aligned with, the start point.
     */
    val racePromptUiState: StateFlow<RacePromptUiState> =
        combine(
            trackResource,
            currentLocation
        ) { resource, location ->
            // If resource is not success, data is null or location is null, just return cant race.
            if(resource.status != Resource.Status.SUCCESS || resource.data == null || location == null) {
                return@combine RacePromptUiState.CantRace
            }
            // Now, we can get the track from resource, since we know it is not null.
            val track: Track = resource.data!!
            // Now, we know location is not null. We can question track as to whether it can be raced.
            if(track.canBeRacedBy(location.latitude, location.longitude, location.rotation)) {
                // We can race!
                return@combine RacePromptUiState.CanRace(track.trackUid, location)
            }
            // Can't race.
            return@combine RacePromptUiState.CantRace
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RacePromptUiState.CantRace)

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
     * Map the track preview UI state, to determine the current state of the rating UI. If the track preview UI's state is anything but a success
     * state, this should emit the loading state.
     */
    val trackRatingUiState: StateFlow<TrackRatingUiState> =
        trackPreviewUiState.map { uiState ->
            when(uiState) {
                is TrackPreviewUiState.GotTrack -> {
                    val track: Track = (uiState as TrackPreviewUiState.GotTrack).track
                    TrackRatingUiState.GotTrackRating(
                        track.trackUid,
                        track.numPositiveVotes,
                        track.numNegativeVotes,
                        track.yourRating
                    )
                }
                else -> TrackRatingUiState.Loading
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TrackRatingUiState.Loading)

    /**
     * Set the selected track's UID. This will cause the targeted track to be queried.
     */
    fun selectTrack(trackUid: String) {
        mutableSelectedTrackUid.tryEmit(trackUid)
    }

    /**
     * Called when the upvote button is clicked.
     */
    fun upvoteTrack(trackUid: String) {

    }

    /**
     * Called when the downvote button is clicked.
     */
    fun downvoteTrack(trackUid: String) {

    }

    /**
     * Called when an already-selected rating state is clicked again. This will clear the rating.
     */
    fun clearTrackRating(trackUid: String) {

    }
}