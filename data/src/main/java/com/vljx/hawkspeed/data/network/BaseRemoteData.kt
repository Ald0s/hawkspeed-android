package com.vljx.hawkspeed.data.network

import com.google.gson.Gson
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.ApiErrorWrapperDto
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.ResourceImpl
import com.vljx.hawkspeed.domain.exc.NoSessionCookieException
import com.vljx.hawkspeed.domain.exc.NoSessionCookieException.Companion.ERROR_NO_SESSION_COOKIE
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

abstract class BaseRemoteData() {
    @Inject
    lateinit var gson: Gson

    /**
     * Provide a technique for handling remote data source requests. This can be called from any child remote data source
     * class; it will check if the request was successful. If successful, the body will be unpacked and returned as a
     * successful resource. If not successful, specific error handling cases will be applied.
     *
     * @param call A suspending function to call that will execute the request.
     * @return An instance of Resource.
     */
    protected open suspend fun <Dto, Model> getResult(
        call: suspend () -> Response<Dto>,
        mapper: DtoMapper<Dto, Model>
    ): Resource<Model> {
        try {
            // Perform the network request to get back a resource of type dto.
            val resultDtoResource: Resource<Dto> = performNetworkRequest(call)
            if(resultDtoResource.status == Resource.Status.LOADING) {
                // It should also never be a loading status.
                throw NotImplementedError("Failed to getResult(), performing network request returned a LOADING status. This is not possible.")
            } else if(resultDtoResource.status == Resource.Status.ERROR) {
                // If this is an error, recreate a data layer error.
                return ResourceImpl(Resource.Status.ERROR, null, resultDtoResource.resourceError)
            } else if(resultDtoResource.data == null) {
                // Now, with this resource; first throw a not implemented error if the data is null, it should never be null.
                throw NotImplementedError("Failed to getResult(), performing network request returned a successful status but containing a null data. This is not possible.")
            }
            // Next, we'll use our given mapper object to map this data to a data layer equivalent and then return a resource for that type.
            return ResourceImpl.success(
                mapper.mapFromDto(resultDtoResource.data!!)
            )
        } catch (nsc: NoSessionCookieException) {
            // No session cookie thrown. We'll return a resource error detailing this event.
            return ResourceImpl.error(ERROR_NO_SESSION_COOKIE, nsc)
        } catch (e: Exception) {
            return ResourceImpl.error(e.message ?: "unknown", e)
        }
    }

    /**
     *
     */
    protected suspend fun <Dto> performNetworkRequest(
        call: suspend () -> Response<Dto>
    ): Resource<Dto> {
        try {
            Timber.d("Performing remote request; $call")
            val response = call()
            if (response.isSuccessful) {
                Timber.d("Request was successful! Code=%d, Status=%s", response.code(), response.message())
                val body = response.body()
                if (body != null) {
                    val responseContentType: String = response.headers()["Content-Type"] ?: "unknown"
                    Timber.d("Successfully received raw response of type; $responseContentType")
                    // Return a resource for the received DTO.
                    return ResourceImpl.success(body)
                } else {
                    // TODO: implement this properly.
                    Timber.e("Request was successful, but a NULL body was returned...")
                    throw NotImplementedError()
                }
            }
            // This request failed. Handle the remote error, which should get back an API wrapper. We'll simply make an API error resource.
            val apiErrorWrapperDto: ApiErrorWrapperDto = handleRemoteError(response)
            return ResourceImpl.apiError(
                response.code(),
                response.message(),
                apiErrorWrapperDto
            )
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Handles an error that occurred during the acquisition of remote data. This function will automatically determine an action for certain errors, such as
     * those that are global or existential in nature.
     *
     * @param response The retrofit Response object containing the error.
     * @return A data layer instance of an ApiErrorWrapperDto.
     */
    private suspend fun <Dto> handleRemoteError(response: Response<Dto>): ApiErrorWrapperDto {
        val httpCode = response.code()
        val httpStatus = response.message()
        val errorBody = response.errorBody()

        Timber.e("A remote request failed! Code=%d, Status=%s", httpCode, httpStatus)
        // Get the body string from error body, or null.
        val errorBodyString: String? = errorBody?.string()
        if (errorBodyString == null) {
            // TODO: study this for opportunity to implement a better handler of NULL body string for errors.
            Timber.e("Failed to process API error from response ${response}, error body is NULL!")
            throw NotImplementedError("Handling a remote error receiving a NULL error body is not a handled condition yet.")
        }
        // Attempt to parse the error body as an API error dto.
        return gson.fromJson(errorBodyString, ApiErrorWrapperDto::class.java)
    }
}