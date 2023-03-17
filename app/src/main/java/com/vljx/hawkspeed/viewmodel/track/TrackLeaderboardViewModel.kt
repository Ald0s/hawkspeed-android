package com.vljx.hawkspeed.viewmodel.track

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import androidx.paging.map
import com.vljx.hawkspeed.domain.interactor.track.PageLeaderboardUseCase
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.requests.track.PageLeaderboardRequest
import com.vljx.hawkspeed.models.ListItemViewModel
import com.vljx.hawkspeed.models.track.LeaderboardEntryListViewItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TrackLeaderboardViewModel @Inject constructor(
    private val pageLeaderboardUseCase: PageLeaderboardUseCase
): ViewModel() {
    /**
     * A shared flow for the currently selected track.
     */
    private val mutableSelectedTrack: MutableSharedFlow<Track> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * A flow for paging the leaderboard entries for this track, as list item view models.
     */
    val leaderboard: Flow<PagingData<ListItemViewModel>> =
        mutableSelectedTrack.flatMapLatest { track ->
            pageLeaderboardUseCase(
                PageLeaderboardRequest(track.trackUid)
            )
        }.map { raceOutcomePagingData ->
            // TODO: is there a better way to calculate the finishing place for this race outcome?
            var index = 0
            raceOutcomePagingData.map {
                LeaderboardEntryListViewItem(index++, it)
            }
        }

    /**
     * Select the track.
     */
    fun selectTrack(track: Track) {
        mutableSelectedTrack.tryEmit(track)
    }
}