package com.vljx.hawkspeed.domain.interactor.account

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.interactor.BaseSuspendingUseCase
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.repository.AccountRepository
import com.vljx.hawkspeed.domain.requests.LoginRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    @Bridged
    private val accountRepository: AccountRepository
): BaseSuspendingUseCase<LoginRequest, Flow<Resource<Account>>> {
    override suspend fun invoke(params: LoginRequest): Flow<Resource<Account>> =
        accountRepository.attemptAuthentication(params)
}