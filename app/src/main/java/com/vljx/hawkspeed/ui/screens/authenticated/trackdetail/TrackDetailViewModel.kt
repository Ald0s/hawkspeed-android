package com.vljx.hawkspeed.ui.screens.authenticated.trackdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.TrackComment
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flatMapLatest
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
    private val clearTrackRatingUseCase: ClearTrackRatingUseCase
): ViewModel() {
    /**
     * Get the Track's UID from the saved state handle.
     * TODO: we also have wants to view leaderboard and wants to comment arguments being passed, but we aren't doing anything with them right now.
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
        combineTransform<String, LeaderboardFilter, PagingData<RaceLeaderboard>>(mutableSelectedTrackUid, selectedLeaderboardFilter) { trackUid, filter ->
            pageTrackLeaderboardUseCase(
                RequestPageTrackLeaderboard(trackUid)
            )
        }.cachedIn(viewModelScope)

    /**
     * Flat map the latest selected track UID to a pagination of comments entries for the track.
     */
    val comments: Flow<PagingData<TrackComment>> =
        combineTransform<String, TrackCommentFilter, PagingData<TrackComment>>(mutableSelectedTrackUid, selectedTrackCommentsFilter) { trackUid, commentsFilter ->
            pageTrackCommentsUseCase(
                RequestPageTrackComments(trackUid)
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
                        trackWithPath.path!!
                    )
                }
                Resource.Status.LOADING -> TrackDetailUiState.Loading
                Resource.Status.ERROR -> TrackDetailUiState.Failed(resource.resourceError!!)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TrackDetailUiState.Loading)

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

    companion object {
        const val ARG_TRACK_UID = "trackUid"
    }
}