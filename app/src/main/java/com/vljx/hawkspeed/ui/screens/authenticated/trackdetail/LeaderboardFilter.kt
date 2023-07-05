package com.vljx.hawkspeed.ui.screens.authenticated.trackdetail

sealed class LeaderboardFilter {
    /**
     * A leaderboard filter state that will page only your attempts on this race track.
     */
    object YourAttemptsOnly: LeaderboardFilter()

    /**
     * The default leaderboard filter state that will page all items in the leaderboard.
     */
    object Default: LeaderboardFilter()
}