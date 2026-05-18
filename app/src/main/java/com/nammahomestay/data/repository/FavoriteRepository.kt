package com.nammahomestay.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nammahomestay.data.model.Favorite
import com.nammahomestay.utils.Constants
import kotlinx.coroutines.tasks.await

class FavoriteRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getFavorites(userId: String): Result<List<Favorite>> {
        return try {
            val snapshot = firestore.collection(Constants.FAVORITES_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val favorites = snapshot.documents.mapNotNull { doc ->
                Favorite.fromMap(doc.data ?: return@mapNotNull null)
            }
            Result.success(favorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isFavorite(userId: String, homestayId: String): Boolean {
        return try {
            val snapshot = firestore.collection(Constants.FAVORITES_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("homestayId", homestayId)
                .limit(1)
                .get()
                .await()
            snapshot.documents.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleFavorite(userId: String, homestayId: String): Result<Boolean> {
        return try {
            val snapshot = firestore.collection(Constants.FAVORITES_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("homestayId", homestayId)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                snapshot.documents.first().reference.delete().await()
                Result.success(false) // removed
            } else {
                val docRef = firestore.collection(Constants.FAVORITES_COLLECTION).document()
                val favorite = Favorite(
                    id = docRef.id,
                    userId = userId,
                    homestayId = homestayId
                )
                docRef.set(favorite.toMap()).await()
                Result.success(true) // added
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}