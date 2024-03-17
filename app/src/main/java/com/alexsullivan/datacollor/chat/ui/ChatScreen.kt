package com.alexsullivan.datacollor.chat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
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
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
        ) {
            messages.forEach {
                item(key = it.id) {
                    ChatGroup(groupItem = it, modifier = Modifier.padding(bottom = 8.dp))
                }
            }
//            testChatItems.forEach {
//                item {
//                    ChatGroup(groupItem = it, modifier = Modifier.padding(bottom = 8.dp))
//                }
//            }
        }
        MessageComposer(
            modifier = Modifier.padding(top = 8.dp),
            onSendClick = viewModel::sendMessage
        )
    }
}

//val testChatItems = listOf(
//    ChatGroupItem.Single(
//        ChatItem("id1", "Test Single"), Sender.SYSTEM
//    ),
//    ChatGroupItem.Group(
//        top = ChatItem("id2", "Test Top Item"),
//        bottom = ChatItem("id3", "Test Bottom Item"),
//        sender = Sender.USER
//    ),
//    ChatGroupItem.Group(
//        top = ChatItem("id4", "Test Top Item"),
//        bottom = ChatItem("id5", "Test Bottom Item"),
//        sender = Sender.SYSTEM
//    ),
//    ChatGroupItem.Group(
//        top = ChatItem("id6", "Test Top Item"),
//        bottom = ChatItem("id7", "Test Bottom Item"),
//        middleItems = listOf(
//            ChatItem("id8", "Test Middle Item"),
//            ChatItem("id9", "Test Middle Item")
//        ),
//        sender = Sender.USER
//    ),
//    ChatGroupItem.Group(
//        top = ChatItem("id10", "Test Top Item"),
//        bottom = ChatItem("id11", "Test Bottom Item"),
//        middleItems = listOf(
//            ChatItem("id12", "Test Middle Item"),
//            ChatItem("id13", "Test Middle Item")
//        ),
//        sender = Sender.SYSTEM
//    ),
//    ChatGroupItem.Single(
//        ChatItem("id14", "Test Single"), Sender.USER
//    ),
//    ChatGroupItem.Group(
//        top = ChatItem("id15", "Test Top Item"),
//        bottom = ChatItem("id16", "Test Bottom Item"),
//        sender = Sender.USER
//    ),
//    ChatGroupItem.Group(
//        top = ChatItem("id17", "Test Top Item"),
//        bottom = ChatItem("id18", "Test Bottom Item"),
//        sender = Sender.SYSTEM
//    ),
//    ChatGroupItem.Group(
//        top = ChatItem("id19", "Test Top Item"),
//        bottom = ChatItem("id20", "Test Bottom Item"),
//        middleItems = listOf(
//            ChatItem("id21", "Test Middle Item"),
//            ChatItem("id22", "Test Middle Item")
//        ),
//        sender = Sender.USER
//    ),
//    ChatGroupItem.Group(
//        top = ChatItem("id23", "Test Top Item"),
//        bottom = ChatItem("id24", "Test Bottom Item"),
//        middleItems = listOf(
//            ChatItem("id25", "Test Middle Item"),
//            ChatItem("id26", "Test Middle Item")
//        ),
//        sender = Sender.SYSTEM
//    ),
//)
