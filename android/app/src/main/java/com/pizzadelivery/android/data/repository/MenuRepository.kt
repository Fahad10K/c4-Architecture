package com.pizzadelivery.android.data.repository

import com.pizzadelivery.android.data.api.ApiService
import com.pizzadelivery.android.data.model.MenuCategory
import com.pizzadelivery.android.data.model.MenuItem
import com.pizzadelivery.android.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getMenu(storeId: String): Resource<List<MenuItem>> {
        return try {
            val response = apiService.getMenu(storeId)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error("Failed to fetch menu: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getMenuItem(id: String): Resource<MenuItem> {
        return try {
            val response = apiService.getMenuItem(id)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Menu item not found")
            } else {
                Resource.Error("Failed to fetch menu item: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getCategories(): Resource<List<MenuCategory>> {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error("Failed to fetch categories: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}
