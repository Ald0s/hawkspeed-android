package com.vljx.hawkspeed.domain.interactor.account

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.interactor.BaseSuspendingUseCase
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.repository.AccountRepository
import com.vljx.hawkspeed.domain.requests.SetupProfileRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetupProfileUseCase @Inject constructor(
    @Bridged
    private val accountRepository: AccountRepository
): BaseSuspendingUseCase<SetupProfileRequest, Flow<Resource<Account>>> {
    override suspend fun invoke(params: SetupProfileRequest): Flow<Resource<Account>> =
        accountRepository.setupProfile(params)
}