package com.vljx.hawkspeed.domain.usecase.account

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import com.vljx.hawkspeed.domain.models.account.CheckName
import com.vljx.hawkspeed.domain.repository.AccountRepository
import com.vljx.hawkspeed.domain.requestmodels.account.RequestCheckName
import javax.inject.Inject

class CheckNameUseCase @Inject constructor(
    @Bridged
    private val accountRepository: AccountRepository
): BaseSuspendingUseCase<RequestCheckName, Resource<CheckName>> {
    override suspend fun invoke(params: RequestCheckName): Resource<CheckName> =
        accountRepository.checkUsernameTaken(params)
}