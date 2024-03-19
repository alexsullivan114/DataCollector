package com.alexsullivan.datacollor.chat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
        val viewState by viewModel.chatViewState.collectAsStateWithLifecycle(
            initialValue = ChatViewState(
                emptyList(), false
            )
        )
        val chatListState = rememberLazyListState()

        LaunchedEffect(key1 = viewState) {
            chatListState.animateScrollToItem(viewState.messages.size)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f),
            state = chatListState
        ) {
            viewState.messages.forEach {
                item(key = it.id) {
                    ChatGroup(groupItem = it, modifier = Modifier.padding(bottom = 8.dp))
                }
            }
            if (viewState.waiting) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        MessageComposer(
            modifier = Modifier.padding(top = 8.dp),
            onSendClick = viewModel::sendMessage
        )
    }
}
