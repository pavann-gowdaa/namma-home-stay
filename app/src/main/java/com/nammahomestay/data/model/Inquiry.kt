package com.nammahomestay.data.model

data class Inquiry(
    val id: String = "",
    val homestayId: String = "",
    val hostId: String = "",
    val guestId: String = "",
    val guestName: String = "",
    val guestPhone: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "homestayId" to homestayId,
        "hostId" to hostId,
        "guestId" to guestId,
        "guestName" to guestName,
        "guestPhone" to guestPhone,
        "message" to message,
        "timestamp" to timestamp
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Inquiry = Inquiry(
            id = map["id"] as? String ?: "",
            homestayId = map["homestayId"] as? String ?: "",
            hostId = map["hostId"] as? String ?: "",
            guestId = map["guestId"] as? String ?: "",
            guestName = map["guestName"] as? String ?: "",
            guestPhone = map["guestPhone"] as? String ?: "",
            message = map["message"] as? String ?: "",
            timestamp = map["timestamp"] as? Long ?: System.currentTimeMillis()
        )
    }
}
