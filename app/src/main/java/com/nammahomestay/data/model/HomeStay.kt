package com.nammahomestay.data.model

data class HomeStay(
    val id: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val hostPhone: String = "",
    val name: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val rate: Double = 0.0,
    val description: String = "",
    val photos: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val availability: Boolean = true,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "hostId" to hostId,
        "hostName" to hostName,
        "hostPhone" to hostPhone,
        "name" to name,
        "location" to location,
        "latitude" to latitude,
        "longitude" to longitude,
        "rate" to rate,
        "description" to description,
        "photos" to photos,
        "amenities" to amenities,
        "availability" to availability,
        "isVerified" to isVerified,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): HomeStay = HomeStay(
            id = map["id"] as? String ?: "",
            hostId = map["hostId"] as? String ?: "",
            hostName = map["hostName"] as? String ?: "",
            hostPhone = map["hostPhone"] as? String ?: "",
            name = map["name"] as? String ?: "",
            location = map["location"] as? String ?: "",
            latitude = map["latitude"] as? Double ?: 0.0,
            longitude = map["longitude"] as? Double ?: 0.0,
            rate = map["rate"] as? Double ?: 0.0,
            description = map["description"] as? String ?: "",
            photos = (map["photos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            amenities = (map["amenities"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            availability = map["availability"] as? Boolean ?: true,
            isVerified = map["isVerified"] as? Boolean ?: false,
            createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
        )
    }
}
