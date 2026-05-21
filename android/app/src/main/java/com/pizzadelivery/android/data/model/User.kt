package com.pizzadelivery.android.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val preferences: UserPreferences? = null,
    val addresses: List<Address> = emptyList(),
    @SerializedName("createdAt")
    val createdAt: String = ""
)

enum class UserRole {
    @SerializedName("customer") CUSTOMER,
    @SerializedName("admin") ADMIN,
    @SerializedName("store_staff") STORE_STAFF,
    @SerializedName("delivery_partner") DELIVERY_PARTNER
}

data class UserPreferences(
    val favoriteStores: List<String> = emptyList(),
    val dietaryRestrictions: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true
)

data class Address(
    val id: String = "",
    val label: String = "",
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val isDefault: Boolean = false
)

data class AuthRequest(
    val email: String,
    val password: String,
    val name: String? = null,
    val phone: String? = null
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

data class TokenRefreshRequest(
    val refreshToken: String
)

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String
)
