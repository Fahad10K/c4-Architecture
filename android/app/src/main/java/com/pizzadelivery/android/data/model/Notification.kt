package com.pizzadelivery.android.data.model

import com.google.gson.annotations.SerializedName

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.ORDER_UPDATE,
    val data: Map<String, String>? = null,
    @SerializedName("isRead")
    val isRead: Boolean = false,
    val createdAt: String = ""
)

enum class NotificationType {
    @SerializedName("order_update") ORDER_UPDATE,
    @SerializedName("promotion") PROMOTION,
    @SerializedName("delivery_update") DELIVERY_UPDATE,
    @SerializedName("system") SYSTEM
}
