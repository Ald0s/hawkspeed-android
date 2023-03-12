package com.vljx.hawkspeed.domain

import com.vljx.hawkspeed.domain.models.ApiError

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
         * To be called whenever any device related error has occurred. Remember, this does NOT relate to Android errors at all,
         * specifically just errors that originate from the data layer as a result of the device.
         */
        fun <T> deviceError(message: String, exception: Exception?): ResourceImpl<T> {
            return ResourceImpl(
                Resource.Status.ERROR, null,
                ResourceErrorImpl(
                    ResourceError.Type.DEVICE,
                    null,
                    null,
                    message,
                    null,
                    exception
                )
            )
        }

        /**
         * To be called whenever a network error occurs.
         */
        fun <T> networkError(httpStatusCode: Int, httpStatus: String, exception: Exception? = null): ResourceImpl<T> {
            return ResourceImpl(Resource.Status.ERROR, null,
                ResourceErrorImpl(
                    ResourceError.Type.NETWORK,
                    httpStatusCode,
                    httpStatus,
                    null,
                    null,
                    exception
                )
            )
        }

        /**
         * To be called whenever an API error has occurred. That is, a remote data request has returned any other status than
         * one of the success indicators.
         */
        fun <T> apiError(httpStatusCode: Int, httpStatus: String, apiError: ApiError? = null): ResourceImpl<T> {
            return ResourceImpl(Resource.Status.ERROR, null,
                ResourceErrorImpl(
                    ResourceError.Type.API,
                    httpStatusCode,
                    httpStatus,
                    null,
                    apiError,
                    null
                )
            )
        }

        /**
         * To be called whenever any other type of error has occurred.
         */
        fun <T> error(message: String, exception: Exception?): Resource<T> {
            return ResourceImpl(Resource.Status.ERROR, null,
                ResourceErrorImpl(
                    ResourceError.Type.OTHER,
                    null,
                    null,
                    message,
                    null,
                    exception
                )
            )
        }

        /**
         * Signals that the requested resource is loading.
         */
        fun <T> loading(): Resource<T> {
            return ResourceImpl(Resource.Status.LOADING, null, null)
        }
    }
}