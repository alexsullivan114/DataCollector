package com.alexsullivan.datacollor.chat.networking.models

data class Message(
    val id: String,
    val thread_id: String,
    val role: String,
    val content: List<MessageContent>,
    val created_at: Long,
)

data class MessageContent(val type: String, val text: MessageContentText)

data class MessageContentText(val value: String)
