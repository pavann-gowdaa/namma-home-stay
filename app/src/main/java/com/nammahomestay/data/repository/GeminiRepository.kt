package com.nammahomestay.data.repository

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.nammahomestay.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun generateFoodSuggestions(
        location: String = "Karnataka",
        mealType: String = "all"
    ): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    Suggest 5 traditional $mealType food items from $location region for a homestay menu.
                    Return ONLY a JSON array of strings with dish names.
                    Example: ["Dish 1", "Dish 2", "Dish 3", "Dish 4", "Dish 5"]
                    Only include authentic local dishes.
                """.trimIndent()

                val suggestions = callGeminiApi(prompt)
                val dishes = parseJsonArray(suggestions)
                Result.success(dishes)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun generateMenuDescription(dishes: List<String>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    Write a brief, appetizing description (2-3 sentences) for a homestay dinner menu
                    featuring these dishes: ${dishes.joinToString(", ")}.
                    Make it sound warm and inviting, like a rural Karnataka homestay experience.
                """.trimIndent()

                val description = callGeminiApi(prompt)
                Result.success(description.trim())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun generateMenuIdeas(location: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    You are a chef specializing in Karnataka rural cuisine. 
                    Create a complete one-day meal plan for a homestay in $location.
                    Include breakfast, lunch, and dinner with 3-4 items each.
                    Format as:
                    BREAKFAST:
                    - item 1
                    - item 2
                    LUNCH:
                    - item 1
                    - item 2
                    DINNER:
                    - item 1
                    - item 2
                    Use only authentic local Karnataka dishes.
                """.trimIndent()

                val response = callGeminiApi(prompt)
                Result.success(response.trim())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun callGeminiApi(prompt: String): String {
        val url = "${Constants.GEMINI_BASE_URL}models/gemini-pro:generateContent?key=${Constants.GEMINI_API_KEY}"

        val requestBody = """
            {
                "contents": [{
                    "parts": [{"text": ${gson.toJson(prompt)}}]
                }],
                "generationConfig": {
                    "temperature": 0.7,
                    "maxOutputTokens": 1024
                }
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        val jsonResponse = gson.fromJson(responseBody, GeminiResponse::class.java)
        return jsonResponse.candidates?.firstOrNull()
            ?.content?.parts?.firstOrNull()
            ?.text ?: throw Exception("Invalid response format")
    }

    private fun parseJsonArray(json: String): List<String> {
        return try {
            val cleaned = json.trim()
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```").trim()
            gson.fromJson(cleaned, Array<String>::class.java).toList()
        } catch (e: Exception) {
            json.split("\n")
                .map { it.trim().removePrefix("-").trim() }
                .filter { it.isNotBlank() }
        }
    }

    data class GeminiResponse(
        val candidates: List<Candidate>? = null
    )

    data class Candidate(
        val content: Content? = null
    )

    data class Content(
        val parts: List<Part>? = null
    )

    data class Part(
        val text: String? = null
    )
}
