package com.vljx.hawkspeed.domain

import com.google.gson.GsonBuilder
import com.vljx.hawkspeed.domain.models.ApiError
import java.lang.StringBuilder

data class ResourceErrorImpl(
    override val type: ResourceError.Type,
    override val httpStatusCode: Int?,
    override val httpStatus: String?,
    override val message: String?,
    override val apiError: ApiError?,
    override val exception: Exception?
) : ResourceError {
    override fun toString(): String =
        summariseError()

    override fun getErrorCode(): String =
        when(type) {
            ResourceError.Type.NETWORK -> throw NotImplementedError("getErrorCode() not implemented for resource errors of type 'NETWORK'")
            ResourceError.Type.API -> {
                // This means we must have an API error. If not though, throw an exception.
                if(apiError == null) {
                    throw Exception("getErrorCode() on error of type 'API' requires an apiError attribute! It is NULL! Info:\n${this.summariseError()}")
                }
                // The error code for an API error is found as the name of the api error.
                apiError.name
            }
            ResourceError.Type.DEVICE -> throw NotImplementedError("getErrorCode() not implemented for resource errors of type 'DEVICE'")
            ResourceError.Type.OTHER -> throw NotImplementedError("getErrorCode() not implemented for resource errors of type 'OTHER'")
            else -> throw NotImplementedError("No such error code for type $type")
        }

    override fun summariseError(): String {
        val infoStringBuilder = StringBuilder().apply {
            appendLine("=== Resource Error ===")
            appendLine("Type\t\t${type.name}")
            if(message != null) {
                appendLine("Message\t\t${message}")
            }
            if(exception != null) {
                appendLine("Exception type\t\t${exception.javaClass.name}")
                appendLine("Exception message\t\t${exception.message}")
                appendLine("Exception stack trace\n${exception.stackTraceToString()}")
            }
            if(httpStatus != null && httpStatusCode != null) {
                appendLine("HTTP Status Code\t\t$httpStatusCode")
                appendLine("HTTP Status\t\t$httpStatus")
            }
            if(apiError != null) {
                val prettyGson = GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                appendLine("API Error name\t\t${apiError.name}")
                appendLine("API Error severity\t\t${apiError.severity}")
                appendLine("API Error dict\n${prettyGson.toJson(apiError.errorInformation)}")
            }
        }
        return infoStringBuilder.toString()
    }
}