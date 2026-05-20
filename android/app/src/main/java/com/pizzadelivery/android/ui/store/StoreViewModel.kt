package com.pizzadelivery.android.ui.store

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzadelivery.android.data.model.Store
import com.pizzadelivery.android.data.repository.StoreRepository
import com.pizzadelivery.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _storesState = MutableStateFlow<StoresUiState>(StoresUiState.Loading)
    val storesState: StateFlow<StoresUiState> = _storesState.asStateFlow()

    private val _storeDetail = MutableStateFlow<StoreDetailUiState>(StoreDetailUiState.Loading)
    val storeDetail: StateFlow<StoreDetailUiState> = _storeDetail.asStateFlow()

    private val storeId: String? = savedStateHandle["storeId"]

    init {
        if (storeId != null) {
            loadStoreDetail(storeId)
        } else {
            loadStores()
        }
    }

    fun loadStores() {
        viewModelScope.launch {
            _storesState.value = StoresUiState.Loading
            when (val result = storeRepository.getStores()) {
                is Resource.Success -> {
                    _storesState.value = StoresUiState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _storesState.value = StoresUiState.Error(result.message ?: "Failed to load stores")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadStoreDetail(id: String) {
        viewModelScope.launch {
            _storeDetail.value = StoreDetailUiState.Loading
            when (val result = storeRepository.getStore(id)) {
                is Resource.Success -> {
                    result.data?.let {
                        _storeDetail.value = StoreDetailUiState.Success(it)
                    }
                }
                is Resource.Error -> {
                    _storeDetail.value = StoreDetailUiState.Error(result.message ?: "Failed to load store")
                }
                is Resource.Loading -> {}
            }
        }
    }
}

sealed class StoresUiState {
    object Loading : StoresUiState()
    data class Success(val stores: List<Store>) : StoresUiState()
    data class Error(val message: String) : StoresUiState()
}

sealed class StoreDetailUiState {
    object Loading : StoreDetailUiState()
    data class Success(val store: Store) : StoreDetailUiState()
    data class Error(val message: String) : StoreDetailUiState()
}
