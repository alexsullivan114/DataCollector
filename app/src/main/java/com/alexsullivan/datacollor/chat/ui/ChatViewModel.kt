package com.alexsullivan.datacollor.chat.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.chat.ChatController
import com.alexsullivan.datacollor.chat.PopulatedMessage
import com.alexsullivan.datacollor.chat.PopulatedMessageContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class ChatViewModel @Inject constructor(private val chatController: ChatController) : ViewModel() {

    val messages = chatController.messages.map(::organizeChatGroups).map { it.reversed() }

    init {
        viewModelScope.launch {
            val result = chatController.initialize()
            Log.d("DEBUGGG", "Finished initializing controller. Resut: $result")
            // TODO: Handle failure
        }

        viewModelScope.launch {
            messages.collect { messages ->
                Log.d("DEBUGGG", "Messages: $messages")
            }
        }
    }

    fun sendMessage(message: String) = viewModelScope.launch {
        chatController.sendMessage(message)
    }

    private fun PopulatedMessageContent.toChatMessage(id: String): ChatItem {
        return when (this) {
            is PopulatedMessageContent.TextContent -> ChatItem.Text(text = text, id = id)
            is PopulatedMessageContent.FileContent -> ChatItem.File(id = id, bytes = byteArray)
        }
    }

    private fun organizeChatGroups(messages: List<PopulatedMessage>): List<ChatGroupItem> {
        val returnList = mutableListOf<ChatGroupItem>()
        var buildingGroup = mutableListOf<PopulatedMessage>()
        var senderForGroup: Sender? = null
        for (message in messages) {
            val lastMessageTimestamp = buildingGroup.lastOrNull()?.createdAt ?: 0
            if (senderForGroup == senderFromRole(message.role) && abs(lastMessageTimestamp - message.createdAt) < 30_000) {
                buildingGroup.add(message)
            } else {
                if (buildingGroup.isNotEmpty()) {
                    returnList.add(buildGroupChatItem(buildingGroup.reversed(), senderForGroup!!))
                }
                buildingGroup = mutableListOf(message)
                senderForGroup = senderFromRole(message.role)
            }
        }
        if (buildingGroup.isNotEmpty()) {
            returnList.add(buildGroupChatItem(buildingGroup.reversed(), senderForGroup!!))
        }
        return returnList
    }

    private fun buildGroupChatItem(items: List<PopulatedMessage>, sender: Sender): ChatGroupItem {
        return when {
            items.size == 1 -> {
                val item = items[0]
                ChatGroupItem.Single(item.content.toChatMessage(item.id), sender, item.id)
            }

            else -> {
                val chatMessages = items.map { message ->
                    message.content.toChatMessage(message.id)
                }

                val id = chatMessages.map { it.id }.reduce { acc, element -> acc + element }
                val top = chatMessages.first()
                val bottom = chatMessages.last()
                val middle = if (chatMessages.size > 2) {
                    chatMessages.subList(2, chatMessages.lastIndex)
                } else {
                    emptyList()
                }
                ChatGroupItem.Group(
                    top = top,
                    middleItems = middle,
                    bottom = bottom,
                    sender = sender,
                    id = id
                )
            }
        }
    }

    private fun senderFromRole(role: String): Sender {
        return when (role) {
            "assistant" -> Sender.SYSTEM
            else -> Sender.USER
        }
    }
}
