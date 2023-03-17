package com.vljx.hawkspeed.models.track

import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.race.RaceOutcome
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.models.ListItemViewModel

class LeaderboardEntryListViewItem(
    val finishingPlace: Int,
    val raceUid: String,
    val trackUid: String,
    val startedMilli: Long,
    val stopwatchMilli: Int,
    val player: User
): ListItemViewModel {
    constructor(finishingPlace: Int, raceOutcome: RaceOutcome):
            this(
                finishingPlace,
                raceOutcome.raceUid,
                raceOutcome.trackUid,
                raceOutcome.finished,
                raceOutcome.stopwatch,
                raceOutcome.player
            )

    override val layoutId: Int
        get() = R.layout.recycler_item_leaderboard_entry

    override val viewType: Int
        get() = 0

    override fun areViewModelsTheSame(listItemViewModel: ListItemViewModel): Boolean =
        (listItemViewModel is LeaderboardEntryListViewItem)
                && listItemViewModel.raceUid == raceUid

    override fun areViewModelContentsTheSame(listItemViewModel: ListItemViewModel): Boolean =
        (listItemViewModel is LeaderboardEntryListViewItem)
                && listItemViewModel.trackUid == trackUid
                && listItemViewModel.finishingPlace == finishingPlace
                && listItemViewModel.startedMilli == startedMilli
                && listItemViewModel.stopwatchMilli == stopwatchMilli
                && listItemViewModel.player == player
}