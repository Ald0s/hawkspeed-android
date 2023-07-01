package com.vljx.hawkspeed

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class WorldMapRecordTrackViewModelTest {

    @Before
    fun setUp() {

    }

    // Going to setup an await type function that checks for a new emission.
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun <T> MutableList<T>.waitForSize(testScope: TestScope, waitFor: Int) {
        val waitForNext = testScope.async {
            withContext(Dispatchers.Default.limitedParallelism(1)) {
                withTimeout(10000) {
                    while(size < waitFor) {
                        delay(200)
                    }
                }
            }
        }
        waitForNext.await()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testWorldMapRecordTrack(): Unit = runTest {
        val numFlow = flowOf(1, 2, 3, 4, 5)

        val numbers = mutableListOf<Int>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            numFlow.toList(numbers)
        }

        numbers.waitForSize(this, 6)
    }
}