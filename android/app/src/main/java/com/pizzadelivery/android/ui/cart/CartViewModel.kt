package com.pizzadelivery.android.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzadelivery.android.data.model.Cart
import com.pizzadelivery.android.data.repository.CartRepository
import com.pizzadelivery.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val cartState: StateFlow<CartUiState> = _cartState.asStateFlow()

    private val _couponMessage = MutableStateFlow<String?>(null)
    val couponMessage: StateFlow<String?> = _couponMessage.asStateFlow()

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            _cartState.value = CartUiState.Loading
            when (val result = cartRepository.getCart()) {
                is Resource.Success -> {
                    _cartState.value = CartUiState.Success(result.data ?: Cart())
                }
                is Resource.Error -> {
                    _cartState.value = CartUiState.Error(result.message ?: "Failed to load cart")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch {
            when (val result = cartRepository.updateCartItem(itemId, quantity)) {
                is Resource.Success -> {
                    _cartState.value = CartUiState.Success(result.data ?: Cart())
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            when (val result = cartRepository.removeFromCart(itemId)) {
                is Resource.Success -> {
                    _cartState.value = CartUiState.Success(result.data ?: Cart())
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            when (cartRepository.clearCart()) {
                is Resource.Success -> {
                    _cartState.value = CartUiState.Success(Cart())
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun applyCoupon(code: String) {
        viewModelScope.launch {
            when (val result = cartRepository.applyCoupon(code)) {
                is Resource.Success -> {
                    result.data?.let {
                        _couponMessage.value = if (it.valid) "Coupon applied!" else it.message
                        loadCart()
                    }
                }
                is Resource.Error -> {
                    _couponMessage.value = result.message
                }
                is Resource.Loading -> {}
            }
        }
    }
}

sealed class CartUiState {
    object Loading : CartUiState()
    data class Success(val cart: Cart) : CartUiState()
    data class Error(val message: String) : CartUiState()
}
