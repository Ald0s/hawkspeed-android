package com.vljx.hawkspeed.data.di.scope

import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class BackgroundScope @Inject constructor(
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
): CoroutineScope {
    override val coroutineContext: CoroutineContext =
        SupervisorJob() + ioDispatcher
}