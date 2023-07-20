package com.vljx.hawkspeed.util

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.ComponentActivity

object Extension {
    const val ALPHA = 0.25f

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

    /**
     * https://github.com/Bhide/Low-Pass-Filter-To-Android-Sensors
     */
    fun applyLowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input
        for (i in input.indices) {
            output[i] = output[i] + ALPHA * (input[i] - output[i])
        }
        return output
    }
}