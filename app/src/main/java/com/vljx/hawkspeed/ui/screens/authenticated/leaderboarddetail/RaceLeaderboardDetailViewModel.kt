package com.vljx.hawkspeed.ui.screens.authenticated.leaderboarddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.usecase.race.GetLeaderboardEntryForRaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RaceLeaderboardDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getLeaderboardEntryForRaceUseCase: GetLeaderboardEntryForRaceUseCase
): ViewModel() {
    /**
     * A mutable state flow for the selected race's UID.
     */
    private val mutableSelectedRaceUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_RACE_UID]))

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
     * Map emissions from the resource to the most appropriate leaderboard detail UI state.
     */
    val raceLeaderboardDetailUiState: StateFlow<RaceLeaderboardDetailUiState> =
        latestLeaderboardEntryResource.map { resource ->
            when(resource.status) {
                Resource.Status.SUCCESS ->
                    RaceLeaderboardDetailUiState.GotRaceLeaderboardDetail(resource.data!!)
                Resource.Status.LOADING ->
                    RaceLeaderboardDetailUiState.Loading
                Resource.Status.ERROR ->
                    RaceLeaderboardDetailUiState.RaceLeaderboardLoadFailed(resource.resourceError!!)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RaceLeaderboardDetailUiState.Loading)

    companion object {
        const val ARG_RACE_UID = "raceUid"
    }
}