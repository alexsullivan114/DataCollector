package com.alexsullivan.datacollor.chat.ui

data class ChatViewState(
    val messages: List<ChatGroupItem>,
    val waiting: Boolean,
    val initializing: Boolean
)
