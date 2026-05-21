package com.pizzadelivery.android.ui.menu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzadelivery.android.data.model.MenuItem
import com.pizzadelivery.android.data.repository.CartRepository
import com.pizzadelivery.android.data.repository.MenuRepository
import com.pizzadelivery.android.data.model.SelectedCustomization
import com.pizzadelivery.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: String = savedStateHandle["storeId"] ?: ""
    private val itemId: String? = savedStateHandle["itemId"]

    private val _menuState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val menuState: StateFlow<MenuUiState> = _menuState.asStateFlow()

    private val _itemDetail = MutableStateFlow<MenuItemDetailState>(MenuItemDetailState.Loading)
    val itemDetail: StateFlow<MenuItemDetailState> = _itemDetail.asStateFlow()

    private val _addToCartState = MutableStateFlow<AddToCartState>(AddToCartState.Idle)
    val addToCartState: StateFlow<AddToCartState> = _addToCartState.asStateFlow()

    init {
        if (itemId != null) {
            loadMenuItem(itemId)
        } else if (storeId.isNotEmpty()) {
            loadMenu()
        }
    }

    fun loadMenu() {
        viewModelScope.launch {
            _menuState.value = MenuUiState.Loading
            when (val result = menuRepository.getMenu(storeId)) {
                is Resource.Success -> {
                    val items = result.data ?: emptyList()
                    val grouped = items.groupBy { it.category }
                    _menuState.value = MenuUiState.Success(items, grouped)
                }
                is Resource.Error -> {
                    _menuState.value = MenuUiState.Error(result.message ?: "Failed to load menu")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadMenuItem(id: String) {
        viewModelScope.launch {
            _itemDetail.value = MenuItemDetailState.Loading
            when (val result = menuRepository.getMenuItem(id)) {
                is Resource.Success -> {
                    result.data?.let {
                        _itemDetail.value = MenuItemDetailState.Success(it)
                    }
                }
                is Resource.Error -> {
                    _itemDetail.value = MenuItemDetailState.Error(result.message ?: "Failed to load item")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun addToCart(menuItemId: String, quantity: Int, customizations: List<SelectedCustomization> = emptyList()) {
        viewModelScope.launch {
            _addToCartState.value = AddToCartState.Loading
            when (val result = cartRepository.addToCart(menuItemId, quantity, customizations)) {
                is Resource.Success -> {
                    _addToCartState.value = AddToCartState.Success
                }
                is Resource.Error -> {
                    _addToCartState.value = AddToCartState.Error(result.message ?: "Failed to add to cart")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun resetAddToCartState() {
        _addToCartState.value = AddToCartState.Idle
    }
}

sealed class MenuUiState {
    object Loading : MenuUiState()
    data class Success(
        val items: List<MenuItem>,
        val groupedItems: Map<String, List<MenuItem>>
    ) : MenuUiState()
    data class Error(val message: String) : MenuUiState()
}

sealed class MenuItemDetailState {
    object Loading : MenuItemDetailState()
    data class Success(val item: MenuItem) : MenuItemDetailState()
    data class Error(val message: String) : MenuItemDetailState()
}

sealed class AddToCartState {
    object Idle : AddToCartState()
    object Loading : AddToCartState()
    object Success : AddToCartState()
    data class Error(val message: String) : AddToCartState()
}
