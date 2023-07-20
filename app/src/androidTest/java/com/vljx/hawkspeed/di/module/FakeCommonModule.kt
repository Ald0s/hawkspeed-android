package com.vljx.hawkspeed.di.module

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vljx.hawkspeed.data.di.module.CommonModule
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.data.network.serialisation.LocalDateDeserialiser
import com.vljx.hawkspeed.data.network.serialisation.LocalDateSerialiser
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import java.time.LocalDate
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CommonModule::class]
)
class FakeCommonModule {
    @Provides
    @Singleton
    fun provideGsonBuilder(): GsonBuilder =
        GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .enableComplexMapKeySerialization()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(LocalDate::class.java, LocalDateSerialiser())
            .registerTypeAdapter(LocalDate::class.java, LocalDateDeserialiser())

    @Provides
    @Singleton
    fun provideGson(
        gsonBuilder: GsonBuilder
    ): Gson = gsonBuilder.create()

    @OptIn(ExperimentalCoroutinesApi::class)
    @IODispatcher
    @Provides
    fun provideIODispatcher(): CoroutineDispatcher =
        StandardTestDispatcher()
}