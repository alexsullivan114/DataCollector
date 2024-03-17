@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalMaterialApi::class
)

package com.alexsullivan.datacollor.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.alexsullivan.datacollor.R

@Composable
fun MessageComposer(modifier: Modifier = Modifier, onSendClick: (String) -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(25.dp)
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            TextInput(modifier = Modifier.weight(1.0f))
            Icon(
                Icons.AutoMirrored.Filled.Send,
                stringResource(id = R.string.send_message),
                modifier = Modifier.padding(start = 16.dp).clickable {
                    onSendClick("")
                }
            )
        }
    }
}

@Composable
private fun TextInput(modifier: Modifier) {
    val rainbowColors = listOf(Color.Red, Color.Blue, Color.Magenta)
    var text by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val brush = remember {
        Brush.linearGradient(
            colors = rainbowColors
        )
    }
    BasicTextField(
        modifier = modifier,
        value = text,
        onValueChange = { text = it },
        textStyle = TextStyle(brush = brush),
        interactionSource = interactionSource,
        decorationBox = {
            TextFieldDefaults.TextFieldDecorationBox(
                value = text,
                innerTextField = it,
                enabled = true,
                interactionSource = interactionSource,
                singleLine = false,
                visualTransformation = VisualTransformation.None,
                contentPadding = PaddingValues(0.dp),
                placeholder = {
                    Text(text = "Message...")
                },
            )
        }
    )
}

