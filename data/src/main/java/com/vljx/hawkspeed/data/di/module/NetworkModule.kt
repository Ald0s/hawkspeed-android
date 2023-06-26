package com.vljx.hawkspeed.data.di.module

import android.app.Application
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.vljx.hawkspeed.data.BuildConfig
import com.vljx.hawkspeed.data.network.api.AccountService
import com.vljx.hawkspeed.data.network.api.RaceService
import com.vljx.hawkspeed.data.network.api.TrackService
import com.vljx.hawkspeed.data.network.api.UserService
import com.vljx.hawkspeed.data.network.serialisation.LocalDateDeserialiser
import com.vljx.hawkspeed.data.network.serialisation.LocalDateSerialiser
import com.vljx.hawkspeed.domain.di.component.AuthenticationComponent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.gotev.cookiestore.SharedPreferencesCookieStore
import net.gotev.cookiestore.okhttp.JavaNetCookieJar
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.internal.cookieToString
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import timber.log.Timber
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.time.LocalDate
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

    // TODO .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
    @Provides
    @Singleton
    fun provideOkHttpClient(cache: Cache, cookieJar: CookieJar): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .authenticator(object: Authenticator {
                @Throws(IOException::class)
                override fun authenticate(route: Route?, response: Response): Request? {
                    // TODO: throw an exception that will cause a global logout.
                    return null
                }
            })
            .addNetworkInterceptor { chain ->
                // TODO: something with cookies here.
                val cookies = cookieJar.loadForRequest(serviceUrl.toHttpUrl())
                Timber.d("Cache: ${cookies.size}")
                cookies.forEach {
                    Timber.d(cookieToString(it, false))
                }
                // TODO: if we're offline, throw an exception here.
                return@addNetworkInterceptor chain.proceed(chain.request())
            }
            .cookieJar(cookieJar)
            .cache(
                when(BuildConfig.DEBUG) {
                    true -> null
                    else -> cache
                }
            )
            .build()

    // TODO: we can add picasso here.

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
}