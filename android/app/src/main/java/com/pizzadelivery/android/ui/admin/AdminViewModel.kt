package com.pizzadelivery.android.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzadelivery.android.data.api.ApiService
import com.pizzadelivery.android.data.model.Order
import com.pizzadelivery.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _adminState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val adminState: StateFlow<AdminUiState> = _adminState.asStateFlow()

    init {
        loadAdminData()
    }

    fun loadAdminData() {
        viewModelScope.launch {
            _adminState.value = AdminUiState.Loading
            try {
                val ordersResponse = apiService.getAdminOrders()
                val analyticsResponse = apiService.getAnalytics()

                val orders = if (ordersResponse.isSuccessful) ordersResponse.body() ?: emptyList() else emptyList()
                val analytics = if (analyticsResponse.isSuccessful) analyticsResponse.body() ?: emptyMap() else emptyMap()

                _adminState.value = AdminUiState.Success(
                    orders = orders,
                    analytics = analytics,
                    totalOrders = orders.size,
                    totalRevenue = orders.sumOf { it.total },
                    pendingOrders = orders.count { it.status.name == "PLACED" || it.status.name == "CONFIRMED" }
                )
            } catch (e: Exception) {
                _adminState.value = AdminUiState.Error(e.message ?: "Failed to load admin data")
            }
        }
    }
}

sealed class AdminUiState {
    object Loading : AdminUiState()
    data class Success(
        val orders: List<Order>,
        val analytics: Map<String, Any>,
        val totalOrders: Int,
        val totalRevenue: Double,
        val pendingOrders: Int
    ) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}
