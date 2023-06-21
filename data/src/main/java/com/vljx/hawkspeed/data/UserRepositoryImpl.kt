package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(

): BaseRepository(), UserRepository {
}