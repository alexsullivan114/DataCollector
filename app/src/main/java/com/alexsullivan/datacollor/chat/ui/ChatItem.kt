package com.alexsullivan.datacollor.chat.ui

sealed class ChatItem {
    abstract val id: String
    data class Text(override val id: String, val text: String): ChatItem()
    data class File(override val id: String, val bytes: ByteArray): ChatItem() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as File

            if (id != other.id) return false
            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }
}
