package com.vljx.hawkspeed.domain.di.component

import java.util.concurrent.atomic.AtomicInteger

// TODO: give this class a non-singleton definition
data class ComponentVersion internal constructor(
    private val version: Int = versionSequence.incrementAndGet()
) {
    companion object {
        private val versionSequence = AtomicInteger(0)

        fun next(): ComponentVersion = ComponentVersion()
    }
}