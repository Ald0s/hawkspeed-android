package com.vljx.hawkspeed.ui.screens.authenticated.trackdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.TrackComment
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackComments
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard
import com.vljx.hawkspeed.domain.usecase.track.ClearTrackRatingUseCase
import com.vljx.hawkspeed.domain.usecase.track.DownvoteTrackUseCase
import com.vljx.hawkspeed.domain.usecase.track.GetTrackWithPathUseCase
import com.vljx.hawkspeed.domain.usecase.track.PageTrackCommentsUseCase
import com.vljx.hawkspeed.domain.usecase.track.PageTrackLeaderboardUseCase
import com.vljx.hawkspeed.domain.usecase.track.UpvoteTrackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getTrackWithPathUseCase: GetTrackWithPathUseCase,
    private val pageTrackCommentsUseCase: PageTrackCommentsUseCase,
    private val pageTrackLeaderboardUseCase: PageTrackLeaderboardUseCase,
    private val upvoteTrackUseCase: UpvoteTrackUseCase,
    private val downvoteTrackUseCase: DownvoteTrackUseCase,
    private val clearTrackRatingUseCase: ClearTrackRatingUseCase,

    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
): ViewModel() {
    /**
     * Get the Track's UID from the saved state handle.
     */
    private val mutableSelectedTrackUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_TRACK_UID]))

    /**
     * A mutable state flow for filtering the leaderboard entries being paged.
     */
    private val selectedLeaderboardFilter: MutableStateFlow<LeaderboardFilter> = MutableStateFlow(LeaderboardFilter.Default)

    /**
     * A mutable state flow for filtering the comments being paged.
     */
    private val selectedTrackCommentsFilter: MutableStateFlow<TrackCommentFilter> = MutableStateFlow(TrackCommentFilter.Default)

    /**
     * Flat map the latest selected track UID to a query for the Track, with its path, as a resource.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val trackResource: Flow<Resource<TrackWithPath>> =
        mutableSelectedTrackUid.flatMapLatest { trackUid ->
            getTrackWithPathUseCase(
                RequestGetTrackWithPath(trackUid)
            )
        }

    /**
     * Flat map the latest selected track UID to a pagination of leaderboard entries for the track.
     */
    val leaderboard: Flow<PagingData<RaceLeaderboard>> =
        combineTransform<String, LeaderboardFilter, PagingData<RaceLeaderboard>>(
            mutableSelectedTrackUid,
            selectedLeaderboardFilter
        ) { trackUid, filter ->
            emitAll(
                pageTrackLeaderboardUseCase(
                    RequestPageTrackLeaderboard(trackUid)
                )
            )
        }.cachedIn(viewModelScope)

    /**
     * Flat map the latest selected track UID to a pagination of comments entries for the track.
     */
    val comments: Flow<PagingData<TrackComment>> =
        combineTransform(
            mutableSelectedTrackUid,
            selectedTrackCommentsFilter
        ) { trackUid, commentsFilter ->
            emitAll(
                pageTrackCommentsUseCase(
                    RequestPageTrackComments(trackUid)
                )
            )
        }.cachedIn(viewModelScope)

    /**
     * Map the emissions from the track resource flow to the most applicable UI state.
     */
    val trackDetailUiState: StateFlow<TrackDetailUiState> =
        trackResource.map { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS -> {
                    val trackWithPath = resource.data!!
                    if(trackWithPath.path == null) {
                        // TODO: while getting track and its path, no path was returned. This is not allowed and also not implemented.
                        throw NotImplementedError("Getting track detail failed because the desired track has no path. This is not implemented.")
                    }
                    TrackDetailUiState.GotTrackDetail(
                        trackWithPath.track,
                        trackWithPath.path!!,
                        TrackRatingUiState.GotTrackRating(
                            trackWithPath.track.trackUid,
                            trackWithPath.track.numPositiveVotes,
                            trackWithPath.track.numNegativeVotes,
                            trackWithPath.track.yourRating
                        )
                    )
                }
                Resource.Status.LOADING -> TrackDetailUiState.Loading
                Resource.Status.ERROR -> TrackDetailUiState.Failed(resource.resourceError!!)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TrackDetailUiState.Loading)

    /**
     * Called when the upvote button is clicked.
     */
    fun upvoteTrack(track: Track) {
        // Run the upvote use case on view model scope. We shouldn't have to do anything with the result, since latest should come from cache.
        viewModelScope.launch(ioDispatcher) {
            // If track is already upvoted, clear track rating instead.
            if(track.yourRating == true) {
                clearTrackRatingUseCase(track.trackUid)
            } else {
                // Otherwise upvote.
                upvoteTrackUseCase(track.trackUid)
            }
        }
    }

    /**
     * Called when the downvote button is clicked.
     */
    fun downvoteTrack(track: Track) {
        // Run the downvote use case on view model scope. We shouldn't have to do anything with the result, since latest should come from cache.
        viewModelScope.launch(ioDispatcher) {
            // If track is already downvoted, clear track rating instead.
            if(track.yourRating == false) {
                clearTrackRatingUseCase(track.trackUid)
            } else {
                // Otherwise downvote.
                downvoteTrackUseCase(track.trackUid)
            }
        }
    }

    companion object {
        const val ARG_TRACK_UID = "trackUid"
    }
}