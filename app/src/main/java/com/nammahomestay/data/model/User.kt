package com.nammahomestay.data.model

data class User(
    val uid: String = "",
    val phone: String = "",
    val name: String = "",
    val role: String = "",  // "guest", "host", "admin"
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "phone" to phone,
        "name" to name,
        "role" to role,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): User = User(
            uid = map["uid"] as? String ?: "",
            phone = map["phone"] as? String ?: "",
            name = map["name"] as? String ?: "",
            role = map["role"] as? String ?: "",
            createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
        )
    }
}
