package com.pizzadelivery.android.data.model

import com.google.gson.annotations.SerializedName

data class MenuItem(
    val id: String = "",
    val storeId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val image: String = "",
    val customizations: List<Customization> = emptyList(),
    @SerializedName("isAvailable")
    val isAvailable: Boolean = true,
    val rating: Double = 0.0,
    val calories: Int? = null
)

data class Customization(
    val id: String = "",
    val name: String = "",
    val type: CustomizationType = CustomizationType.SINGLE,
    val required: Boolean = false,
    val options: List<CustomizationOption> = emptyList()
)

enum class CustomizationType {
    @SerializedName("single") SINGLE,
    @SerializedName("multiple") MULTIPLE
}

data class CustomizationOption(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0
)

data class MenuCategory(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val image: String = "",
    val itemCount: Int = 0
)
