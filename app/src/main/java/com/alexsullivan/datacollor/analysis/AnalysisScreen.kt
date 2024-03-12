package com.alexsullivan.datacollor.analysis

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
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            ChatGroup(
                groupItem = ChatGroupItem.Single(
                    ChatItem("Test Single", Sender.SYSTEM)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            ChatGroup(
                groupItem = ChatGroupItem.Group(
                    top = ChatItem("Test Top Item", Sender.SYSTEM),
                    bottom = ChatItem("Test Bottom Item", Sender.SYSTEM)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            ChatGroup(
                groupItem = ChatGroupItem.Group(
                    top = ChatItem("Test Top Item", Sender.SYSTEM),
                    bottom = ChatItem("Test Bottom Item", Sender.SYSTEM)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            ChatGroup(
                groupItem = ChatGroupItem.Group(
                    top = ChatItem("Test Top Item", Sender.SYSTEM),
                    bottom = ChatItem("Test Bottom Item", Sender.SYSTEM),
                    middleItems = listOf(
                        ChatItem("Test Middle Item", Sender.SYSTEM),
                        ChatItem("Test Middle Item", Sender.SYSTEM)
                    )
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            ChatGroup(
                groupItem = ChatGroupItem.Group(
                    top = ChatItem("Test Top Item", Sender.USER),
                    bottom = ChatItem("Test Bottom Item", Sender.USER),
                    middleItems = listOf(
                        ChatItem("Test Middle Item", Sender.USER),
                        ChatItem("Test Middle Item", Sender.USER)
                    )
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            MessageComposer()
        }
    }
}
