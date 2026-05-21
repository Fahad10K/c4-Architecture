package com.pizzadelivery.android.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzadelivery.android.data.model.ChatMessage
import com.pizzadelivery.android.data.model.ChatRole
import com.pizzadelivery.android.data.repository.ChatRepository
import com.pizzadelivery.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var conversationId: String? = null

    init {
        // Add initial greeting
        _messages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "Hi! I'm your Pizza Delivery assistant. How can I help you today? I can help with:\n• Menu recommendations\n• Order status\n• Store information\n• Delivery updates",
                role = ChatRole.ASSISTANT,
                timestamp = System.currentTimeMillis().toString(),
                suggestions = listOf("Show me the menu", "Track my order", "Find nearest store", "Show offers")
            )
        )
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = content,
            role = ChatRole.USER,
            timestamp = System.currentTimeMillis().toString()
        )

        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            when (val result = chatRepository.sendMessage(content, conversationId)) {
                is Resource.Success -> {
                    result.data?.let { response ->
                        conversationId = response.conversationId
                        _messages.value = _messages.value + response.message
                    }
                }
                is Resource.Error -> {
                    val errorMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = "Sorry, I'm having trouble connecting. Please try again.",
                        role = ChatRole.ASSISTANT,
                        timestamp = System.currentTimeMillis().toString()
                    )
                    _messages.value = _messages.value + errorMessage
                }
                is Resource.Loading -> {}
            }
            _isLoading.value = false
        }
    }
}
