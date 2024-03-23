package com.alexsullivan.datacollor.chat.networking.models

enum class RunStatus {
    QUEUED,
    IN_PROGRESS,
    FAILED,
    COMPLETED,
    UNKNOWN;

    companion object {
        fun from(status: String): RunStatus {
            return when (status) {
                "queued" -> QUEUED
                "failed" -> FAILED
                "completed" -> COMPLETED
                "in_progress" -> IN_PROGRESS
                else -> UNKNOWN
            }
        }
    }
}


