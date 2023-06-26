package com.vljx.hawkspeed.data.source

import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.user.RequestGetUser

interface UserRemoteData {
    /**
     * Perform a query for the indicated User identified by the UID.
     */
    suspend fun queryUserByUid(requestGetUser: RequestGetUser): Resource<UserModel>
}