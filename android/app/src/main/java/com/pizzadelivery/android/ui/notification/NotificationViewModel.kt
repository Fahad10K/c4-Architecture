package com.pizzadelivery.android.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzadelivery.android.data.api.WebSocketManager
import com.pizzadelivery.android.data.model.Notification
import com.pizzadelivery.android.data.repository.NotificationRepository
import com.pizzadelivery.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _notificationsState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val notificationsState: StateFlow<NotificationsUiState> = _notificationsState.asStateFlow()

    init {
        loadNotifications()
        listenForNewNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _notificationsState.value = NotificationsUiState.Loading
            when (val result = notificationRepository.getNotifications()) {
                is Resource.Success -> {
                    _notificationsState.value = NotificationsUiState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _notificationsState.value = NotificationsUiState.Error(result.message ?: "Failed to load")
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun listenForNewNotifications() {
        viewModelScope.launch {
            webSocketManager.notifications.collect { newNotification ->
                val current = _notificationsState.value
                if (current is NotificationsUiState.Success) {
                    _notificationsState.value = current.copy(
                        notifications = listOf(newNotification) + current.notifications
                    )
                }
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
            val current = _notificationsState.value
            if (current is NotificationsUiState.Success) {
                _notificationsState.value = current.copy(
                    notifications = current.notifications.map {
                        if (it.id == id) it.copy(isRead = true) else it
                    }
                )
            }
        }
    }
}

sealed class NotificationsUiState {
    object Loading : NotificationsUiState()
    data class Success(val notifications: List<Notification>) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}
