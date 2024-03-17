package com.alexsullivan.datacollor.utils

@Suppress("UNCHECKED_CAST")
suspend fun <T, R> Result<T>.sFlatMap(mapper: suspend (T) -> Result<R>): Result<R> {
    return if (this.isSuccess) {
        mapper(getOrThrow())
    } else {
        this as Result<R>
    }
}

@Suppress("UNCHECKED_CAST")
fun <T, R> Result<T>.flatMap(mapper: (T) -> Result<R>): Result<R> {
    return if (this.isSuccess) {
        mapper(getOrThrow())
    } else {
        this as Result<R>
    }
}
