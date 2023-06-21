package com.vljx.hawkspeed.data.database

import com.vljx.hawkspeed.data.database.dao.UserDao
import com.vljx.hawkspeed.data.source.UserLocalData
import javax.inject.Inject

class UserLocalDataImpl @Inject constructor(
    private val userDao: UserDao
): UserLocalData {
}