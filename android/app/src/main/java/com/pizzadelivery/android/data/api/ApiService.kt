package com.pizzadelivery.android.data.api

import com.pizzadelivery.android.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): Response<TokenRefreshResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    // Account
    @GET("accounts/profile")
    suspend fun getProfile(): Response<User>

    @PUT("accounts/profile")
    suspend fun updateProfile(@Body user: User): Response<User>

    @GET("accounts/addresses")
    suspend fun getAddresses(): Response<List<Address>>

    @POST("accounts/addresses")
    suspend fun addAddress(@Body address: Address): Response<Address>

    @PUT("accounts/addresses/{id}")
    suspend fun updateAddress(@Path("id") id: String, @Body address: Address): Response<Address>

    @DELETE("accounts/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): Response<Unit>

    // Stores
    @GET("stores")
    suspend fun getStores(): Response<List<Store>>

    @GET("stores/{id}")
    suspend fun getStore(@Path("id") id: String): Response<Store>

    @GET("stores/nearby")
    suspend fun getNearbyStores(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Double = 10.0
    ): Response<List<Store>>

    // Menu
    @GET("stores/{storeId}/menu")
    suspend fun getMenu(@Path("storeId") storeId: String): Response<List<MenuItem>>

    @GET("menu/items/{id}")
    suspend fun getMenuItem(@Path("id") id: String): Response<MenuItem>

    @GET("menu/categories")
    suspend fun getCategories(): Response<List<MenuCategory>>

    // Cart
    @GET("cart")
    suspend fun getCart(): Response<Cart>

    @POST("cart")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<Cart>

    @PUT("cart/{itemId}")
    suspend fun updateCartItem(
        @Path("itemId") itemId: String,
        @Body request: UpdateCartItemRequest
    ): Response<Cart>

    @DELETE("cart/{itemId}")
    suspend fun removeFromCart(@Path("itemId") itemId: String): Response<Cart>

    @DELETE("cart")
    suspend fun clearCart(): Response<Unit>

    @POST("cart/apply-coupon")
    suspend fun applyCoupon(@Body request: ApplyCouponRequest): Response<CouponResponse>

    // Orders
    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<Order>

    @GET("orders")
    suspend fun getOrders(): Response<List<Order>>

    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: String): Response<Order>

    @PUT("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): Response<Order>

    // Payment
    @POST("payments")
    suspend fun createPayment(@Body request: CreatePaymentRequest): Response<PaymentResponse>

    @GET("payments/{id}")
    suspend fun getPayment(@Path("id") id: String): Response<PaymentResponse>

    // Delivery Tracking
    @GET("delivery/{orderId}/track")
    suspend fun trackDelivery(@Path("orderId") orderId: String): Response<DeliveryInfo>

    // Notifications
    @GET("notifications")
    suspend fun getNotifications(): Response<List<Notification>>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<Unit>

    // Search
    @GET("search")
    suspend fun search(@Query("q") query: String): Response<SearchResult>

    // Chatbot
    @POST("chatbot/message")
    suspend fun sendChatMessage(@Body request: ChatRequest): Response<ChatResponse>

    // Recommendations
    @GET("recommendations")
    suspend fun getRecommendations(): Response<List<Recommendation>>

    @GET("recommendations/offers")
    suspend fun getOffers(): Response<List<Offer>>

    // Admin
    @GET("admin/orders")
    suspend fun getAdminOrders(): Response<List<Order>>

    @GET("admin/analytics")
    suspend fun getAnalytics(): Response<Map<String, Any>>
}
