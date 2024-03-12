package com.alexsullivan.datacollor.analysis.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

sealed class ChatGroupItem {
    data class Group(
        val top: ChatItem,
        val bottom: ChatItem,
        val middleItems: List<ChatItem> = emptyList()
    ) : ChatGroupItem()

    data class Single(val item: ChatItem) : ChatGroupItem()
}

@Preview
@Composable
fun SingleChatGroupPreview() {
    ChatGroup(ChatGroupItem.Single(ChatItem("Test Single Text", Sender.SYSTEM)))
}

@Preview
@Composable
fun GroupChatGroupPreview() {
    ChatGroup(
        ChatGroupItem.Group(
            top = ChatItem("Test Top Text", Sender.SYSTEM),
            bottom = ChatItem("Test Bottom Text", Sender.SYSTEM),
            middleItems = listOf(ChatItem("Test Middle Text", Sender.SYSTEM))
        )
    )
}

@Composable
fun ChatGroup(groupItem: ChatGroupItem, modifier: Modifier = Modifier) {
    when (groupItem) {
        is ChatGroupItem.Group -> GroupChatItem(groupItem = groupItem, modifier)
        is ChatGroupItem.Single -> SingleChatItem(single = groupItem, modifier)
    }
}

@Composable
fun GroupChatItem(groupItem: ChatGroupItem.Group, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        ChatMessage(groupItem.top, ChatPosition.TOP, Modifier.padding(bottom = 2.dp))
        for (middleItem in groupItem.middleItems) {
            ChatMessage(middleItem, ChatPosition.MIDDLE, Modifier.padding(bottom = 2.dp))
        }
        ChatMessage(groupItem.bottom, ChatPosition.BOTTOM)
    }
}

@Composable
fun SingleChatItem(single: ChatGroupItem.Single, modifier: Modifier) {
    ChatMessage(single.item, ChatPosition.SOLO, modifier)
}

@Composable
fun ChatMessage(chatItem: ChatItem, chatPosition: ChatPosition, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = chatItem.sender.horizontalArrangement()) {
        Box(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.tertiaryContainer,
                chatPosition.shape()
            )
        ) {
            Text(chatItem.text, modifier = Modifier.padding(8.dp))
        }
    }
}

private fun Sender.horizontalArrangement(): Arrangement.Horizontal {
    return when (this) {
        Sender.USER -> Arrangement.End
        Sender.SYSTEM -> Arrangement.Start
    }
}

private fun ChatPosition.shape(): Shape {
    val cornerRadius = 8.dp
    return when (this) {
        ChatPosition.TOP -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
        ChatPosition.MIDDLE -> RectangleShape
        ChatPosition.BOTTOM -> RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
        ChatPosition.SOLO -> RoundedCornerShape(cornerRadius)
    }
}
