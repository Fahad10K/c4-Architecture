package com.pizzadelivery.android.data.repository

import com.pizzadelivery.android.data.api.ApiService
import com.pizzadelivery.android.data.model.*
import com.pizzadelivery.android.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun createOrder(request: CreateOrderRequest): Resource<Order> {
        return try {
            val response = apiService.createOrder(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Order creation failed")
            } else {
                Resource.Error("Failed to create order: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getOrders(): Resource<List<Order>> {
        return try {
            val response = apiService.getOrders()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error("Failed to fetch orders: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getOrder(id: String): Resource<Order> {
        return try {
            val response = apiService.getOrder(id)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Order not found")
            } else {
                Resource.Error("Failed to fetch order: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun cancelOrder(id: String): Resource<Order> {
        return try {
            val response = apiService.cancelOrder(id)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Cancellation failed")
            } else {
                Resource.Error("Failed to cancel order: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun trackDelivery(orderId: String): Resource<DeliveryInfo> {
        return try {
            val response = apiService.trackDelivery(orderId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Tracking info not available")
            } else {
                Resource.Error("Failed to track delivery: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}
