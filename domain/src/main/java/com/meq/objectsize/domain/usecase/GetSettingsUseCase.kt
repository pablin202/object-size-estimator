package com.meq.objectsize.domain.usecase

import com.meq.objectsize.domain.entity.AppSettings
import com.meq.objectsize.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get current app settings as a Flow
 */
class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = settingsRepository.settings
}
