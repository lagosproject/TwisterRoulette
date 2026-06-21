package com.lakescorp.twisterroulette.di

import com.lakescorp.twisterroulette.data.SettingsRepositoryImpl
import com.lakescorp.twisterroulette.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// TtsManager and SpeechRecognizerManager are provided directly via their
// @Inject @Singleton constructors and need no module entry. Only the
// interface-to-implementation binding requires a module.
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
