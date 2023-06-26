package com.vljx.hawkspeed.data.di.module

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vljx.hawkspeed.data.network.serialisation.LocalDateDeserialiser
import com.vljx.hawkspeed.data.network.serialisation.LocalDateSerialiser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CommonModule {
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
}