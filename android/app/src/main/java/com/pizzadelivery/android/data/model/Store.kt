package com.pizzadelivery.android.data.model

import com.google.gson.annotations.SerializedName

data class Store(
    val id: String = "",
    val name: String = "",
    val address: StoreAddress = StoreAddress(),
    val location: Location = Location(),
    val hours: StoreHours? = null,
    @SerializedName("isOpen")
    val isOpen: Boolean = false,
    val rating: Double = 0.0,
    val phone: String = "",
    val image: String = "",
    val distance: Double? = null
)

data class StoreAddress(
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = ""
)

data class Location(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

data class StoreHours(
    val monday: DayHours? = null,
    val tuesday: DayHours? = null,
    val wednesday: DayHours? = null,
    val thursday: DayHours? = null,
    val friday: DayHours? = null,
    val saturday: DayHours? = null,
    val sunday: DayHours? = null
)

data class DayHours(
    val open: String = "",
    val close: String = ""
)
