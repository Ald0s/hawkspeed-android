package com.vljx.hawkspeed.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.race.RaceLeaderboardEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RaceLeaderboardDao: BaseDao<RaceLeaderboardEntity>() {
    @Query("""
        SELECT *
        FROM race_leaderboard
        
        INNER JOIN track
        ON track.trackUid = race_leaderboard.trackUid
        
        WHERE track.trackUid = :trackUid
        ORDER BY race_leaderboard.stopwatch ASC
    """)
    abstract fun pageRaceLeaderboardFromTrack(trackUid: String): PagingSource<Int, RaceLeaderboardEntity>

    @Query("""
        SELECT *
        FROM race_leaderboard
        WHERE raceUid = :raceUid
    """)
    abstract fun selectLeaderboardEntryForRace(raceUid: String): Flow<RaceLeaderboardEntity?>

    @Query("""
        DELETE FROM race_leaderboard
        WHERE race_leaderboard.trackUid = :trackUid
    """)
    abstract suspend fun clearLeaderboardFor(trackUid: String)
}