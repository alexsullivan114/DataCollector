package com.alexsullivan.datacollor.chat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
@Preview
fun ChatScreen() {
    val viewModel = hiltViewModel<ChatViewModel>()
    Column(modifier = Modifier.padding(16.dp)) {
        val messages by viewModel.messages.collectAsStateWithLifecycle(initialValue = emptyList())
        val chatListState = rememberLazyListState()

        LaunchedEffect(key1 = messages) {
            chatListState.animateScrollToItem(messages.size)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f),
            state = chatListState
        ) {
            messages.forEach {
                item(key = it.id) {
                    ChatGroup(groupItem = it, modifier = Modifier.padding(bottom = 8.dp))
                }
            }
        }
        MessageComposer(
            modifier = Modifier.padding(top = 8.dp),
            onSendClick = viewModel::sendMessage
        )
    }
}
