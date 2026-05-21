package com.pizzadelivery.android.data.repository

import com.pizzadelivery.android.data.api.ApiService
import com.pizzadelivery.android.data.model.ChatRequest
import com.pizzadelivery.android.data.model.ChatResponse
import com.pizzadelivery.android.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun sendMessage(message: String, conversationId: String?): Resource<ChatResponse> {
        return try {
            val response = apiService.sendChatMessage(
                ChatRequest(message = message, conversationId = conversationId)
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Failed to send message: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}
