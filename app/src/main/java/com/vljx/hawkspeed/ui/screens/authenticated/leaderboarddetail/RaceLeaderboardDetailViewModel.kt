package com.vljx.hawkspeed.ui.screens.authenticated.leaderboarddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.usecase.race.GetLeaderboardEntryForRaceUseCase
import com.vljx.hawkspeed.domain.usecase.track.GetTrackWithPathUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class RaceLeaderboardDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getLeaderboardEntryForRaceUseCase: GetLeaderboardEntryForRaceUseCase,
    private val getTrackWithPathUseCase: GetTrackWithPathUseCase
): ViewModel() {
    /**
     * A mutable state flow for the selected race's UID.
     */
    private val mutableSelectedRaceUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_RACE_UID]))

    /**
     * A mutable state flow for the selected race's track's UID.
     */
    private val mutableSelectedTrackUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_TRACK_UID]))

    /**
     * Flat map the latest selected race UID to a query for the leaderboard entry.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val latestLeaderboardEntryResource: Flow<Resource<RaceLeaderboard>> =
        mutableSelectedRaceUid.flatMapLatest { raceUid ->
            getLeaderboardEntryForRaceUseCase(
                RequestGetRace(raceUid)
            )
        }

    /**
     * Flat map the latest selected track UID to a query for the track with its path.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val trackWithPathResource: Flow<Resource<TrackWithPath>> =
        mutableSelectedTrackUid.flatMapLatest { trackUid ->
            getTrackWithPathUseCase(
                RequestGetTrackWithPath(trackUid)
            )
        }

    /**
     * Map emissions from both the leaderboard entry and track with path resource flows, and from this generate UI states. If either are loading, we will emit
     * the loading state. If either fail, emit the load failed state with the resource error.
     */
    val raceLeaderboardDetailUiState: StateFlow<RaceLeaderboardDetailUiState> =
        combine(
            latestLeaderboardEntryResource,
            trackWithPathResource
        ) { leaderboardEntryResource, trackResource ->
            when {
                /**
                 * If either resource fails, send the applicable resource error out in a failed UI state.
                 */
                leaderboardEntryResource.status == Resource.Status.ERROR ->
                    RaceLeaderboardDetailUiState.RaceLeaderboardLoadFailed(leaderboardEntryResource.resourceError!!)
                trackResource.status == Resource.Status.ERROR ->
                    RaceLeaderboardDetailUiState.RaceLeaderboardLoadFailed(trackResource.resourceError!!)

                /**
                 * If either resource is loading, always emit the common loading state.
                 */
                leaderboardEntryResource.status == Resource.Status.LOADING || trackResource.status == Resource.Status.LOADING ->
                    RaceLeaderboardDetailUiState.Loading

                /**
                 * Otherwise, both presumed successful; emit the success state with track, its path and the leaderboard entry.
                 */
                else -> RaceLeaderboardDetailUiState.RaceTrackLeaderboardDetail(
                    trackResource.data!!.track,
                    trackResource.data!!.path!!,
                    leaderboardEntryResource.data!!
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RaceLeaderboardDetailUiState.Loading)

    companion object {
        const val ARG_RACE_UID = "raceUid"
        const val ARG_TRACK_UID = "trackUid"
    }
}