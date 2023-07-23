package com.vljx.hawkspeed.base

import androidx.annotation.RawRes
import androidx.test.platform.app.InstrumentationRegistry
import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.models.track.TrackPathWithPointsModel
import com.vljx.hawkspeed.data.models.track.TrackPointModel
import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack.WorldMapRecordTrackUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.ByteArrayOutputStream

open class BaseTest {
    // Going to setup an await type function that checks for a new emission.
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun <T> MutableList<T>.waitForSize(testScope: TestScope, waitFor: Int, timeoutMs: Long = 10000) {
        val waitForNext = testScope.async {
            withContext(Dispatchers.Default.limitedParallelism(1)) {
                withTimeout(timeoutMs) {
                    while(size < waitFor) {
                        delay(200)
                    }
                }
            }
        }
        waitForNext.await()
        /*val waitForNext = testScope.async {
            var waitedForMs = 0
            while(size < waitFor) {
                if(waitedForMs > 10000) {
                    throw NotImplementedError()
                }
                delay(200)
                waitedForMs += 200
            }
        }
        waitForNext.await()*/
    }

    fun getTrackWithPathFromResource(
        @RawRes rawId: Int
    ): TrackWithPathModel {
        // Read as string.
        val trackWithPathJsonString = getResourceAsString(rawId)
        // Now, convert this to a JSON object.
        val trackJsonObject = JSONObject(trackWithPathJsonString)
        val trackUid = trackJsonObject.getString("name")

        val trackPathModel = TrackPathWithPointsModel(
            trackUid,
            trackJsonObject
                .getJSONArray("segments")
                .getJSONObject(0)
                .getJSONArray("points")
                .run {
                    val trackPointModelList = mutableListOf<TrackPointModel>()
                    for(i in 0 until length()) {
                        val trackPointJson = getJSONObject(i)
                        trackPointModelList.add(
                            TrackPointModel(
                                trackPointJson.getDouble("latitude"),
                                trackPointJson.getDouble("longitude"),
                                trackUid
                            )
                        )
                    }
                    return@run trackPointModelList
                }
        )

        val trackModel = TrackModel(
            trackUid,
            trackJsonObject.getString("name"),
            trackJsonObject.getString("description"),
            UserModel("USER01", "aldos", "bio", 0, false, true),
            listOf(),
            trackPathModel.points[0],
            180f, true,1893, true, TrackType.SPRINT,
            0, 0, null, 0, true, true, true, true
        )

        return TrackWithPathModel(
            trackModel,
            trackPathModel
        )
    }

    fun getResourceAsString(@RawRes rawId: Int): String {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        appContext.resources.openRawResource(rawId).let {
            // https://gist.github.com/dupontgu/6b7e0c6fab037f7d08a82c44533873b2
            val result = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var length = it.read(buffer)
            while (length != -1) {
                result.write(buffer, 0, length)
                length = it.read(buffer)
            }
            return result.toString("UTF-8")
        }
    }
}