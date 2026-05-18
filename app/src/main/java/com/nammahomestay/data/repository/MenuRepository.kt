package com.nammahomestay.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nammahomestay.data.model.DailyMenu
import com.nammahomestay.utils.Constants
import com.nammahomestay.utils.DateUtils
import kotlinx.coroutines.tasks.await

class MenuRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addDailyMenu(menu: DailyMenu): Result<Unit> {
        return try {
            val docRef = firestore.collection(Constants.DAILY_MENU_COLLECTION).document()
            docRef.set(menu.copy(id = docRef.id).toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTodayMenu(homestayId: String): Result<DailyMenu?> {
        return try {
            val today = DateUtils.getTodayDate()
            val snapshot = firestore.collection(Constants.DAILY_MENU_COLLECTION)
                .whereEqualTo("homestayId", homestayId)
                .whereEqualTo("date", today)
                .limit(1)
                .get()
                .await()
            val menu = snapshot.documents.firstOrNull()?.let {
                DailyMenu.fromMap(it.data ?: return@let null)
            }
            Result.success(menu)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMenuByDate(homestayId: String, date: String): Result<DailyMenu?> {
        return try {
            val snapshot = firestore.collection(Constants.DAILY_MENU_COLLECTION)
                .whereEqualTo("homestayId", homestayId)
                .whereEqualTo("date", date)
                .limit(1)
                .get()
                .await()
            val menu = snapshot.documents.firstOrNull()?.let {
                DailyMenu.fromMap(it.data ?: return@let null)
            }
            Result.success(menu)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeeklyMenus(homestayId: String): Result<List<DailyMenu>> {
        return try {
            val snapshot = firestore.collection(Constants.DAILY_MENU_COLLECTION)
                .whereEqualTo("homestayId", homestayId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(7)
                .get()
                .await()
            val menus = snapshot.documents.mapNotNull { doc ->
                DailyMenu.fromMap(doc.data ?: return@mapNotNull null)
            }
            Result.success(menus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
