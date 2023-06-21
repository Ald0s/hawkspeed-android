package com.vljx.hawkspeed.data.socket

import com.google.gson.Gson
import com.vljx.hawkspeed.data.socket.Extension.emit
import io.socket.client.Ack
import io.socket.emitter.Emitter
import kotlinx.coroutines.Job
import org.json.JSONObject

object Extension {
    suspend inline fun <reified RequestDto, reified ResponseDto> Emitter.sendMessage(name: String, request: RequestDto): ResponseDto {
        // Launch a new completable job here.
        val gotResponseJob = Job()
        // Our result.
        var responseDto: ResponseDto? = null
        // Now we'll send our message to the server.
        this.emit<RequestDto, ResponseDto>(name, request) { response ->
            responseDto = response
            // Complete the job.
            gotResponseJob.complete()
        }
        // Wait for job completion.
        gotResponseJob.join()
        // Return result.
        return responseDto
            ?: throw IllegalStateException("Emitter.sendMessage extension failed because response DTO returned is NULL! This is not allowed.")
    }

    inline fun <reified T> Emitter.on(eventName: String, crossinline listener: (T) -> Unit) {
        val gson = Gson()
        this.on(eventName) { responseArray ->
            val responseJsonObject: JSONObject = responseArray[1] as JSONObject
            val resultObject: T = gson.fromJson(responseJsonObject.toString(), T::class.java)
            listener(resultObject)
        }
    }

    inline fun <reified T> Emitter.emit(eventName: String, messageRequestDto: T) {
        val gson = Gson()
        // Get the outgoing message DTO, but as a JSON object.
        val outgoingJSONObject: JSONObject = JSONObject(gson.toJson(messageRequestDto, T::class.java))
        // Perform the emission, toward the given event name, with the outgoing JSON object.
        this.emit(eventName, outgoingJSONObject)
    }

    inline fun <reified T, reified A> Emitter.emit(eventName: String, messageRequestDto: T, crossinline acknowledge: (responseDto: A) -> Unit) {
        val gson = Gson()
        // Create an acknowledgement layer between SocketIO and our solution.
        val innerAcknowledgement = Ack { response ->
            // Convert the first item in the response array to the type of A via JSON.
            val incomingJSONObject: JSONObject = response[0] as JSONObject
            val incomingDto: A = gson.fromJson(incomingJSONObject.toString(), A::class.java)
            // Call the outer acknowledge function.
            acknowledge(incomingDto)
        }
        // Get the outgoing message DTO, but as a JSON object.
        val outgoingJSONObject = JSONObject(gson.toJson(messageRequestDto, T::class.java))
        // Perform the emission, toward the given event name, with the outgoing JSON object, and with the inner ack.
        this.emit(eventName, outgoingJSONObject, innerAcknowledgement)
    }
}