package com.alexsullivan.datacollor.chat.ui

data class ChatViewState(
    val messages: List<ChatGroupItem>,
    val systemResponding: Boolean,
    val showInitializingDialog: Boolean,
    val showInitializingError: Boolean
)
