package com.alexsullivan.datacollor.chat.networking.models

data class CreateAssistant(
    val instructions: String,
    val name: String,
    val tools: List<AssistantTool>,
    val model: String = "gpt-3.5-turbo",
    val file_ids: List<String>
)

data class AssistantTool(val type: String = "code_interpreter")
