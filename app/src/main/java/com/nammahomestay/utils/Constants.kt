package com.nammahomestay.utils

object Constants {
    const val APP_NAME = "Namma HomeStay"

    // Firebase Collections
    const val USERS_COLLECTION = "users"
    const val HOMESTAYS_COLLECTION = "homestays"
    const val INQUIRIES_COLLECTION = "inquiries"
    const val DAILY_MENU_COLLECTION = "daily_menu"
    const val GUIDE_PLACES_COLLECTION = "guide_places"
    const val FAVORITES_COLLECTION = "favorites"

    // Gemini API
    const val GEMINI_API_KEY = "AIzaSyCqemgIOjOYKpPiiOpwPM3nR_b9ezxm8KY"
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"

    // Storage Paths
    const val HOMESTAY_IMAGES_PATH = "homestay_images"

    // Intent Actions
    const val MAPS_PACKAGE_NAME = "com.google.android.apps.maps"
    const val WHATSAPP_PACKAGE_NAME = "com.whatsapp"

    // Date format
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"

    // Google Sign-In - get this from Firebase Console → Authentication → Sign-in method → Google → Web client ID
    const val GOOGLE_WEB_CLIENT_ID = "881721332182-kmnnasn20hmaufqr0m4hc1ij1117u13j.apps.googleusercontent.com"

    // Loading states
    const val STATE_LOADING = "loading"
    const val STATE_SUCCESS = "success"
    const val STATE_ERROR = "error"
    const val STATE_EMPTY = "empty"
}
