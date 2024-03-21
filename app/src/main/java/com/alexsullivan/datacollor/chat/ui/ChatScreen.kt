package com.alexsullivan.datacollor.chat.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexsullivan.datacollor.utils.byteArrayToImageBitmap

@Composable
@Preview
fun ChatScreen() {
    Scaffold {
        var imageToBlowUp by remember { mutableStateOf<ByteArray?>(null) }
        val viewModel = hiltViewModel<ChatViewModel>()
        val viewState by viewModel.chatViewState.collectAsStateWithLifecycle()
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(it)
        ) {
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
                        ChatGroup(
                            groupItem = it,
                            modifier = Modifier.padding(bottom = 8.dp),
                            onImageTap = { imageToBlowUp = it })
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
        imageToBlowUp?.let { image ->
            Dialog(
                properties = DialogProperties(usePlatformDefaultWidth = false),
                onDismissRequest = { imageToBlowUp = null }) {
                Image(bitmap = byteArrayToImageBitmap(image), contentDescription = "AI Generated Image")
            }
        }

        if (viewState.initializing) {
            Dialog(
                properties = DialogProperties(usePlatformDefaultWidth = false),
                onDismissRequest = {}) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}
