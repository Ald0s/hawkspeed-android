package com.vljx.hawkspeed

import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.di.module.DataModule
import com.vljx.hawkspeed.data.di.module.DatabaseModule
import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.models.track.TrackPathModel
import com.vljx.hawkspeed.data.models.track.TrackPointModel
import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.data.source.TrackLocalData
import com.vljx.hawkspeed.data.source.TrackPathLocalData
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
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

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        appDatabase.clearAllTables()
    }

    @Test
    fun testTrackPath(): Unit = runBlocking {
        // Create a track model.
        val trackModel: TrackModel = TrackModel(
            "track01",
            "Track",
            "Track DEsc",
            UserModel("US", "aldos", 0, true, true),
            TrackPointModel(1.0, 1.0, "track01"),
            true, true, true, true
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
        trackLocalData.upsertTrack(trackModel)
        trackPathLocalData.upsertTrackPath(trackPathModel)
        // Now with these inserted, query a flow for the track path model again, and collect it.
        launch {
            trackPathLocalData.selectTrackPath(GetTrackPathRequest("track01")).collect { receivedTrackPathModel ->
                // Ensure it is not null.
                assertNotNull(receivedTrackPathModel)
                // Ensure there are 10 points.
                assertEquals(receivedTrackPathModel!!.points.size, 10)
                // Ensure points, when iterated each ascends the last.
                var currentBiggest: Double = 0.0
                receivedTrackPathModel.points.forEach {
                    // Ensure track point model's latitude is greater than current biggest.
                    assertTrue(it.latitude > currentBiggest)
                    // Set the current biggest to this latitude.
                    currentBiggest = it.latitude
                }
                cancel()
            }
        }
    }
}