package com.alexsullivan.datacollor.chat.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.io.ByteArrayInputStream

sealed class ChatGroupItem() {
    abstract val sender: Sender
    abstract val id: String
    data class Group(
        val top: ChatItem,
        val bottom: ChatItem,
        val middleItems: List<ChatItem> = emptyList(),
        override val sender: Sender,
        override val id: String
    ) : ChatGroupItem()

    data class Single(val item: ChatItem, override val sender: Sender, override val id: String) :
        ChatGroupItem()
}

@Preview
@Composable
fun SingleChatGroupPreview() {
    ChatGroup(ChatGroupItem.Single(ChatItem.Text("id1", "Test Single Text"), Sender.SYSTEM, ""))
}

@Preview
@Composable
fun GroupChatGroupPreview() {
    ChatGroup(
        ChatGroupItem.Group(
            top = ChatItem.Text("id1", "Test Top Text"),
            bottom = ChatItem.Text("id2", "Test Bottom Text"),
            middleItems = listOf(ChatItem.Text("id3", "Test Middle Text")),
            sender = Sender.USER,
            id = ""
        )
    )
}

@Composable
fun ChatGroup(groupItem: ChatGroupItem, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(groupItem.sender.padding()),
        horizontalArrangement = groupItem.sender.horizontalArrangement()
    ) {
        when (groupItem) {
            is ChatGroupItem.Group -> GroupChatItem(groupItem = groupItem, modifier)
            is ChatGroupItem.Single -> SingleChatItem(single = groupItem, modifier)
        }
    }
}

@Composable
fun GroupChatItem(groupItem: ChatGroupItem.Group, modifier: Modifier = Modifier) {
    val backgroundColor = groupItem.sender.backgroundColor()
    Column(modifier = modifier.width(IntrinsicSize.Max)) {
        ChatMessage(
            groupItem.top, Modifier
                .padding(bottom = 2.dp)
                .fillMaxWidth()
                .background(backgroundColor, ChatPosition.TOP.shape())
        )
        for (middleItem in groupItem.middleItems) {
            ChatMessage(
                middleItem, Modifier
                    .padding(bottom = 2.dp)
                    .fillMaxWidth()
                    .background(backgroundColor, ChatPosition.MIDDLE.shape())
            )
        }
        ChatMessage(
            groupItem.bottom,
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, ChatPosition.BOTTOM.shape())
        )
    }
}

@Composable
fun SingleChatItem(single: ChatGroupItem.Single, modifier: Modifier) {
    ChatMessage(
        single.item,
        modifier.background(single.sender.backgroundColor(), ChatPosition.SOLO.shape())
    )
}

@Composable
fun ChatMessage(chatItem: ChatItem, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
    ) {
        when (chatItem) {
            is ChatItem.File -> ChatImage(chatItem)
            is ChatItem.Text -> Text(chatItem.text, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun ChatImage(file: ChatItem.File) {
    Image(
        bitmap = byteArrayToImageBitmap(file.bytes),
        contentDescription = "AI generated image",
        modifier = Modifier.clip(RoundedCornerShape(MESSAGE_CORNER_PADDING.dp))
    )
}

private fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap {
    val inputStream = ByteArrayInputStream(byteArray)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    return bitmap.asImageBitmap()
}

@Composable
private fun Sender.backgroundColor(): Color {
    return when (this) {
        Sender.USER -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)
        Sender.SYSTEM -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
    }
}

private fun Sender.padding(): PaddingValues {
    return when (this) {
        Sender.USER -> PaddingValues(start = 16.dp)
        Sender.SYSTEM -> PaddingValues(end = 16.dp)
    }
}

private fun Sender.horizontalArrangement(): Arrangement.Horizontal {
    return when (this) {
        Sender.USER -> Arrangement.End
        Sender.SYSTEM -> Arrangement.Start
    }
}

private fun ChatPosition.shape(): Shape {
    val cornerRadius = MESSAGE_CORNER_PADDING.dp
    return when (this) {
        ChatPosition.TOP -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
        ChatPosition.MIDDLE -> RectangleShape
        ChatPosition.BOTTOM -> RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
        ChatPosition.SOLO -> RoundedCornerShape(cornerRadius)
    }
}

private const val MESSAGE_CORNER_PADDING = 8
