package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.BuildConfig
import com.example.myapplication.network.AppApiService
import com.example.myapplication.network.NetworkLogger
import com.example.myapplication.network.OkHttpClientFactory
import com.example.myapplication.network.PrettyHttpLoggingInterceptor
import com.example.myapplication.network.RetrofitFactory
import com.example.myapplication.network.api.JsonPlaceholderApi
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import timber.log.Timber
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_HTTP_BIN = "https://httpbin.org/"
    private const val BASE_JSON_PLACEHOLDER = "https://jsonplaceholder.typicode.com/"

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().serializeNulls().create()

    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache {
        val dir = File(context.cacheDir, "http_cache")
        dir.mkdirs()
        return Cache(dir, 50L * 1024 * 1024)
    }

    @Provides
    @Singleton
    fun provideNetworkLogger(): NetworkLogger =
        NetworkLogger { tag, message ->
            if (!BuildConfig.DEBUG) return@NetworkLogger
            var i = 0
            val len = message.length
            while (i < len) {
                val end = minOf(i + 3500, len)
                Timber.tag(tag).d(message.substring(i, end))
                i = end
            }
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        cache: Cache,
        networkLogger: NetworkLogger,
    ): OkHttpClient {
        val interceptors = buildList {
            if (BuildConfig.DEBUG) {
                add(PrettyHttpLoggingInterceptor(networkLogger))
            }
            add(ChuckerInterceptor.Builder(context).build())
        }
        return OkHttpClientFactory.create(
            cache = cache,
            applicationInterceptors = interceptors,
        )
    }

    @Provides
    @Singleton
    @HttpBinRetrofit
    fun provideHttpBinRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        RetrofitFactory.create(client, BASE_HTTP_BIN, gson)

    @Provides
    @Singleton
    @JsonPlaceholderRetrofit
    fun provideJsonPlaceholderRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        RetrofitFactory.create(client, BASE_JSON_PLACEHOLDER, gson)

    @Provides
    @Singleton
    fun provideAppApiService(@HttpBinRetrofit retrofit: Retrofit): AppApiService =
        retrofit.create(AppApiService::class.java)

    @Provides
    @Singleton
    fun provideJsonPlaceholderApi(@JsonPlaceholderRetrofit retrofit: Retrofit): JsonPlaceholderApi =
        retrofit.create(JsonPlaceholderApi::class.java)
}
