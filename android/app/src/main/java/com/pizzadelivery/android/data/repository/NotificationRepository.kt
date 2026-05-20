package com.pizzadelivery.android.data.repository

import com.pizzadelivery.android.data.api.ApiService
import com.pizzadelivery.android.data.model.Notification
import com.pizzadelivery.android.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getNotifications(): Resource<List<Notification>> {
        return try {
            val response = apiService.getNotifications()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error("Failed to fetch notifications: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun markAsRead(id: String): Resource<Unit> {
        return try {
            val response = apiService.markNotificationRead(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to mark notification as read")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}
