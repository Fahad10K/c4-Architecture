package com.pizzadelivery.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzadelivery.android.data.repository.AuthRepository
import com.pizzadelivery.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val isLoggedIn: Flow<Boolean> = authRepository.isLoggedIn

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState> = _registerState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            when (val result = authRepository.login(email, password)) {
                is Resource.Success -> {
                    _loginState.value = AuthUiState.Success
                }
                is Resource.Error -> {
                    _loginState.value = AuthUiState.Error(result.message ?: "Login failed")
                }
                is Resource.Loading -> {
                    _loginState.value = AuthUiState.Loading
                }
            }
        }
    }

    fun register(name: String, email: String, password: String, phone: String) {
        viewModelScope.launch {
            _registerState.value = AuthUiState.Loading
            when (val result = authRepository.register(name, email, password, phone)) {
                is Resource.Success -> {
                    _registerState.value = AuthUiState.Success
                }
                is Resource.Error -> {
                    _registerState.value = AuthUiState.Error(result.message ?: "Registration failed")
                }
                is Resource.Loading -> {
                    _registerState.value = AuthUiState.Loading
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun resetLoginState() {
        _loginState.value = AuthUiState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
