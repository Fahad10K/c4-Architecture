package com.pizzadelivery.android.data.repository

import com.pizzadelivery.android.data.api.ApiService
import com.pizzadelivery.android.data.model.Address
import com.pizzadelivery.android.data.model.User
import com.pizzadelivery.android.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getProfile(): Resource<User> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Profile not found")
            } else {
                Resource.Error("Failed to fetch profile: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun updateProfile(user: User): Resource<User> {
        return try {
            val response = apiService.updateProfile(user)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Update failed")
            } else {
                Resource.Error("Failed to update profile: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getAddresses(): Resource<List<Address>> {
        return try {
            val response = apiService.getAddresses()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error("Failed to fetch addresses: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun addAddress(address: Address): Resource<Address> {
        return try {
            val response = apiService.addAddress(address)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Failed to add address")
            } else {
                Resource.Error("Failed to add address: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun updateAddress(id: String, address: Address): Resource<Address> {
        return try {
            val response = apiService.updateAddress(id, address)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Failed to update address")
            } else {
                Resource.Error("Failed to update address: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun deleteAddress(id: String): Resource<Unit> {
        return try {
            val response = apiService.deleteAddress(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to delete address: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}
