package com.alexsullivan.datacollor.analysis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alexsullivan.datacollor.analysis.chat.ChatGroup
import com.alexsullivan.datacollor.analysis.chat.ChatGroupItem
import com.alexsullivan.datacollor.analysis.chat.ChatItem
import com.alexsullivan.datacollor.analysis.chat.MessageComposer
import com.alexsullivan.datacollor.analysis.chat.Sender

@Composable
@Preview
fun AnalysisScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
        ) {
            testChatItems.forEach {
                item {
                    ChatGroup(groupItem = it, modifier = Modifier.padding(bottom = 8.dp))
                }
            }
        }
        MessageComposer(modifier = Modifier.padding(top = 8.dp))
    }
}

val testChatItems = listOf(
    ChatGroupItem.Single(
        ChatItem("Test Single"), Sender.SYSTEM),
    ChatGroupItem.Group(
        top = ChatItem("Test Top Item"),
        bottom = ChatItem("Test Bottom Item"),
        sender = Sender.USER
    ),
    ChatGroupItem.Group(
        top = ChatItem("Test Top Item"),
        bottom = ChatItem("Test Bottom Item"),
        sender = Sender.SYSTEM
    ),
    ChatGroupItem.Group(
        top = ChatItem("Test Top Item"),
        bottom = ChatItem("Test Bottom Item"),
        middleItems = listOf(
            ChatItem("Test Middle Item"),
            ChatItem("Test Middle Item")
        ),
        sender = Sender.USER
    ),
    ChatGroupItem.Group(
        top = ChatItem("Test Top Item"),
        bottom = ChatItem("Test Bottom Item"),
        middleItems = listOf(
            ChatItem("Test Middle Item"),
            ChatItem("Test Middle Item")
        ),
        sender = Sender.SYSTEM
    ),
    ChatGroupItem.Single(
        ChatItem("Test Single"), Sender.USER),
    ChatGroupItem.Group(
        top = ChatItem("Test Top Item"),
        bottom = ChatItem("Test Bottom Item"),
        sender = Sender.USER
    ),
    ChatGroupItem.Group(
        top = ChatItem("Test Top Item"),
        bottom = ChatItem("Test Bottom Item"),
        sender = Sender.SYSTEM
    ),
    ChatGroupItem.Group(
        top = ChatItem("Test Top Item"),
        bottom = ChatItem("Test Bottom Item"),
        middleItems = listOf(
            ChatItem("Test Middle Item"),
            ChatItem("Test Middle Item")
        ),
        sender = Sender.USER
    ),
    ChatGroupItem.Group(
        top = ChatItem("Test Top Item"),
        bottom = ChatItem("Test Bottom Item"),
        middleItems = listOf(
            ChatItem("Test Middle Item"),
            ChatItem("Test Middle Item")
        ),
        sender = Sender.SYSTEM
    ),
)
