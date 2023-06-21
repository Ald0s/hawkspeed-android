package com.vljx.hawkspeed.domain

import com.vljx.hawkspeed.domain.base.ApiErrorWrapper

data class ResourceImpl<out T>(
    override val status: Resource.Status,
    override val data: T?,
    override val resourceError: ResourceError?
): Resource<T> {
    companion object {
        /**
         * To be called whenever a resource is successfully loaded. Optionally, an http code can be supplied if the resource
         * was retrieved remotely.
         */
        fun <T> success(data: T): ResourceImpl<T> {
            return ResourceImpl(Resource.Status.SUCCESS, data, null)
        }

        /**
         * Signals that the requested resource is loading.
         */
        fun <T> loading(): Resource<T> {
            return ResourceImpl(Resource.Status.LOADING, null, null)
        }

        /**
         * To be called whenever an API error has occurred. That is, a remote data request has returned any other status than
         * one of the success indicators.
         */
        fun <T> apiError(httpStatusCode: Int, httpStatus: String, apiErrorWrapper: ApiErrorWrapper): ResourceImpl<T> {
            return ResourceImpl(
                Resource.Status.ERROR,
                null,
                ResourceError.ApiError(httpStatusCode, httpStatus, apiErrorWrapper)
            )
        }

        /**
         * To be called whenever any other type of error has occurred.
         */
        fun <T> error(message: String, exception: Exception?): Resource<T> {
            return ResourceImpl(
                Resource.Status.ERROR,
                null,
                ResourceError.GeneralError(message, exception)
            )
        }
    }
}