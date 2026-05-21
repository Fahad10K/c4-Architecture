package com.pizzadelivery.android.data.model

data class Cart(
    val id: String = "",
    val userId: String = "",
    val storeId: String = "",
    val items: List<CartItem> = emptyList(),
    val couponCode: String? = null,
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0
)

data class CartItem(
    val id: String = "",
    val menuItemId: String = "",
    val name: String = "",
    val image: String = "",
    val quantity: Int = 1,
    val customizations: List<SelectedCustomization> = emptyList(),
    val price: Double = 0.0,
    val totalPrice: Double = 0.0
)

data class SelectedCustomization(
    val customizationId: String = "",
    val optionIds: List<String> = emptyList()
)

data class AddToCartRequest(
    val menuItemId: String,
    val quantity: Int = 1,
    val customizations: List<SelectedCustomization> = emptyList()
)

data class UpdateCartItemRequest(
    val quantity: Int
)

data class ApplyCouponRequest(
    val couponCode: String
)

data class CouponResponse(
    val valid: Boolean,
    val discount: Double = 0.0,
    val message: String = ""
)
