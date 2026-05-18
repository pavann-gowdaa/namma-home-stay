package com.nammahomestay.data.model

data class DailyMenu(
    val id: String = "",
    val homestayId: String = "",
    val date: String = "",  // format: yyyy-MM-dd
    val breakfast: List<String> = emptyList(),
    val lunch: List<String> = emptyList(),
    val dinner: List<String> = emptyList(),
    val description: String = "",
    val isAiGenerated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "homestayId" to homestayId,
        "date" to date,
        "breakfast" to breakfast,
        "lunch" to lunch,
        "dinner" to dinner,
        "description" to description,
        "isAiGenerated" to isAiGenerated,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): DailyMenu = DailyMenu(
            id = map["id"] as? String ?: "",
            homestayId = map["homestayId"] as? String ?: "",
            date = map["date"] as? String ?: "",
            breakfast = (map["breakfast"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            lunch = (map["lunch"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            dinner = (map["dinner"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            description = map["description"] as? String ?: "",
            isAiGenerated = map["isAiGenerated"] as? Boolean ?: false,
            createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
        )
    }
}
