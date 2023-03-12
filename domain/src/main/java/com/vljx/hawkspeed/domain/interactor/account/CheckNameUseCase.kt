package com.vljx.hawkspeed.domain.interactor.account

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.interactor.BaseSuspendingUseCase
import com.vljx.hawkspeed.domain.models.account.CheckName
import com.vljx.hawkspeed.domain.repository.AccountRepository
import com.vljx.hawkspeed.domain.requests.CheckNameRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckNameUseCase @Inject constructor(
    @Bridged
    private val accountRepository: AccountRepository
): BaseSuspendingUseCase<CheckNameRequest, Flow<Resource<CheckName>>> {
    override suspend fun invoke(params: CheckNameRequest): Flow<Resource<CheckName>> =
        accountRepository.checkUsernameTaken(params)
}