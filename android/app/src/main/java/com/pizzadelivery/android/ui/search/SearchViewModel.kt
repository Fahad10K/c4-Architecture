package com.pizzadelivery.android.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzadelivery.android.data.model.SearchResult
import com.pizzadelivery.android.data.repository.SearchRepository
import com.pizzadelivery.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _searchState.value = SearchUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _searchState.value = SearchUiState.Loading
            when (val result = searchRepository.search(newQuery)) {
                is Resource.Success -> {
                    _searchState.value = SearchUiState.Success(result.data ?: SearchResult())
                }
                is Resource.Error -> {
                    _searchState.value = SearchUiState.Error(result.message ?: "Search failed")
                }
                is Resource.Loading -> {}
            }
        }
    }
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val result: SearchResult) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
