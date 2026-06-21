package com.lakescorp.twisterroulette.domain.repository

import com.lakescorp.twisterroulette.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun saveSettings(settings: AppSettings)
}
