package com.vljx.hawkspeed.domain.usecase.account

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.repository.AccountRepository
import com.vljx.hawkspeed.domain.requestmodels.account.RequestSetupProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetupAccountProfileUseCase @Inject constructor(
    @Bridged
    private val accountRepository: AccountRepository
): BaseUseCase<RequestSetupProfile, Flow<Resource<Account>>> {
    override fun invoke(params: RequestSetupProfile): Flow<Resource<Account>> =
        accountRepository.setupAccountProfile(params)
}