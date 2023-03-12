package com.vljx.hawkspeed

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class HawkSpeedApp: Application() {
    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    companion object {
        // TODO review sample
        // https://github.com/JakeWharton/timber/blob/trunk/timber-sample/src/main/java/com/example/timber/ExampleApp.java
        class CrashReportingTree: Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                    return;
                }
                // TODO: log via a crash library here. .log()
                if(t != null) {
                    if(priority == Log.ERROR) {
                        // TODO: log via a crash library here .logError
                    } else if(priority == Log.WARN) {
                        // TODO: log via a crash library here .logWarning
                    }
                }
            }
        }
    }
}