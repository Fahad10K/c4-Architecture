package com.pizzadelivery.android.data.model

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    val id: String = "",
    val content: String = "",
    val role: ChatRole = ChatRole.USER,
    val timestamp: String = "",
    val suggestions: List<String> = emptyList()
)

enum class ChatRole {
    @SerializedName("user") USER,
    @SerializedName("assistant") ASSISTANT
}

data class ChatRequest(
    val message: String,
    val conversationId: String? = null
)

data class ChatResponse(
    val message: ChatMessage,
    val conversationId: String
)
