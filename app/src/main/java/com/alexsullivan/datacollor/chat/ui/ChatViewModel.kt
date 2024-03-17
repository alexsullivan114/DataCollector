package com.alexsullivan.datacollor.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexsullivan.datacollor.QLPreferences
import com.alexsullivan.datacollor.chat.ChatController
import com.alexsullivan.datacollor.chat.networking.OpenAIService
import com.alexsullivan.datacollor.chat.networking.models.Message
import com.alexsullivan.datacollor.serialization.GetLifetimeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val openAIService: OpenAIService,
    private val getLifetimeData: GetLifetimeDataUseCase,
    private val prefs: QLPreferences,
    private val chatController: ChatController
) : ViewModel() {

    val messages = chatController.messages.map { createMessageItems ->
        createMessageItems.flatMap { it.toChatMessage() }
    }

    init {
        viewModelScope.launch {
            val result = chatController.initialize()
            // TODO: Handle failure
        }
    }

    fun sendMessage(message: String) = viewModelScope.launch {
        chatController.sendMessage(message)
    }

    private fun Message.toChatMessage(): List<ChatItem> {
        return content.mapIndexed { index, messageContent ->
            ChatItem(id = id + index, text = messageContent.text.value)
        }
    }
}
