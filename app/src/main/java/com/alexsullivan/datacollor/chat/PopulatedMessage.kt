package com.alexsullivan.datacollor.chat

data class PopulatedMessage(
    val id: String,
    val role: String,
    val createdAt: Long,
    val content: PopulatedMessageContent
)

sealed class PopulatedMessageContent {
    data class TextContent(val text: String) : PopulatedMessageContent()
    data class FileContent(val byteArray: ByteArray) : PopulatedMessageContent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FileContent

            if (!byteArray.contentEquals(other.byteArray)) return false

            return true
        }

        override fun hashCode(): Int {
            return byteArray.contentHashCode()
        }
    }
}
