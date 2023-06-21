package com.vljx.hawkspeed.domain.usecase.account

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.repository.AccountRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    @Bridged
    private val accountRepository: AccountRepository
): BaseSuspendingUseCase<Unit, Resource<Account>> {
    override suspend fun invoke(params: Unit): Resource<Account> =
        accountRepository.logout()
}