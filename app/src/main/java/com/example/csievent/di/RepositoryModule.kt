package com.example.csievent.di

import com.example.csievent.data.remote.api.TeamApi
import com.example.csievent.data.repository.AuthRepositoryImpl
import com.example.csievent.data.repository.CriteriaRepositoryImpl
import com.example.csievent.data.repository.EventRepositoryImpl
import com.example.csievent.data.repository.JudgeAssignmentRepositoryImpl
import com.example.csievent.data.repository.ScoreRepositoryImpl
import com.example.csievent.data.repository.TeamRepositoryImpl
import com.example.csievent.data.repository.UserRepositoryImpl
import com.example.csievent.domain.repository.AuthRepository
import com.example.csievent.domain.repository.CriteriaRepository
import com.example.csievent.domain.repository.EventRepository
import com.example.csievent.domain.repository.JudgeAssignmentRepository
import com.example.csievent.domain.repository.ScoreRepository
import com.example.csievent.domain.repository.TeamRepository
import com.example.csievent.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        impl: EventRepositoryImpl
    ): EventRepository

    @Binds
    abstract fun bindTeamRepository(
        impl: TeamRepositoryImpl
    ): TeamRepository

    @Binds
    abstract fun bindCriteriaRepository(
        impl: CriteriaRepositoryImpl
    ): CriteriaRepository

    @Binds
    abstract fun bindScoreRepository(
        impl: ScoreRepositoryImpl
    ): ScoreRepository

    @Binds
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    abstract fun bindJudgeAssignmentRepository(
        impl: JudgeAssignmentRepositoryImpl
    ): JudgeAssignmentRepository








}
