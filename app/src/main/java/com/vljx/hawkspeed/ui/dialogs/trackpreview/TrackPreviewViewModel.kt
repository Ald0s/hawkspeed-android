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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
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
        getCurrentLocationUseCase(Unit).onEach {
            Timber.d("New location: $it")
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Combine the track resource flow and the device's current location. Return equivalent states for resource loading and error, determine whether Player can enter
     * race mode based on their most recent location. Finally, build a track rating state from the latest track resource. This is configured as a cold flow.
     */
    private val innerTrackPreviewUiState: Flow<TrackPreviewUiState> =
        combine(
            trackResource.distinctUntilChanged { old, new ->
                old.data?.trackUid == new.data?.trackUid
            },
            currentLocation
        ) { resource, location ->
            return@combine when(resource.status) {
                Resource.Status.SUCCESS ->
                    TrackPreviewUiState.TrackPreview(
                        resource.data!!,
                        // If location is null, return can't race, since its indeterminate. Otherwise, determine whether the player can enter race mode for this track.
                        location?.let {
                            // Now, get the track.
                            val track: Track = resource.data!!
                            // Get the distance to the start point here.
                            val distanceToStart: Float = track.distanceToStartPointFor(location.latitude, location.longitude)
                            // Determine whether orientation is correct.
                            val isOrientationCorrect: Boolean = track.isOrientationCorrectFor(location.rotation)
                            Timber.d("Checking location: distance: ${distanceToStart}m, orientation: $isOrientationCorrect")
                            when {
                                /**
                                 * When we are within 30 meters of the start line, we will allow race mode.
                                 */
                                distanceToStart <= 30f ->
                                    RaceModePromptUiState.CanEnterRaceMode(track.trackUid, location)
                                /**
                                 * Otherwise, we can't enter race mode.
                                 */
                                else -> RaceModePromptUiState.CantEnterRaceMode
                            }
                        } ?: RaceModePromptUiState.CantEnterRaceMode,
                        // Transform the track into a track rating state here.
                        resource.data!!.let { track ->
                            TrackRatingUiState.GotTrackRating(
                                track.trackUid,
                                track.numPositiveVotes,
                                track.numNegativeVotes,
                                track.yourRating
                            )
                        }
                    )

                /**
                 * When loading, simply return the equivalent loading state for preview.
                 */
                Resource.Status.LOADING ->
                    TrackPreviewUiState.Loading

                /**
                 * If resource failed, return the equivalent error state for preview.
                 */
                Resource.Status.ERROR ->
                    TrackPreviewUiState.Failed(
                        resource.resourceError!!
                    )
            }
        }

    /**
     * Publicise the track preview UI state and reconfigure it as a state flow.
     */
    val trackPreviewUiState: StateFlow<TrackPreviewUiState> =
        innerTrackPreviewUiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TrackPreviewUiState.Loading)

    /**
     * Set the selected track's UID. This will cause the targeted track to be queried.
     */
    fun selectTrack(trackUid: String) {
        Timber.d("Selecting track: $trackUid")
        mutableSelectedTrackUid.tryEmit(trackUid)
    }

    /**
     * Called when the upvote button is clicked.
     */
    fun upvoteTrack(trackUid: String) {
        // Run the upvote use case on view model scope. We shouldn't have to do anything with the result, since latest should come from cache.
        viewModelScope.launch {
            upvoteTrackUseCase(trackUid)
        }
    }

    /**
     * Called when the downvote button is clicked.
     */
    fun downvoteTrack(trackUid: String) {
        // Run the downvote use case on view model scope. We shouldn't have to do anything with the result, since latest should come from cache.
        viewModelScope.launch {
            downvoteTrackUseCase(trackUid)
        }
    }

    /**
     * Called when an already-selected rating state is clicked again. This will clear the rating.
     */
    fun clearTrackRating(trackUid: String) {
        // Run the clear rating use case on view model scope. We shouldn't have to do anything with the result, since latest should come from cache.
        viewModelScope.launch {
            clearTrackRatingUseCase(trackUid)
        }
    }
}