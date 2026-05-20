package com.pizzadelivery.android.data.model

data class SearchResult(
    val items: List<MenuItem> = emptyList(),
    val stores: List<Store> = emptyList(),
    val totalResults: Int = 0
)
