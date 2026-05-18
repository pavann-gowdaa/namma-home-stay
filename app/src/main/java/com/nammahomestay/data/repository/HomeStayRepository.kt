package com.nammahomestay.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.nammahomestay.NammaHomeStayApp
import com.nammahomestay.data.model.FilterOptions
import com.nammahomestay.data.model.HomeStay
import com.nammahomestay.utils.Constants
import com.nammahomestay.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class HomeStayRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun getHomeStays(): Result<List<HomeStay>> {
        return try {
            val snapshot = firestore.collection(Constants.HOMESTAYS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val homestays = snapshot.documents.mapNotNull { doc ->
                HomeStay.fromMap(doc.data ?: return@mapNotNull null)
            }
            Result.success(homestays)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVerifiedHomeStays(): Result<List<HomeStay>> {
        return try {
            val snapshot = firestore.collection(Constants.HOMESTAYS_COLLECTION)
                .whereEqualTo("isVerified", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val homestays = snapshot.documents.mapNotNull { doc ->
                HomeStay.fromMap(doc.data ?: return@mapNotNull null)
            }
            Result.success(homestays)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHomeStaysByHost(hostId: String): Result<List<HomeStay>> {
        return try {
            val snapshot = firestore.collection(Constants.HOMESTAYS_COLLECTION)
                .whereEqualTo("hostId", hostId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val homestays = snapshot.documents.mapNotNull { doc ->
                HomeStay.fromMap(doc.data ?: return@mapNotNull null)
            }
            Result.success(homestays)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHomeStaysWithFilters(filters: FilterOptions): Result<List<HomeStay>> {
        return try {
            var query = firestore.collection(Constants.HOMESTAYS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING) as Query

            if (filters.location.isNotBlank()) {
                query = query.whereEqualTo("location", filters.location)
            }
            if (filters.availabilityOnly) {
                query = query.whereEqualTo("availability", true)
            }
            if (filters.verifiedOnly) {
                query = query.whereEqualTo("isVerified", true)
            }

            val snapshot = query.get().await()
            var homestays = snapshot.documents.mapNotNull { doc ->
                HomeStay.fromMap(doc.data ?: return@mapNotNull null)
            }

            if (filters.minPrice > 0 || filters.maxPrice < Double.MAX_VALUE) {
                homestays = homestays.filter {
                    it.rate >= filters.minPrice && it.rate <= filters.maxPrice
                }
            }

            Result.success(homestays)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addHomeStay(homestay: HomeStay): Result<String> {
        return try {
            val docRef = firestore.collection(Constants.HOMESTAYS_COLLECTION).document()
            val id = docRef.id
            val newHomeStay = homestay.copy(id = id)
            docRef.set(newHomeStay.toMap()).await()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHomeStay(homestay: HomeStay): Result<Unit> {
        return try {
            firestore.collection(Constants.HOMESTAYS_COLLECTION)
                .document(homestay.id)
                .set(homestay.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteHomeStay(id: String): Result<Unit> {
        return try {
            firestore.collection(Constants.HOMESTAYS_COLLECTION)
                .document(id)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImage(uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val imageRef = storage.reference
                    .child("${Constants.HOMESTAY_IMAGES_PATH}/${UUID.randomUUID()}.jpg")

                val compressedBytes = ImageUtils.compressImage(
                    NammaHomeStayApp.instance, uri
                )

                imageRef.putBytes(compressedBytes).await()
                val downloadUrl = imageRef.downloadUrl.await()
                Result.success(downloadUrl.toString())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun verifyHomeStay(id: String, verified: Boolean): Result<Unit> {
        return try {
            firestore.collection(Constants.HOMESTAYS_COLLECTION)
                .document(id)
                .update("isVerified", verified)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchHomeStays(query: String): Result<List<HomeStay>> {
        return try {
            val snapshot = firestore.collection(Constants.HOMESTAYS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val all = snapshot.documents.mapNotNull { doc ->
                HomeStay.fromMap(doc.data ?: return@mapNotNull null)
            }
            val q = query.lowercase()
            val filtered = all.filter {
                it.name.lowercase().contains(q) ||
                it.location.lowercase().contains(q) ||
                it.description.lowercase().contains(q)
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllHomeStaysAdmin(): Result<List<HomeStay>> {
        return try {
            val snapshot = firestore.collection(Constants.HOMESTAYS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val homestays = snapshot.documents.mapNotNull { doc ->
                HomeStay.fromMap(doc.data ?: return@mapNotNull null)
            }
            Result.success(homestays)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
