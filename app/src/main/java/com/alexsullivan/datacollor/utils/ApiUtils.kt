package com.alexsullivan.datacollor.utils

import java.net.SocketTimeoutException
import java.net.UnknownHostException

inline fun <T> executeCall(block: () -> Result<T>): Result<T> {
    return try {
        block()
    } catch (e: UnknownHostException) {
        Result.failure(e)
    } catch (e: SocketTimeoutException) {
        Result.failure(e)
    }
}
