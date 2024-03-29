package com.vljx.hawkspeed.domain

import com.google.gson.GsonBuilder
import com.vljx.hawkspeed.domain.base.ApiErrorWrapper
import com.vljx.hawkspeed.domain.base.SocketErrorWrapper
import java.lang.StringBuilder
import java.time.LocalDate
import javax.inject.Inject

sealed class ResourceError {
    val gsonBuilder: GsonBuilder =
        GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .enableComplexMapKeySerialization()
            .excludeFieldsWithoutExposeAnnotation()

    /**
     * Create a resource error of the API type. This also has HTTP related information such as the status and status code served.
     */
    data class ApiError(
        val httpStatusCode: Int,
        val httpStatus: String,
        val apiErrorWrapper: ApiErrorWrapper
    ): ResourceError() {
        override val errorSummary: String
            get() = StringBuilder().apply {
                appendLine("=== API Error ===")
                appendLine("HTTP Status Code\t\t$httpStatusCode")
                appendLine("HTTP Status\t\t$httpStatus")
                //val prettyGson = GsonBuilder()
                //    .setPrettyPrinting()
                //    .create()
                val prettyGson = gsonBuilder
                    .setPrettyPrinting()
                    .create()
                appendLine("Error name\t\t${apiErrorWrapper.name}")
                appendLine("Error severity\t\t${apiErrorWrapper.severity}")
                appendLine("Error dict\n${prettyGson.toJson(apiErrorWrapper.errorInformation)}")
            }.toString()
    }

    /**
     * Create a resource error of the Socket type. This deals primarily with errors sent by the socket interface itself and doesn't necessarily do anything about socket-related
     * connectivity errors.
     */
    data class SocketError(
        val socketErrorWrapper: SocketErrorWrapper
    ): ResourceError() {
        val reason: String
            get() = socketErrorWrapper.reason
        
        override val errorSummary: String
            get() = StringBuilder().apply {
                appendLine("=== Socket Error ===")
                //val prettyGson = GsonBuilder()
                //    .setPrettyPrinting()
                //    .create()
                val prettyGson = gsonBuilder
                    .setPrettyPrinting()
                    .create()
                appendLine("Error name\t\t${socketErrorWrapper.name}")
                appendLine("Error reason\t\t${socketErrorWrapper.reason}")
                appendLine("Error dict\n${prettyGson.toJson(socketErrorWrapper.errorInformation)}")
            }.toString()
    }

    /**
     * Create a resource error of the general type. These usually contain just a simple internal message, or an exception.
     */
    data class GeneralError(
        val messageOrType: String,
        val exception: Exception?
    ): ResourceError() {
        override val errorSummary: String
            get() = StringBuilder().apply {
                appendLine("=== General Error ===")
                appendLine("Message\t\t${messageOrType}")
                if(exception != null) {
                    appendLine("Exception type\t\t${exception.javaClass.name}")
                    appendLine("Exception message\t\t${exception.message}")
                    appendLine("Exception stack trace\n${exception.stackTraceToString()}")
                }
            }.toString()

        companion object {
            const val TYPE_DEVICE = "device"
            const val TYPE_SOCKET = "socket"
            const val TYPE_HTTP = "http"
        }
    }

    /**
     * Define an error summary for each type of resource error that briefly discusses the error in question.
     */
    abstract val errorSummary: String
}