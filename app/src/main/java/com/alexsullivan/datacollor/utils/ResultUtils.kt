package com.alexsullivan.datacollor.utils

@Suppress("UNCHECKED_CAST")
inline fun <T, R> Result<T>.flatMap(mapper: (T) -> Result<R>): Result<R> {
    return if (this.isSuccess) {
        mapper(getOrThrow())
    } else {
        this as Result<R>
    }
}
