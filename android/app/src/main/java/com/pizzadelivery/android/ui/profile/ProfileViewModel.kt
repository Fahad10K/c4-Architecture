package com.pizzadelivery.android.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzadelivery.android.data.model.Address
import com.pizzadelivery.android.data.model.User
import com.pizzadelivery.android.data.repository.AccountRepository
import com.pizzadelivery.android.data.repository.AuthRepository
import com.pizzadelivery.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    private val _addressesState = MutableStateFlow<AddressesUiState>(AddressesUiState.Loading)
    val addressesState: StateFlow<AddressesUiState> = _addressesState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            when (val result = accountRepository.getProfile()) {
                is Resource.Success -> {
                    result.data?.let {
                        _profileState.value = ProfileUiState.Success(it)
                    }
                }
                is Resource.Error -> {
                    _profileState.value = ProfileUiState.Error(result.message ?: "Failed to load profile")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            when (val result = accountRepository.updateProfile(user)) {
                is Resource.Success -> {
                    result.data?.let {
                        _profileState.value = ProfileUiState.Success(it)
                    }
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _addressesState.value = AddressesUiState.Loading
            when (val result = accountRepository.getAddresses()) {
                is Resource.Success -> {
                    _addressesState.value = AddressesUiState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _addressesState.value = AddressesUiState.Error(result.message ?: "Failed to load")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun addAddress(address: Address) {
        viewModelScope.launch {
            when (val result = accountRepository.addAddress(address)) {
                is Resource.Success -> { loadAddresses() }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteAddress(id: String) {
        viewModelScope.launch {
            when (accountRepository.deleteAddress(id)) {
                is Resource.Success -> { loadAddresses() }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class AddressesUiState {
    object Loading : AddressesUiState()
    data class Success(val addresses: List<Address>) : AddressesUiState()
    data class Error(val message: String) : AddressesUiState()
}
