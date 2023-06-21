package com.vljx.hawkspeed.domain

import com.google.gson.GsonBuilder
import com.vljx.hawkspeed.domain.base.ApiErrorWrapper
import java.lang.StringBuilder

sealed class ResourceError {
    data class ApiError(
        val httpStatusCode: Int,
        val httpStatus: String,
        val apiErrorWrapper: ApiErrorWrapper
    ): ResourceError() {
        override val errorSummary: String
            get() = StringBuilder().apply {
                appendLine("=== API Error Error ===")
                appendLine("HTTP Status Code\t\t$httpStatusCode")
                appendLine("HTTP Status\t\t$httpStatus")
                val prettyGson = GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                appendLine("API Error name\t\t${apiErrorWrapper.name}")
                appendLine("API Error severity\t\t${apiErrorWrapper.severity}")
                appendLine("API Error dict\n${prettyGson.toJson(apiErrorWrapper.errorInformation)}")
            }.toString()
    }

    data class GeneralError(
        val message: String,
        val exception: Exception?
    ): ResourceError() {
        override val errorSummary: String
            get() = StringBuilder().apply {
                appendLine("=== General Error ===")
                appendLine("Message\t\t${message}")
                if(exception != null) {
                    appendLine("Exception type\t\t${exception.javaClass.name}")
                    appendLine("Exception message\t\t${exception.message}")
                    appendLine("Exception stack trace\n${exception.stackTraceToString()}")
                }
            }.toString()
    }

    abstract val errorSummary: String
}