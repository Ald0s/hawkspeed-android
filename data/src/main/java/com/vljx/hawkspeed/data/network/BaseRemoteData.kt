package com.vljx.hawkspeed.data.network

import android.app.Application
import android.content.Intent
import com.google.gson.Gson
import com.vljx.hawkspeed.data.CountingIdlingResourceSingleton
import com.vljx.hawkspeed.data.mapper.ApiErrorMapper
import com.vljx.hawkspeed.data.models.ApiErrorModel
import com.vljx.hawkspeed.data.network.mapper.ApiErrorDtoMapper
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.ApiErrorDto
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.ResourceImpl
import com.vljx.hawkspeed.domain.authentication.AuthenticationSession
import com.vljx.hawkspeed.domain.models.ApiError
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

abstract class BaseRemoteData() {
    @Inject
    lateinit var application: Application

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var authenticationSession: AuthenticationSession

    private val apiErrorDtoMapper = ApiErrorDtoMapper()
    private val apiErrorMapper = ApiErrorMapper()

    /**
     * Provide a technique for handling remote data source requests. This can be called from any child remote data source
     * class; it will check if the request was successful. If successful, the body will be unpacked and returned as a
     * successful resource. If not successful, specific error handling cases will be applied.
     *
     * @param call A suspending function to call that will execute the request.
     * @return An instance of Resource.
     */
    protected suspend fun <Dto, Model> getResult(
        call: suspend () -> Response<Dto>,
        mapper: DtoMapper<Dto, Model>
    ): Resource<Model> {
        try {
            Timber.d("Performing remote request; %s", call.toString())
            CountingIdlingResourceSingleton.increment()
            val response = call()
            if (response.isSuccessful) {
                Timber.d("Request was successful! Code=%d, Status=%s", response.code(), response.message())
                val body = response.body()
                if (body != null) {
                    val bodyModel = mapper.mapFromDto(body)
                    Timber.d("Successfully received resource; %s", bodyModel.toString())
                    return ResourceImpl.success(bodyModel)
                } else {
                    Timber.e("Request was successful, but a NULL body was returned...")
                    throw NotImplementedError()
                }
            }
            return handleRemoteError(response)
        } catch (e: Exception) {
            return ResourceImpl.error(e.message ?: "unknown", e)
        } finally {
            CountingIdlingResourceSingleton.decrement()
        }
    }

    /**
     * Handles an error that occurred during the acquisition of remote data. This function will automatically determine an
     * action for certain errors, such as those that are global or existential in nature.
     *
     * @param response The retrofit Response object containing the error.
     * @return An instance of Resource.
     */
    private suspend fun <Dto, Model> handleRemoteError(response: Response<Dto>): Resource<Model> {
        val httpCode = response.code()
        val httpStatus = response.message()
        val errorBody = response.errorBody()

        Timber.e("A remote request failed! Code=%d, Status=%s", httpCode, httpStatus)
        // Get the body string from error body, or null.
        val errorBodyString: String? = errorBody?.string()
        if(errorBodyString == null) {
            Timber.e("Failed to process API error from response ${response}, error body is NULL!")
            return ResourceImpl.error("unknown", null)
        }
        // Attempt to parse the error body as an API error dto.
        val apiErrorDto: ApiErrorDto = gson.fromJson(errorBodyString, ApiErrorDto::class.java)
        // Immediately convert to a data model, if successful.
        val apiErrorModel: ApiErrorModel = apiErrorDtoMapper.mapFromDto(apiErrorDto)
        // Immediately convert to domain model.
        val apiError: ApiError = apiErrorMapper.mapFromData(apiErrorModel)
        Timber.e("Failed remote request has returned an API error: %s", apiErrorModel.toString())
        // Here, we'll attempt to handle this error automatically.
        val handled = attemptHandleSpecialError(httpCode, httpStatus, apiError)
        // TODO: we can discriminate against this error if it has/hasn't been handled. But for now, we will just return the original too.
        return ResourceImpl.apiError(httpCode, httpStatus, apiError)
    }

    /**
     * Attempt to handle errors that may have special instructions attached to them. For example, upon the receipt of an HTTP 401, irrespective of
     * specifics, the current authentication session will be reset.
     */
    private suspend fun attemptHandleSpecialError(httpCode: Int, httpStatus: String, apiError: ApiError): Boolean {
        // Now, broadcast this ApiError, as long as its global.
        if(apiError.isGlobal) {
            if(!this::application.isInitialized) {
                // If application is not set, throw an exception.
                throw Exception("Failed to broadcast global API error, injected field 'application' is not initialised.")
            }
            Timber.d("Received GLOBAL api error. Broadcasting it now... ($apiError)")
            // Instantiate a new Intent with the global api error action and add the api error itself as the arg.
            val globalApiErrorIntent = Intent(ACTION_GLOBAL_API_ERROR).apply {
                putExtra(ARG_GLOBAL_API_ERROR, apiError)
            }
            // Now, use application to broadcast this intent.
            application.sendBroadcast(globalApiErrorIntent)
        }
        // Now, parse the http code and make decisions.
        when(httpCode) {
            401 -> {
                // An unauthorised error has been encountered. We'll clear our account.
                Timber.w("Remote data request resulted in an HTTP 401; clearing account with reason: ${apiError.errorInformation.get("error-code")}")
                authenticationSession.clearAuthentication()
            }
        }
        return false
    }

    companion object {
        const val ACTION_GLOBAL_API_ERROR = "com.vljx.data.network.BaseRemoteData.ACTION_GLOBAL_API_ERROR"
        const val ARG_GLOBAL_API_ERROR = "com.vljx.data.network.BaseRemoteData.ARG_GLOBAL_API_ERROR"
    }
}