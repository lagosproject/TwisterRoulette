package com.lakescorp.twisterroulette.domain.usecase

import com.lakescorp.twisterroulette.domain.model.AppSettings
import com.lakescorp.twisterroulette.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> {
        return repository.getSettings()
    }
}
