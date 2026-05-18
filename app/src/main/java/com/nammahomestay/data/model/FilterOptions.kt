package com.nammahomestay.data.model

data class FilterOptions(
    val location: String = "",
    val minPrice: Double = 0.0,
    val maxPrice: Double = Double.MAX_VALUE,
    val availabilityOnly: Boolean = false,
    val verifiedOnly: Boolean = false
)
