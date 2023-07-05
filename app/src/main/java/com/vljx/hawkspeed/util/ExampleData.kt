package com.vljx.hawkspeed.util

import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.track.TrackPoint
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle

/**
 * TODO: exclude this entire file from production code.
 */
object ExampleData {
    fun getExampleAccount(
        userUid: String = "USER01",
        emailAddress: String = "aldos@mail.com",
        userName: String? = "aldos",
        isAccountVerified: Boolean = true,
        isPasswordVerified: Boolean = true,
        isProfileSetup: Boolean = userName != null,
        canCreateTracks: Boolean = true,
    ): Account =
        Account(
            userUid = userUid,
            emailAddress = emailAddress,
            userName = userName,
            isAccountVerified = isAccountVerified,
            isPasswordVerified = isPasswordVerified,
            isProfileSetup = isProfileSetup,
            canCreateTracks = canCreateTracks
        )

    fun getExampleUser(
        userUid: String = "USER01",
        userName: String = "aldos",
        privilege: Int = 0,
        isBot: Boolean = false,
        isYou: Boolean = true
    ): User =
        User(
            userUid,
            userName,
            privilege,
            isBot,
            isYou
        )

    fun getExampleVehicle(
        vehicleUid: String = "VEHICLE01",
        text: String = "1994 Toyota Supra",
        belongsToYou: Boolean = true
    ): Vehicle =
        Vehicle(
            vehicleUid,
            text,
            belongsToYou
        )

    fun getExampleTrack(
        trackUid: String = "YARRABOULEVARD",
        name: String = "Yarra Boulevard",
        description: String = "A nice race track.",
        user: User = getExampleUser(),
        topLeaderboard: List<RaceLeaderboard> = getExampleLeaderboard(),
        trackPoint: TrackPoint = TrackPoint(
            -37.79217229732622,
            145.02083480358127,
            trackUid
        ),
        isVerified: Boolean = true,
        numPositiveVotes: Int = 6,
        numNegativeVotes: Int = 2,
        yourRating: Boolean? = null,
        numComments: Int = 3,
        canRace: Boolean = true,
        canEdit: Boolean = true,
        canDelete: Boolean = true,
        canComment: Boolean = true
    ): Track =
        Track(
            trackUid,
            name,
            description,
            getExampleUser(),
            topLeaderboard,
            trackPoint,
            isVerified,
            TrackType.SPRINT,
            numPositiveVotes,
            numNegativeVotes,
            yourRating,
            numComments,
            canRace,
            canEdit,
            canDelete,
            canComment
        )

    fun getExampleTrackPath(
        trackPathUid: String = "YARRABOULEVARD",
        points: List<TrackPoint> = listOf()
    ): TrackPath =
        TrackPath(
            trackPathUid = trackPathUid,
            points = points
        )

    fun getExampleLeaderboard(

    ): List<RaceLeaderboard> =
        listOf(
            RaceLeaderboard("RACE01", finishingPlace = 1, 1000, 100, 26450, User("USER01", "aldos", 0, false, true), Vehicle("VEHICLE01", "1994 Toyota Supra", true), "YARRABOULEVARD"),
            RaceLeaderboard("RACE01", finishingPlace = 2, 1000, 100, 54210, User("USER02", "user1", 0, false, false), Vehicle("VEHICLE02", "1994 Toyota Supra", false), "YARRABOULEVARD"),
            RaceLeaderboard("RACE01", finishingPlace = 3, 1000, 100, 125134, User("USER03", "user2", 0, false, false), Vehicle("VEHICLE03", "1994 Toyota Supra", false), "YARRABOULEVARD")
        )

    fun getExampleRacingRace(
        raceUid: String = "RACE01",
        trackUid: String = "YARRABOULEVARD",
        started: Long = System.currentTimeMillis() - (2 * 1000),
        finished: Long? = null,
        isDisqualified: Boolean = false,
        disqualificationReason: String? = null,
        isCancelled: Boolean = false
    ): Race =
        Race(raceUid, trackUid, started, finished, isDisqualified, disqualificationReason, isCancelled)

    fun getExampleFinishedRace(
        raceUid: String = "RACE01",
        trackUid: String = "YARRABOULEVARD",
        started: Long = System.currentTimeMillis() - (4 * 1000),
        finished: Long? = System.currentTimeMillis() - 1000,
        isDisqualified: Boolean = false,
        disqualificationReason: String? = null,
        isCancelled: Boolean = false
    ): Race =
        Race(raceUid, trackUid, started, finished, isDisqualified, disqualificationReason, isCancelled)

    fun getExampleCancelledRace(
        raceUid: String = "RACE01",
        trackUid: String = "YARRABOULEVARD",
        started: Long = System.currentTimeMillis() - (2 * 1000),
        finished: Long? = null,
        isDisqualified: Boolean = false,
        disqualificationReason: String? = null,
        isCancelled: Boolean = true
    ): Race =
        Race(raceUid, trackUid, started, finished, isDisqualified, disqualificationReason, isCancelled)

    fun getExampleDisqualifiedRace(
        raceUid: String = "RACE01",
        trackUid: String = "YARRABOULEVARD",
        started: Long = System.currentTimeMillis() - (2 * 1000),
        finished: Long? = null,
        isDisqualified: Boolean = true,
        disqualificationReason: String? = "left-track",
        isCancelled: Boolean = false
    ): Race =
        Race(raceUid, trackUid, started, finished, isDisqualified, disqualificationReason, isCancelled)
}