package com.vljx.hawkspeed.domain.usecase.user

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.repository.UserRepository
import com.vljx.hawkspeed.domain.requestmodels.user.RequestGetUser
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    @Bridged
    private val userRepository: UserRepository
): BaseUseCase<RequestGetUser, Flow<Resource<User>>> {
    override fun invoke(params: RequestGetUser): Flow<Resource<User>> =
        userRepository.getUserByUid(params)
}