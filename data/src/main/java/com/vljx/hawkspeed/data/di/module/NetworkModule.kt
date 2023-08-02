package com.vljx.hawkspeed.data.di.module

import android.app.Application
import com.google.gson.Gson
import com.vljx.hawkspeed.data.BuildConfig
import com.vljx.hawkspeed.data.network.api.AccountService
import com.vljx.hawkspeed.data.network.api.RaceService
import com.vljx.hawkspeed.data.network.api.TrackService
import com.vljx.hawkspeed.data.network.api.UserService
import com.vljx.hawkspeed.data.network.api.VehicleService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.gotev.cookiestore.SharedPreferencesCookieStore
import net.gotev.cookiestore.okhttp.JavaNetCookieJar
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    private val serviceUrl: String = BuildConfig.SERVICE_URL

    @Provides
    @Singleton
    fun provideHttpCache(application: Application): Cache =
        Cache(application.cacheDir, 10L * 1024L * 1024L)

    @Provides
    @Singleton
    fun provideCookieManager(application: Application): CookieManager =
        CookieManager(
            SharedPreferencesCookieStore(application, "hawkcookies"),
            CookiePolicy.ACCEPT_ALL
        )

    @Provides
    @Singleton
    fun provideCookieJar(cookieManager: CookieManager): CookieJar =
        JavaNetCookieJar(cookieManager)

    @Provides
    @Singleton
    fun provideOkHttpClient(cache: Cache, cookieJar: CookieJar): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .cookieJar(cookieJar)
            .cache(
                when(BuildConfig.DEBUG) {
                    true -> null
                    else -> cache
                }
            )
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        gson: Gson,
        httpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .client(httpClient)
            .baseUrl(serviceUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideAccountService(retrofit: Retrofit): AccountService =
        retrofit.create(AccountService::class.java)

    @Provides
    @Singleton
    fun provideTrackService(retrofit: Retrofit): TrackService =
        retrofit.create(TrackService::class.java)

    @Provides
    @Singleton
    fun provideRaceService(retrofit: Retrofit): RaceService =
        retrofit.create(RaceService::class.java)

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService =
        retrofit.create(UserService::class.java)

    @Provides
    @Singleton
    fun provideVehicleService(retrofit: Retrofit): VehicleService =
        retrofit.create(VehicleService::class.java)
}