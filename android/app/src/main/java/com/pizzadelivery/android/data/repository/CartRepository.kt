package com.pizzadelivery.android.data.repository

import com.pizzadelivery.android.data.api.ApiService
import com.pizzadelivery.android.data.model.*
import com.pizzadelivery.android.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getCart(): Resource<Cart> {
        return try {
            val response = apiService.getCart()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: Cart())
            } else {
                Resource.Error("Failed to fetch cart: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun addToCart(menuItemId: String, quantity: Int, customizations: List<SelectedCustomization>): Resource<Cart> {
        return try {
            val response = apiService.addToCart(
                AddToCartRequest(menuItemId = menuItemId, quantity = quantity, customizations = customizations)
            )
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: Cart())
            } else {
                Resource.Error("Failed to add to cart: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun updateCartItem(itemId: String, quantity: Int): Resource<Cart> {
        return try {
            val response = apiService.updateCartItem(itemId, UpdateCartItemRequest(quantity))
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: Cart())
            } else {
                Resource.Error("Failed to update cart: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun removeFromCart(itemId: String): Resource<Cart> {
        return try {
            val response = apiService.removeFromCart(itemId)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: Cart())
            } else {
                Resource.Error("Failed to remove from cart: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun clearCart(): Resource<Unit> {
        return try {
            val response = apiService.clearCart()
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to clear cart: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun applyCoupon(code: String): Resource<CouponResponse> {
        return try {
            val response = apiService.applyCoupon(ApplyCouponRequest(code))
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Invalid coupon")
            } else {
                Resource.Error("Coupon application failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}
