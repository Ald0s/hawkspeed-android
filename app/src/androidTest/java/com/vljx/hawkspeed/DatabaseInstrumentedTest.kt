package com.vljx.hawkspeed

import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.di.module.DataModule
import com.vljx.hawkspeed.data.di.module.DatabaseModule
import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.models.track.TrackPathModel
import com.vljx.hawkspeed.data.models.track.TrackPathWithPointsModel
import com.vljx.hawkspeed.data.models.track.TrackPointModel
import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.data.source.track.TrackLocalData
import com.vljx.hawkspeed.data.source.track.TrackPathLocalData
import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.world.RequestGetWorldObjects
import com.vljx.hawkspeed.domain.usecase.world.GetWorldObjectsUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.Assert.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@UninstallModules(DatabaseModule::class, DataModule::class)
@HiltAndroidTest
class DatabaseInstrumentedTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var trackLocalData: TrackLocalData

    @Inject
    lateinit var trackPathLocalData: TrackPathLocalData

    @Inject
    lateinit var getWorldObjectsUseCase: GetWorldObjectsUseCase

    @Before
    fun setUp() {
        hiltRule.inject()
        appDatabase.clearAllTables()
    }

    @After
    fun tearDown() {
        appDatabase.clearAllTables()
    }

    suspend fun insertTrackAndPath() {
        // Create a track model.
        val trackModel: TrackModel = TrackModel(
            "track01",
            "Track",
            "Track DEsc",
            UserModel("US", "aldos", 0, true, true),
            listOf(),
            TrackPointModel(1.0, 1.0, "track01"),
            true, TrackType.SPRINT, 0, 0, true, 0, true, true, true, true
        )
        // Now, create 10 points, all belonging to the same track, and a trackpath model from that.
        val trackPathModel: TrackPathModel = TrackPathModel(
            "track01",
            listOf(
                TrackPointModel(1.0, 1.0, "track01"),
                TrackPointModel(2.0, 1.0, "track01"),
                TrackPointModel(3.0, 1.0, "track01"),
                TrackPointModel(4.0, 1.0, "track01"),
                TrackPointModel(5.0, 1.0, "track01"),
                TrackPointModel(6.0, 1.0, "track01"),
                TrackPointModel(7.0, 1.0, "track01"),
                TrackPointModel(8.0, 1.0, "track01"),
                TrackPointModel(9.0, 1.0, "track01"),
                TrackPointModel(10.0, 1.0, "track01")
            )
        )
        // Upsert the track & the points.
        trackPathLocalData.upsertTrackWithPath(
            TrackWithPathModel(
                trackModel,
                TrackPathWithPointsModel(
                    trackPathModel.trackUid,
                    trackPathModel.points
                )
            )
        )
    }

    suspend fun insertTracksAndPaths() {
        // Create 3 track models.
        val track01Model: TrackModel = TrackModel(
            "track01",
            "Track 01",
            "Track DEsc",
            UserModel("US", "aldos", 0, true, true),
            listOf(),
            TrackPointModel(1.0, 1.0, "track01"),
            true, TrackType.SPRINT, 0, 0, true, 0, true, true, true, true
        )
        val track02Model: TrackModel = TrackModel(
            "track02",
            "Track 02",
            "Track DEsc",
            UserModel("US", "aldos", 0, true, true),
            listOf(),
            TrackPointModel(1.0, 1.0, "track01"),
            true, TrackType.SPRINT, 0, 0, true, 0, true, true, true, true
        )
        val track03Model: TrackModel = TrackModel(
            "track03",
            "Track 03",
            "Track DEsc",
            UserModel("US", "aldos", 0, true, true),
            listOf(),
            TrackPointModel(1.0, 1.0, "track01"),
            true, TrackType.SPRINT, 0, 0, true, 0, true, true, true, true
        )
        // Now, create 10 points belonging to track 2. Tracks 1 and 3 have no track.
        val track02PathModel: TrackPathModel = TrackPathModel(
            "track02",
            listOf(
                TrackPointModel(1.0, 1.0, "track02"),
                TrackPointModel(2.0, 1.0, "track02"),
                TrackPointModel(3.0, 1.0, "track02"),
                TrackPointModel(4.0, 1.0, "track02"),
                TrackPointModel(5.0, 1.0, "track02"),
                TrackPointModel(6.0, 1.0, "track02"),
                TrackPointModel(7.0, 1.0, "track02"),
                TrackPointModel(8.0, 1.0, "track02"),
                TrackPointModel(9.0, 1.0, "track02"),
                TrackPointModel(10.0, 1.0, "track02")
            )
        )
        // Upsert track 02 & the points.
        trackPathLocalData.upsertTrackWithPath(
            TrackWithPathModel(
                track02Model,
                TrackPathWithPointsModel(
                    track02PathModel.trackUid,
                    track02PathModel.points
                )
            )
        )
        // Upsert the other two tracks, these are inserted directly into cache.
        trackLocalData.upsertTrack(track01Model)
        trackLocalData.upsertTrack(track03Model)
    }

    /*@Test
    fun testTrackPathLocalData(): Unit = runBlocking {
        insertTrackAndPath()
        // Now with these inserted, query a flow for the track path model again, and collect it.
        launch {
            trackPathLocalData.selectTrackWithPath(
                RequestGetTrackWithPath("track01")
            ).collect { trackWithPath ->
                // Ensure it is not null.
                assertNotNull(trackWithPath)
                // Ensure track path with points is not null.
                assertNotNull(trackWithPath!!.trackPathWithPoints)
                // Ensure there are 10 points.
                assertEquals(10, trackWithPath.trackPathWithPoints!!.points.size)
                cancel()
            }
        }

        // Launch another, where we'll get ALL tracks with ALL paths.
        launch {
            trackPathLocalData.selectTracksWithPaths().collect { tracksWithPaths ->
                // Ensure there's 1 item.
                assertEquals(tracksWithPaths.size, 1)
                cancel()
            }
        }
    }

    @Test
    fun testTrackPathUseCase(): Unit = runBlocking {
        insertTrackAndPath()
        // Now with these inserted, call the get world objects use case and collect.
        launch {
            getWorldObjectsUseCase(RequestGetWorldObjects()).collect { worldObjects ->
                // Ensure world objects is not empty.
                assertEquals(false, worldObjects.isEmpty)
                // Ensure there's one track.
                assertEquals(1, worldObjects.tracks.size)
                // Ensure that one track has a valid path.
                assertNotNull(worldObjects.tracks[0].path)
                // Ensure there's 10 points.
                assertEquals(10, worldObjects.tracks[0].path!!.points.size)
                cancel()
            }
        }
    }*/

    @Test
    fun testMultipleTracksPathsUseCase(): Unit = runBlocking {
        // Insert multiple tracks.
        insertTracksAndPaths()
        // Now with these inserted, call the get world objects use case and collect.
        launch {
            getWorldObjectsUseCase(RequestGetWorldObjects()).collect { worldObjects ->
                // Ensure world objects is not empty.
                assertEquals(worldObjects.isEmpty, false)
                // Ensure there's 3 tracks.
                assertEquals(3, worldObjects.tracks.size)
                val tracks: List<TrackWithPath> = worldObjects.tracks
                // Ensure the first is 02, second is 01 and third is 03.
                assertEquals(tracks[0].track.trackUid, "track02")
                assertEquals(tracks[1].track.trackUid, "track01")
                assertEquals(tracks[2].track.trackUid, "track03")
                // Ensure track 2 has a path, the other two don't.
                assertNotNull(tracks[0].path)
                assertNull(tracks[1].path)
                assertNull(tracks[2].path)
                cancel()
            }
        }
    }
}