package com.pizzadelivery.android.data.model

data class Recommendation(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val items: List<MenuItem> = emptyList(),
    val type: RecommendationType = RecommendationType.PERSONALIZED
)

enum class RecommendationType {
    PERSONALIZED,
    TRENDING,
    NEW_ARRIVALS,
    OFFERS
}

data class Offer(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val code: String = "",
    val discount: Double = 0.0,
    val discountType: String = "percentage",
    val image: String = "",
    val validUntil: String = ""
)
