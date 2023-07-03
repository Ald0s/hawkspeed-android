package com.vljx.hawkspeed.domain.base

interface SocketErrorWrapper {
    /**
     * The overall name for the error.
     */
    val name: String

    /**
     * The reason code for this failure, this is what endpoint code should key off.
     */
    val reason: String

    /**
     * The full error information dictionary, this will contain at least reason.
     */
    val errorInformation: HashMap<String, String>
}