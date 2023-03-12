package com.vljx.hawkspeed.domain.interactor.account

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.interactor.BaseSuspendingUseCase
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    @Bridged
    private val accountRepository: AccountRepository
): BaseSuspendingUseCase<Unit, Flow<Resource<Account>>> {
    override suspend fun invoke(params: Unit): Flow<Resource<Account>> =
        accountRepository.logout()
}