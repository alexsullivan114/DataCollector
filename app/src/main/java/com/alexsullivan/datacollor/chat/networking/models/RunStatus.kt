package com.alexsullivan.datacollor.chat.networking.models

enum class RunStatus {
    QUEUED,
    FAILED,
    COMPLETED,
    UNKNOWN;

    companion object {
        fun from(status: String): RunStatus {
            return when (status) {
                "queued" -> QUEUED
                "failed" -> FAILED
                "completed" -> COMPLETED
                else -> UNKNOWN
            }
        }
    }
}


