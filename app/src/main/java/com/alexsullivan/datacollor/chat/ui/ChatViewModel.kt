package com.alexsullivan.datacollor.chat.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.QLPreferences
import com.alexsullivan.datacollor.chat.ChatController
import com.alexsullivan.datacollor.chat.networking.OpenAIService
import com.alexsullivan.datacollor.chat.networking.models.Message
import com.alexsullivan.datacollor.chat.networking.models.MessageContent
import com.alexsullivan.datacollor.serialization.GetLifetimeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val openAIService: OpenAIService,
    private val getLifetimeData: GetLifetimeDataUseCase,
    private val prefs: QLPreferences,
    private val chatController: ChatController
) : ViewModel() {

    val messages = chatController.messages.map(::organizeChatGroups)

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

    private fun MessageContent.toChatMessage(id: String): ChatItem {
        return ChatItem(id = id, text = text.value)
    }

    private fun organizeChatGroups(messages: List<Message>): List<ChatGroupItem> {
        val returnList = mutableListOf<ChatGroupItem>()
        var buildingGroup = mutableListOf<Message>()
        var senderForGroup: Sender? = null
        for (message in messages) {
            val lastMessageTimestamp = buildingGroup.lastOrNull()?.created_at ?: 0
            if (senderForGroup == senderFromRole(message.role) && abs(lastMessageTimestamp - message.created_at) < 30_000) {
                buildingGroup.add(message)
            } else {
                if (buildingGroup.isNotEmpty()) {
                    returnList.add(buildGroupChatItem(buildingGroup, senderForGroup!!))
                }
                buildingGroup = mutableListOf(message)
                senderForGroup = senderFromRole(message.role)
            }
        }
        if (buildingGroup.isNotEmpty()) {
            returnList.add(buildGroupChatItem(buildingGroup, senderForGroup!!))
        }
        return returnList
    }

    private fun buildGroupChatItem(items: List<Message>, sender: Sender): ChatGroupItem {
        return when {
            items.size == 1 && items[0].content.size == 1 -> {
                val item = items[0]
                ChatGroupItem.Single(item.content[0].toChatMessage(item.id), sender, item.id)
            }

            else -> {
                val chatMessages = items.flatMap { message ->
                    message.content.mapIndexed { index, messageContent ->
                        messageContent.toChatMessage(message.id + index)
                    }
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
