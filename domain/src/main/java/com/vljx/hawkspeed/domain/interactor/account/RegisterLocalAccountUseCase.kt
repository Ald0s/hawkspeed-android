package com.vljx.hawkspeed.domain.interactor.account

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.interactor.BaseSuspendingUseCase
import com.vljx.hawkspeed.domain.models.account.Registration
import com.vljx.hawkspeed.domain.repository.AccountRepository
import com.vljx.hawkspeed.domain.requests.RegisterLocalAccountRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterLocalAccountUseCase @Inject constructor(
    @Bridged
    private val accountRepository: AccountRepository
): BaseSuspendingUseCase<RegisterLocalAccountRequest, Flow<Resource<Registration>>> {
    override suspend fun invoke(params: RegisterLocalAccountRequest): Flow<Resource<Registration>> =
        accountRepository.registerLocalAccount(params)
}