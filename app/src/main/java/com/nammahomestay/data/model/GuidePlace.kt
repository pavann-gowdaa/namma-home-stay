package com.nammahomestay.data.model

data class GuidePlace(
    val id: String = "",
    val homestayId: String = "",
    val name: String = "",
    val distance: String = "",  // e.g., "2.5 km"
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val category: String = "",  // "temple", "waterfall", "trek", "restaurant", etc.
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "homestayId" to homestayId,
        "name" to name,
        "distance" to distance,
        "description" to description,
        "latitude" to latitude,
        "longitude" to longitude,
        "category" to category,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): GuidePlace = GuidePlace(
            id = map["id"] as? String ?: "",
            homestayId = map["homestayId"] as? String ?: "",
            name = map["name"] as? String ?: "",
            distance = map["distance"] as? String ?: "",
            description = map["description"] as? String ?: "",
            latitude = map["latitude"] as? Double ?: 0.0,
            longitude = map["longitude"] as? Double ?: 0.0,
            category = map["category"] as? String ?: "",
            createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
        )
    }
}
