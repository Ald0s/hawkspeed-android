package com.vljx.hawkspeed.data.socket

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.vljx.hawkspeed.data.socket.Extension.emit
import io.socket.client.Ack
import io.socket.emitter.Emitter
import kotlinx.coroutines.Job
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

/**
 * TODO: GSON references in this class do not refer to provided in CommonModule, so all custom (de)serialisers may not work properly. Fix this.
 */
object Extension {
    /**
     * Extension on Socket's sendMessage function that will asynchronously handle the emission of a given DTO request, the receipt of the given DTO response,
     * and the immediate return of said given DTO response, without callbacks.
     */
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

    /**
     * Extension on Socket's on function that will deal with the receipt of a JSON object, the mapping of that object to the desired type, then
     * the invocation of a desired callback with the resulting DTO. We will convert the second item in response array, since first item is the
     * event's name (correct...?)
     */
    inline fun <reified T> Emitter.on(eventName: String, crossinline listener: (T) -> Unit) {
        val gson = Gson()
        this.on(eventName) { responseArray ->
            // Get response string from array at second index, then attempt to convert that from JSON string to the desired type, and call listener.
            val responseJsonObject: JSONObject = responseArray[1] as JSONObject
            val resultObject: T = gson.fromJson(responseJsonObject.toString(), T::class.java)
            listener(resultObject)
        }
    }

    /**
     * Behaves the same as above, but is not strict on the actual type of the object sent to this client. An attempt will be made to load the incoming
     * response as the desired type, but failing this, the original response array will be passed to the failure callback.
     */
    inline fun <reified T> Emitter.on(eventName: String, crossinline listener: (T) -> Unit, crossinline failure: (Array<out Any>) -> Unit) {
        val gson = Gson()
        this.on(eventName) { responseArray ->
            try {
                // Get response string from array at second index, then attempt to convert that from JSON string to the desired type, and call listener.
                val responseJsonObject: JSONObject = responseArray[1] as JSONObject
                val resultObject: T = gson.fromJson(responseJsonObject.toString(), T::class.java)
                listener(resultObject)
            } catch(aiofb: ArrayIndexOutOfBoundsException) {
                // Failed to load incoming response, call failure handler.
                failure(responseArray)
            } catch(jse: JsonSyntaxException) {
                // We'll print out a warning for the exception, just to be thorough.
                Timber.w(jse)
                // Failed to load incoming response, call failure handler.
                failure(responseArray)
            }
        }
    }

    /**
     * Extension on Socket's emit function that will emit the given request DTO, and return no acknowledgement.
     */
    inline fun <reified T> Emitter.emit(eventName: String, messageRequestDto: T) {
        val gson = Gson()
        // Get the outgoing message DTO, but as a JSON object.
        val outgoingJSONObject: JSONObject = JSONObject(gson.toJson(messageRequestDto, T::class.java))
        // Perform the emission, toward the given event name, with the outgoing JSON object.
        this.emit(eventName, outgoingJSONObject)
    }

    /**
     * Extension on Socket's emit function that will emit the given request DTO, then load the response as the given response DTO, then invoke a callback
     * keeping in line with original emit function; which utilises acknowledgements.
     */
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

    /**
     * Credit: https://stackoverflow.com/a/64002903
     * Maps the given JSONObject to a map of string to string. This function does not support multi-level objects.
     */
    fun JSONObject.toMap(): Map<String, String> = keys().asSequence().associateWith {
        // TODO: come up with proprietary solution for this.
        when (val value = this[it])
        {
            is JSONArray -> throw NotImplementedError()
            is JSONObject -> throw NotImplementedError()
            JSONObject.NULL -> throw NotImplementedError()
            else -> value.toString()
        }
    }
}