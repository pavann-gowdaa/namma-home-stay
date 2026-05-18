package com.nammahomestay.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nammahomestay.data.model.Inquiry
import com.nammahomestay.utils.Constants
import kotlinx.coroutines.tasks.await

class InquiryRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun sendInquiry(inquiry: Inquiry): Result<Unit> {
        return try {
            val docRef = firestore.collection(Constants.INQUIRIES_COLLECTION).document()
            docRef.set(inquiry.copy(id = docRef.id).toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInquiriesForHost(hostId: String): Result<List<Inquiry>> {
        return try {
            val snapshot = firestore.collection(Constants.INQUIRIES_COLLECTION)
                .whereEqualTo("hostId", hostId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val inquiries = snapshot.documents.mapNotNull { doc ->
                Inquiry.fromMap(doc.data ?: return@mapNotNull null)
            }
            Result.success(inquiries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInquiriesForGuest(guestId: String): Result<List<Inquiry>> {
        return try {
            val snapshot = firestore.collection(Constants.INQUIRIES_COLLECTION)
                .whereEqualTo("guestId", guestId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val inquiries = snapshot.documents.mapNotNull { doc ->
                Inquiry.fromMap(doc.data ?: return@mapNotNull null)
            }
            Result.success(inquiries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
