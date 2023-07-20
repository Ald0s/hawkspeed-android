package com.vljx.hawkspeed.util

import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.models.race.Race.Companion.DQ_REASON_MISSED_TRACK
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackComment
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.track.TrackPoint
import com.vljx.hawkspeed.domain.models.track.TrackPointDraft
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
        bio: String = "This is an example bio. I love street racing, so naturally I enjoy almost dying 4-9.5 times per night.",
        privilege: Int = 0,
        isBot: Boolean = false,
        isYou: Boolean = true
    ): User =
        User(
            userUid,
            userName,
            bio,
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
        startPointBearing: Float = 180f,
        isVerified: Boolean = true,
        length: Int = 7623,
        isSnappedToRoads: Boolean = true,
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
            startPointBearing,
            isVerified,
            length,
            isSnappedToRoads,
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

    fun getTrackDraftWithPoints(
        trackDraftId: Long = 1,
        trackType: TrackType = TrackType.SPRINT,
        trackName: String = "",
        trackDescription: String = "",
        pointsDraft: List<TrackPointDraft> = listOf(
            TrackPointDraft(1, 0.0, 0.0, System.currentTimeMillis(), 50f, 180f, 1),
            TrackPointDraft(2, 0.0, 0.0, System.currentTimeMillis(), 50f, 180f, 1),
            TrackPointDraft(3, 0.0, 0.0, System.currentTimeMillis(), 50f, 180f, 1)
        )
    ): TrackDraftWithPoints =
        TrackDraftWithPoints(
            trackDraftId = trackDraftId,
            trackType = trackType,
            trackName = trackName,
            trackDescription = trackDescription,
            pointDrafts = pointsDraft
        )

    fun getExampleTrackComment(
        commentUid: String = "COMMENT01",
        createdSeconds: Int = 1678508081,
        text: String = "This is a great track. There's currently a lot of potholes all over so be careful. Suggest attempting racing late at night.",
        user: User = getExampleUser(),
        trackUid: String = "TRACK01"
    ): TrackComment =
        TrackComment(
            commentUid,
            createdSeconds,
            text,
            user,
            trackUid
        )

    fun getExampleLeaderboard(

    ): List<RaceLeaderboard> =
        listOf(
            RaceLeaderboard("RACE01", finishingPlace = 1, 1672882465000, 1672882609000, 144000, 19, 0, getExampleUser(), Vehicle("VEHICLE01", "1994 Toyota Supra", true), "YARRABOULEVARD"),
            RaceLeaderboard("RACE02", finishingPlace = 2, 1673007209000, 1673007371000,162000, 19, 0, User("USER02", "user1", "This is user1's bio", 0, false, false), Vehicle("VEHICLE02", "1994 Toyota Supra", false), "YARRABOULEVARD"),
            RaceLeaderboard("RACE03", finishingPlace = 3, 1673003429000, 1673003603000,174000, 19, 0, User("USER03", "user2", "This is user2's bio", 0, false, false), Vehicle("VEHICLE03", "1994 Toyota Supra", false), "YARRABOULEVARD"),
            RaceLeaderboard("RACE04", finishingPlace = 4, 1672954990000, 1672955176000,186000, 19, 0, User("USER04", "user3", "This is user3's bio", 0, false, false), Vehicle("VEHICLE04", "1994 Toyota Supra", false), "YARRABOULEVARD")
        )

    fun getExampleRacingRace(
        raceUid: String = "RACE01",
        trackUid: String = "YARRABOULEVARD",
        started: Long = System.currentTimeMillis() - (2 * 1000),
        finished: Long? = null,
        isDisqualified: Boolean = false,
        disqualificationReason: String? = null,
        isCancelled: Boolean = false,
        averageSpeed: Int? = 70,
        numLapsComplete: Int? = null,
        percentComplete: Int? = 45
    ): Race =
        Race(raceUid, trackUid, started, finished, isDisqualified, disqualificationReason, isCancelled, averageSpeed, numLapsComplete, percentComplete)

    fun getExampleFinishedRace(
        raceUid: String = "RACE01",
        trackUid: String = "YARRABOULEVARD",
        started: Long = System.currentTimeMillis() - (4 * 1000),
        finished: Long? = System.currentTimeMillis() - 1000,
        isDisqualified: Boolean = false,
        disqualificationReason: String? = null,
        isCancelled: Boolean = false,
        averageSpeed: Int? = 120,
        numLapsComplete: Int? = null,
        percentComplete: Int? = 100
    ): Race =
        Race(raceUid, trackUid, started, finished, isDisqualified, disqualificationReason, isCancelled, averageSpeed, numLapsComplete, percentComplete)

    fun getExampleRaceLeaderboard(
        raceUid: String = "RACE01",
        finishingPlace: Int = 1,
        started: Long = System.currentTimeMillis() - (4 * 1000),
        finished: Long = System.currentTimeMillis() - 1000,
        averageSpeed: Int = 19,
        percentMissed: Int = 0,
        player: User = getExampleUser(),
        vehicle: Vehicle = getExampleVehicle(),
        trackUid: String = "YARRABOULEVARD"
    ): RaceLeaderboard =
        RaceLeaderboard(
            raceUid = raceUid,
            finishingPlace = finishingPlace,
            started = started,
            finished = finished,
            stopwatch = (finished-started).toInt(),
            averageSpeed = averageSpeed,
            percentMissed = percentMissed,
            player = player,
            vehicle = vehicle,
            trackUid = trackUid
        )

    fun getExampleCancelledRace(
        raceUid: String = "RACE01",
        trackUid: String = "YARRABOULEVARD",
        started: Long = System.currentTimeMillis() - (2 * 1000),
        finished: Long? = null,
        isDisqualified: Boolean = false,
        disqualificationReason: String? = null,
        isCancelled: Boolean = true,
        averageSpeed: Int? = 20,
        numLapsComplete: Int? = null,
        percentComplete: Int? = 2
    ): Race =
        Race(raceUid, trackUid, started, finished, isDisqualified, disqualificationReason, isCancelled, averageSpeed, numLapsComplete, percentComplete)

    fun getExampleDisqualifiedRace(
        raceUid: String = "RACE01",
        trackUid: String = "YARRABOULEVARD",
        started: Long = System.currentTimeMillis() - (2 * 1000),
        finished: Long? = null,
        isDisqualified: Boolean = true,
        disqualificationReason: String? = DQ_REASON_MISSED_TRACK,
        isCancelled: Boolean = false,
        averageSpeed: Int? = 45,
        numLapsComplete: Int? = null,
        percentComplete: Int? = 23
    ): Race =
        Race(raceUid, trackUid, started, finished, isDisqualified, disqualificationReason, isCancelled, averageSpeed, numLapsComplete, percentComplete)
}