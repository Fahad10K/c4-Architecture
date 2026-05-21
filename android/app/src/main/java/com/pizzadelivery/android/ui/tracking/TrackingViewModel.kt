package com.pizzadelivery.android.ui.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzadelivery.android.data.api.WebSocketManager
import com.pizzadelivery.android.data.model.DeliveryInfo
import com.pizzadelivery.android.data.model.Order
import com.pizzadelivery.android.data.repository.OrderRepository
import com.pizzadelivery.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val webSocketManager: WebSocketManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String = savedStateHandle["orderId"] ?: ""

    private val _trackingState = MutableStateFlow<TrackingUiState>(TrackingUiState.Loading)
    val trackingState: StateFlow<TrackingUiState> = _trackingState.asStateFlow()

    private val _deliveryInfo = MutableStateFlow<DeliveryInfo?>(null)
    val deliveryInfo: StateFlow<DeliveryInfo?> = _deliveryInfo.asStateFlow()

    init {
        loadTracking()
        subscribeToUpdates()
    }

    private fun loadTracking() {
        viewModelScope.launch {
            _trackingState.value = TrackingUiState.Loading

            val orderResult = orderRepository.getOrder(orderId)
            val trackingResult = orderRepository.trackDelivery(orderId)

            if (orderResult is Resource.Success) {
                val order = orderResult.data
                val delivery = (trackingResult as? Resource.Success)?.data
                _deliveryInfo.value = delivery
                _trackingState.value = TrackingUiState.Success(order!!, delivery)
            } else {
                _trackingState.value = TrackingUiState.Error("Failed to load tracking info")
            }
        }
    }

    private fun subscribeToUpdates() {
        webSocketManager.connect()
        webSocketManager.subscribeToDelivery(orderId)

        viewModelScope.launch {
            webSocketManager.deliveryUpdates.collect { update ->
                _deliveryInfo.value = update
                val current = _trackingState.value
                if (current is TrackingUiState.Success) {
                    _trackingState.value = current.copy(delivery = update)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.unsubscribeFromDelivery(orderId)
    }
}

sealed class TrackingUiState {
    object Loading : TrackingUiState()
    data class Success(val order: Order, val delivery: DeliveryInfo?) : TrackingUiState()
    data class Error(val message: String) : TrackingUiState()
}
