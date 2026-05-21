package com.pizzadelivery.android.data.repository

import com.pizzadelivery.android.data.api.ApiService
import com.pizzadelivery.android.data.local.TokenManager
import com.pizzadelivery.android.data.model.AuthRequest
import com.pizzadelivery.android.data.model.AuthResponse
import com.pizzadelivery.android.data.model.TokenRefreshRequest
import com.pizzadelivery.android.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    val isLoggedIn: Flow<Boolean> = tokenManager.isLoggedIn
    val userRole: Flow<String?> = tokenManager.userRole

    suspend fun login(email: String, password: String): Resource<AuthResponse> {
        return try {
            val response = apiService.login(AuthRequest(email = email, password = password))
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                    tokenManager.saveUserInfo(authResponse.user.id, authResponse.user.role.name)
                    Resource.Success(authResponse)
                } ?: Resource.Error("Empty response body")
            } else {
                Resource.Error("Login failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun register(name: String, email: String, password: String, phone: String): Resource<AuthResponse> {
        return try {
            val response = apiService.register(
                AuthRequest(email = email, password = password, name = name, phone = phone)
            )
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                    tokenManager.saveUserInfo(authResponse.user.id, authResponse.user.role.name)
                    Resource.Success(authResponse)
                } ?: Resource.Error("Empty response body")
            } else {
                Resource.Error("Registration failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun refreshToken(): Resource<Unit> {
        return try {
            val refreshToken = kotlinx.coroutines.flow.first { true }.let {
                var token: String? = null
                tokenManager.refreshToken.collect { token = it }
                token
            }
            refreshToken?.let { rt ->
                val response = apiService.refreshToken(TokenRefreshRequest(rt))
                if (response.isSuccessful) {
                    response.body()?.let {
                        tokenManager.saveTokens(it.accessToken, it.refreshToken)
                        Resource.Success(Unit)
                    } ?: Resource.Error("Empty response")
                } else {
                    Resource.Error("Token refresh failed")
                }
            } ?: Resource.Error("No refresh token")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun logout() {
        try {
            apiService.logout()
        } catch (_: Exception) {}
        tokenManager.clearAll()
    }
}
