package com.vljx.hawkspeed.data

import androidx.test.espresso.idling.CountingIdlingResource
import timber.log.Timber

object CountingIdlingResourceSingleton {
    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    var counter = 0

    fun increment() {
        counter++
        Timber.d("CountingIdlingResource increment by 1 (new=$counter")
        countingIdlingResource.increment()
        countingIdlingResource.dumpStateToLogs()
    }

    fun decrement() {
        if(!countingIdlingResource.isIdleNow) {
            counter--
            Timber.d("CountingIdlingResource decrement by 1 (new=$counter")
            countingIdlingResource.decrement()
        } else {
            Timber.w("Counting idling resource has been OVER decremented! Call stack:")
        }
        countingIdlingResource.dumpStateToLogs()
    }

    fun reset() {
        val safety = 100
        for(x in 0..safety) {
            if(countingIdlingResource.isIdleNow) {
                break
            }
            decrement()
        }
    }
}