package com.pizzadelivery.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzadelivery.android.data.model.Offer
import com.pizzadelivery.android.data.model.Recommendation
import com.pizzadelivery.android.data.model.Store
import com.pizzadelivery.android.data.repository.RecommendationRepository
import com.pizzadelivery.android.data.repository.StoreRepository
import com.pizzadelivery.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Load stores
            launch {
                when (val result = storeRepository.getStores()) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            nearbyStores = result.data?.take(5) ?: emptyList()
                        )
                    }
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                }
            }

            // Load recommendations
            launch {
                when (val result = recommendationRepository.getRecommendations()) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            recommendations = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                }
            }

            // Load offers
            launch {
                when (val result = recommendationRepository.getOffers()) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            offers = result.data ?: emptyList(),
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val nearbyStores: List<Store> = emptyList(),
    val recommendations: List<Recommendation> = emptyList(),
    val offers: List<Offer> = emptyList()
)
