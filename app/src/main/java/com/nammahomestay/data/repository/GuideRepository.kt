package com.nammahomestay.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nammahomestay.data.model.GuidePlace
import com.nammahomestay.utils.Constants
import kotlinx.coroutines.tasks.await

class GuideRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addPlace(place: GuidePlace): Result<Unit> {
        return try {
            val docRef = firestore.collection(Constants.GUIDE_PLACES_COLLECTION).document()
            docRef.set(place.copy(id = docRef.id).toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlacesForHomeStay(homestayId: String): Result<List<GuidePlace>> {
        return try {
            val snapshot = firestore.collection(Constants.GUIDE_PLACES_COLLECTION)
                .whereEqualTo("homestayId", homestayId)
                .get()
                .await()
            val places = snapshot.documents.mapNotNull { doc ->
                GuidePlace.fromMap(doc.data ?: return@mapNotNull null)
            }
            Result.success(places)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePlace(id: String): Result<Unit> {
        return try {
            firestore.collection(Constants.GUIDE_PLACES_COLLECTION)
                .document(id)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
