package com.huabu.app.di

import com.huabu.app.BuildConfig
import com.huabu.app.data.remote.GiphyApiService
import com.huabu.app.data.remote.HuabuApiService
import com.huabu.app.data.remote.YouTubeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.huabu.app/v1/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideHuabuApiService(retrofit: Retrofit): HuabuApiService {
        return retrofit.create(HuabuApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideYouTubeApiService(okHttpClient: OkHttpClient): YouTubeApiService {
        return Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YouTubeApiService::class.java)
    }

    @Provides
    @Named("youtubeApiKey")
    fun provideYouTubeApiKey(): String = BuildConfig.YOUTUBE_API_KEY

    @Provides
    @Named("giphyApiKey")
    fun provideGiphyApiKey(): String = BuildConfig.GIPHY_API_KEY

    @Provides
    @Singleton
    fun provideGiphyApiService(okHttpClient: OkHttpClient): GiphyApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.giphy.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GiphyApiService::class.java)
    }
}
