package com.example.csievent.di

import com.example.csievent.BuildConfig
import com.example.csievent.data.remote.api.AuthApi
import com.example.csievent.data.remote.api.CriteriaApi
import com.example.csievent.data.remote.api.EventApi
import com.example.csievent.data.remote.api.JudgeAssignmentApi
import com.example.csievent.data.remote.api.ScoreApi
import com.example.csievent.data.remote.api.TeamApi
import com.example.csievent.data.remote.api.UserApi
import com.example.csievent.data.remote.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    private const val BASE_URL = "https://csieventmangement.onrender.com/"


    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY    // full logs in development
            } else {
                HttpLoggingInterceptor.Level.NONE    // silent in production
            }
        }
    }


    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor:    AuthInterceptor,        // attaches JWT Bearer token
        loggingInterceptor: HttpLoggingInterceptor  // logs requests/responses
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)        // 1st: add auth header
            .addInterceptor(loggingInterceptor)     // 2nd: log the final request
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


    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideEventApi(retrofit: Retrofit): EventApi =
        retrofit.create(EventApi::class.java)

    @Provides @Singleton
    fun provideTeamApi(retrofit: Retrofit): TeamApi =
        retrofit.create(TeamApi::class.java)

    @Provides @Singleton
    fun provideScoreApi(retrofit: Retrofit): ScoreApi =
        retrofit.create(ScoreApi::class.java)

    @Provides @Singleton
    fun provideCriteriaApi(retrofit: Retrofit): CriteriaApi =
        retrofit.create(CriteriaApi::class.java)

    @Provides @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides @Singleton
    fun provideJudgeAssignmentApi(retrofit: Retrofit): JudgeAssignmentApi =
        retrofit.create(JudgeAssignmentApi::class.java)
}