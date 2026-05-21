package com.pizzadelivery.android.ui.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String? = savedStateHandle["orderId"]

    private val _ordersState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val ordersState: StateFlow<OrdersUiState> = _ordersState.asStateFlow()

    private val _orderDetail = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val orderDetail: StateFlow<OrderDetailUiState> = _orderDetail.asStateFlow()

    init {
        if (orderId != null) {
            loadOrderDetail(orderId)
        } else {
            loadOrders()
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            _ordersState.value = OrdersUiState.Loading
            when (val result = orderRepository.getOrders()) {
                is Resource.Success -> {
                    _ordersState.value = OrdersUiState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _ordersState.value = OrdersUiState.Error(result.message ?: "Failed to load orders")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadOrderDetail(id: String) {
        viewModelScope.launch {
            _orderDetail.value = OrderDetailUiState.Loading
            when (val result = orderRepository.getOrder(id)) {
                is Resource.Success -> {
                    result.data?.let {
                        _orderDetail.value = OrderDetailUiState.Success(it)
                    }
                }
                is Resource.Error -> {
                    _orderDetail.value = OrderDetailUiState.Error(result.message ?: "Failed to load order")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun cancelOrder(id: String) {
        viewModelScope.launch {
            when (val result = orderRepository.cancelOrder(id)) {
                is Resource.Success -> {
                    result.data?.let {
                        _orderDetail.value = OrderDetailUiState.Success(it)
                    }
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }
}

sealed class OrdersUiState {
    object Loading : OrdersUiState()
    data class Success(val orders: List<Order>) : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}

sealed class OrderDetailUiState {
    object Loading : OrderDetailUiState()
    data class Success(val order: Order) : OrderDetailUiState()
    data class Error(val message: String) : OrderDetailUiState()
}
