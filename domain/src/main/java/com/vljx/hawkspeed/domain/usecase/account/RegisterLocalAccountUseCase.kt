package com.vljx.hawkspeed.domain.usecase.account

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import com.vljx.hawkspeed.domain.models.account.Registration
import com.vljx.hawkspeed.domain.repository.AccountRepository
import com.vljx.hawkspeed.domain.requestmodels.account.RequestRegisterLocalAccount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterLocalAccountUseCase @Inject constructor(
    @Bridged
    private val accountRepository: AccountRepository
): BaseUseCase<RequestRegisterLocalAccount, Flow<Resource<Registration>>> {
    override fun invoke(params: RequestRegisterLocalAccount): Flow<Resource<Registration>> =
        accountRepository.registerLocalAccount(params)
}