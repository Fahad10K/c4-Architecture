package com.pizzadelivery.android.data.repository

import com.pizzadelivery.android.data.api.ApiService
import com.pizzadelivery.android.data.model.SearchResult
import com.pizzadelivery.android.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun search(query: String): Resource<SearchResult> {
        return try {
            val response = apiService.search(query)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: SearchResult())
            } else {
                Resource.Error("Search failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}
