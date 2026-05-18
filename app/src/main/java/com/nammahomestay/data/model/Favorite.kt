package com.nammahomestay.data.model

data class Favorite(
    val id: String = "",
    val userId: String = "",
    val homestayId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "homestayId" to homestayId,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Favorite = Favorite(
            id = map["id"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            homestayId = map["homestayId"] as? String ?: "",
            createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
        )
    }
}