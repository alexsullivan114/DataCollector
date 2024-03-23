package com.alexsullivan.datacollor.chat.ui

sealed class ChatGroupItem {
    abstract val sender: Sender
    abstract val id: String
    data class Group(
        val top: ChatItem,
        val bottom: ChatItem,
        val middleItems: List<ChatItem> = emptyList(),
        override val sender: Sender,
        override val id: String
    ) : ChatGroupItem()

    data class Single(val item: ChatItem, override val sender: Sender, override val id: String) :
        ChatGroupItem()
}
