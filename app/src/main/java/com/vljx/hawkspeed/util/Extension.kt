package com.vljx.hawkspeed.util

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.ComponentActivity

object Extension {
    fun Context.getActivity(): ComponentActivity {
        val currentContext = this
        while(currentContext is ContextWrapper) {
            if(currentContext is ComponentActivity) {
                return currentContext
            }
        }
        throw IllegalStateException("Failed to getActivity() from context. Current context is either not a contextwrapper or is not a component activity.")
    }

    inline fun <reified T : Enum<T>> Intent.putExtra(victim: T, name: String): Intent =
        putExtra(name, victim.ordinal)

    inline fun <reified T: Enum<T>> Intent.getEnumExtra(name: String): T? =
        getIntExtra(name, -1)
            .takeUnless { it == -1 }
            ?.let { T::class.java.enumConstants?.get(it) }
}