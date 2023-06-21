package com.vljx.hawkspeed.domain.usecase.account

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckLoginUseCase @Inject constructor(
    @Bridged
    private val accountRepository: AccountRepository
): BaseUseCase<Unit, Flow<Resource<Account>>> {
    override fun invoke(params: Unit): Flow<Resource<Account>> =
        accountRepository.attemptAuthentication()
}