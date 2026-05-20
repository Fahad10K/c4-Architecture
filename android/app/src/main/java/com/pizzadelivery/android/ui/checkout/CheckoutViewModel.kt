package com.pizzadelivery.android.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzadelivery.android.data.model.*
import com.pizzadelivery.android.data.repository.AccountRepository
import com.pizzadelivery.android.data.repository.CartRepository
import com.pizzadelivery.android.data.repository.OrderRepository
import com.pizzadelivery.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _checkoutState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Loading)
    val checkoutState: StateFlow<CheckoutUiState> = _checkoutState.asStateFlow()

    private val _placeOrderState = MutableStateFlow<PlaceOrderState>(PlaceOrderState.Idle)
    val placeOrderState: StateFlow<PlaceOrderState> = _placeOrderState.asStateFlow()

    init {
        loadCheckoutData()
    }

    private fun loadCheckoutData() {
        viewModelScope.launch {
            _checkoutState.value = CheckoutUiState.Loading
            val cartResult = cartRepository.getCart()
            val addressResult = accountRepository.getAddresses()

            if (cartResult is Resource.Success && addressResult is Resource.Success) {
                _checkoutState.value = CheckoutUiState.Ready(
                    cart = cartResult.data ?: Cart(),
                    addresses = addressResult.data ?: emptyList(),
                    selectedAddressId = addressResult.data?.firstOrNull { it.isDefault }?.id ?: addressResult.data?.firstOrNull()?.id,
                    selectedPaymentMethod = "card"
                )
            } else {
                _checkoutState.value = CheckoutUiState.Error("Failed to load checkout data")
            }
        }
    }

    fun selectAddress(addressId: String) {
        val current = _checkoutState.value
        if (current is CheckoutUiState.Ready) {
            _checkoutState.value = current.copy(selectedAddressId = addressId)
        }
    }

    fun selectPaymentMethod(method: String) {
        val current = _checkoutState.value
        if (current is CheckoutUiState.Ready) {
            _checkoutState.value = current.copy(selectedPaymentMethod = method)
        }
    }

    fun placeOrder() {
        val current = _checkoutState.value
        if (current !is CheckoutUiState.Ready) return

        viewModelScope.launch {
            _placeOrderState.value = PlaceOrderState.Loading
            val request = CreateOrderRequest(
                storeId = current.cart.storeId,
                items = current.cart.items.map {
                    OrderItem(
                        menuItemId = it.menuItemId,
                        name = it.name,
                        quantity = it.quantity,
                        price = it.price,
                        customizations = it.customizations
                    )
                },
                deliveryAddressId = current.selectedAddressId ?: "",
                paymentMethodId = current.selectedPaymentMethod,
                couponCode = current.cart.couponCode
            )

            when (val result = orderRepository.createOrder(request)) {
                is Resource.Success -> {
                    result.data?.let {
                        _placeOrderState.value = PlaceOrderState.Success(it.id)
                    }
                }
                is Resource.Error -> {
                    _placeOrderState.value = PlaceOrderState.Error(result.message ?: "Order failed")
                }
                is Resource.Loading -> {}
            }
        }
    }
}

sealed class CheckoutUiState {
    object Loading : CheckoutUiState()
    data class Ready(
        val cart: Cart,
        val addresses: List<Address>,
        val selectedAddressId: String?,
        val selectedPaymentMethod: String
    ) : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
}

sealed class PlaceOrderState {
    object Idle : PlaceOrderState()
    object Loading : PlaceOrderState()
    data class Success(val orderId: String) : PlaceOrderState()
    data class Error(val message: String) : PlaceOrderState()
}
