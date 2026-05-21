package com.pizzadelivery.android.data.model

import com.google.gson.annotations.SerializedName

data class Order(
    val id: String = "",
    val userId: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val items: List<OrderItem> = emptyList(),
    val status: OrderStatus = OrderStatus.PLACED,
    val payment: PaymentInfo? = null,
    val delivery: DeliveryInfo? = null,
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class OrderItem(
    val menuItemId: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val customizations: List<SelectedCustomization> = emptyList()
)

enum class OrderStatus {
    @SerializedName("placed") PLACED,
    @SerializedName("confirmed") CONFIRMED,
    @SerializedName("preparing") PREPARING,
    @SerializedName("ready") READY,
    @SerializedName("picked_up") PICKED_UP,
    @SerializedName("on_the_way") ON_THE_WAY,
    @SerializedName("delivered") DELIVERED,
    @SerializedName("cancelled") CANCELLED
}

data class PaymentInfo(
    val id: String = "",
    val method: String = "",
    val status: String = "",
    val transactionId: String = ""
)

data class DeliveryInfo(
    val driverId: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val status: DeliveryStatus = DeliveryStatus.ASSIGNED,
    val currentLocation: Location? = null,
    val eta: Int? = null,
    val route: List<Location> = emptyList()
)

enum class DeliveryStatus {
    @SerializedName("assigned") ASSIGNED,
    @SerializedName("picked_up") PICKED_UP,
    @SerializedName("on_the_way") ON_THE_WAY,
    @SerializedName("delivered") DELIVERED
}

data class CreateOrderRequest(
    val storeId: String,
    val items: List<OrderItem>,
    val deliveryAddressId: String,
    val paymentMethodId: String,
    val couponCode: String? = null
)
