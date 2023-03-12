package com.vljx.hawkspeed.util

import android.content.Intent

object Extension {
    inline fun <reified T : Enum<T>> Intent.putExtra(victim: T, name: String): Intent =
        putExtra(name, victim.ordinal)

    inline fun <reified T: Enum<T>> Intent.getEnumExtra(name: String): T? =
        getIntExtra(name, -1)
            .takeUnless { it == -1 }
            ?.let { T::class.java.enumConstants?.get(it) }
}