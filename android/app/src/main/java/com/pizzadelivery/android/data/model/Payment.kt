package com.pizzadelivery.android.data.model

import com.google.gson.annotations.SerializedName

data class PaymentMethod(
    val id: String = "",
    val type: PaymentType = PaymentType.CARD,
    val last4: String = "",
    val brand: String = "",
    val expiryMonth: Int = 0,
    val expiryYear: Int = 0,
    @SerializedName("isDefault")
    val isDefault: Boolean = false
)

enum class PaymentType {
    @SerializedName("card") CARD,
    @SerializedName("cash") CASH,
    @SerializedName("wallet") WALLET
}

data class CreatePaymentRequest(
    val orderId: String,
    val paymentMethodId: String,
    val amount: Double
)

data class PaymentResponse(
    val id: String,
    val status: String,
    val transactionId: String,
    val clientSecret: String? = null
)
