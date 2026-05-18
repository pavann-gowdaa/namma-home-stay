# Namma HomeStay - Complete Project Setup & Development Guide

## 1. ENVIRONMENT SETUP

### 1.1 Install Android Studio
- Download latest Android Studio from https://developer.android.com/studio
- Install with default settings
- SDK Manager → Install:
  - Android SDK Platform 34
  - Android SDK Build-Tools 34.0.0
  - Android Emulator
  - Android SDK Platform-Tools

### 1.2 Open Project
- File → Open → Select `NammaHomeStay` folder
- Wait for Gradle sync to complete
- If prompted, install any missing SDK components

---

## 2. FIREBASE SETUP

### 2.1 Create Firebase Project
1. Go to https://console.firebase.google.com
2. Click "Add project" → Name: `Namma-HomeStay`
3. Disable Google Analytics (optional)
4. Click "Create project"

### 2.2 Register Android App
1. In Firebase Console, click Android icon to add app
2. Package name: `com.nammahomestay`
3. App nickname: `Namma HomeStay`
4. Debug signing certificate SHA-1: leave blank (optional)
5. Click "Register app"

### 2.3 Download google-services.json
1. Click "Download google-services.json"
2. Place file at: `NammaHomeStay/app/google-services.json`
3. Click "Next" and "Continue to console"

### 2.4 Enable Authentication
1. Firebase Console → Authentication → Sign-in method
2. Enable "Phone" provider
3. Enable "Email/Password" provider (for admin)

### 2.5 Create Firestore Database
1. Firebase Console → Firestore Database → Create database
2. Choose "Start in test mode" (update rules below)
3. Select region (e.g., `asia-south1`)
4. Click "Enable"

### 2.6 Create Firebase Storage
1. Firebase Console → Storage → Get started
2. Select "Start in test mode"
3. Choose region → Done

---

## 3. FIREBASE SECURITY RULES

### Firestore Rules (`firestore.rules`)
Copy these to Firebase Console → Firestore → Rules:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper: Check if user is authenticated
    function isAuth() {
      return request.auth != null;
    }

    // Helper: Check role
    function isRole(role) {
      return isAuth() &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == role;
    }

    // Users: users can read their own data
    match /users/{userId} {
      allow create: if isAuth() && request.auth.uid == userId;
      allow read, update: if isAuth() && request.auth.uid == userId;
      allow delete: if isRole('admin');
    }

    // HomeStays: anyone can read, host can create/update own
    match /homestays/{id} {
      allow read: if true;
      allow create: if isAuth() && request.resource.data.hostId == request.auth.uid;
      allow update: if isAuth() && (
        resource.data.hostId == request.auth.uid ||
        isRole('admin')
      );
      allow delete: if isAuth() && isRole('admin');
    }

    // Inquiries
    match /inquiries/{id} {
      allow read: if isAuth() && (
        resource.data.guestId == request.auth.uid ||
        resource.data.hostId == request.auth.uid
      );
      allow create: if isAuth();
      allow update, delete: if false;
    }

    // Daily Menu
    match /daily_menu/{id} {
      allow read: if true;
      allow create, update: if isAuth();
      allow delete: if isRole('admin');
    }

    // Guide Places
    match /guide_places/{id} {
      allow read: if true;
      allow create: if isAuth();
      allow update, delete: if isRole('admin');
    }
  }
}
```

### Storage Rules (`storage.rules`)
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /homestay_images/{imageId} {
      allow read: if true;
      allow write: if request.auth != null
        && request.resource.size < 5 * 1024 * 1024
        && request.resource.contentType.matches('image/.*');
    }
  }
}
```

---

## 4. GEMINI API SETUP

1. Go to https://makersuite.google.com/app/apikey
2. Click "Create API Key"
3. Copy the API key
4. Open `app/src/main/java/com/nammahomestay/utils/Constants.kt`
5. Replace `YOUR_GEMINI_API_KEY` with actual key

---

## 5. BUILD AND RUN

### Build
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing)
./gradlew assembleRelease

# Run lint
./gradlew lint

# Run tests
./gradlew test
```

### Run on Emulator
- Tools → AVD Manager → Create Virtual Device
- Select Pixel 4 (or any device)
- Select API 34 system image
- Click Play to start emulator
- In Android Studio, select the emulator and click Run

### Run on Physical Device
- Enable Developer Options on phone
- Enable USB Debugging
- Connect via USB
- Select device in Android Studio → Run

---

## 6. FIRESTORE DATA STRUCTURE

### Collection: `users`
```json
{
  "uid": "firebase-auth-uid",
  "phone": "+919876543210",
  "name": "User Name",
  "role": "guest|host|admin",
  "createdAt": 1700000000000
}
```

### Collection: `homestays`
```json
{
  "id": "auto-generated",
  "hostId": "firebase-uid",
  "hostName": "Host Name",
  "hostPhone": "+919876543210",
  "name": "Cozy Farm Stay",
  "location": "Chikmagalur",
  "latitude": 13.3161,
  "longitude": 75.7720,
  "rate": 1500,
  "description": "Beautiful farm stay with coffee plantation view...",
  "photos": ["https://storage.url/photo1.jpg"],
  "amenities": ["WiFi", "Parking", "Meals"],
  "availability": true,
  "isVerified": false,
  "createdAt": 1700000000000
}
```

### Collection: `inquiries`
```json
{
  "id": "auto-generated",
  "homestayId": "homestay-id",
  "hostId": "host-uid",
  "guestId": "guest-uid",
  "guestName": "Guest Name",
  "guestPhone": "+919876543210",
  "message": "Is the property available for Dec 25-27?",
  "timestamp": 1700000000000
}
```

### Collection: `daily_menu`
```json
{
  "id": "auto-generated",
  "homestayId": "homestay-id",
  "date": "2024-01-15",
  "breakfast": ["Idli", "Sambar", "Chutney"],
  "lunch": ["Ragi Mudde", "Koli Saaru", "Tonde Kayi"],
  "dinner": ["Chapati", "Baingan Bharta", "Rice"],
  "description": "Traditional Karnataka home-cooked meals",
  "isAiGenerated": true,
  "createdAt": 1700000000000
}
```

### Collection: `guide_places`
```json
{
  "id": "auto-generated",
  "homestayId": "homestay-id",
  "name": "Mullayanagiri Peak",
  "distance": "15 km",
  "description": "Highest peak in Karnataka, great trekking spot",
  "latitude": 13.3897,
  "longitude": 75.7217,
  "category": "trek",
  "createdAt": 1700000000000
}
```

---

## 7. CREATE ADMIN USER

No admin registration UI. Create admin via Firebase Console:
1. Firebase Console → Authentication → Users → Add user
2. Add email/password user (e.g., `admin@nammahomestay.com`)
3. Firebase Console → Firestore → `users` collection
4. Add document with UID from step 1:
   ```json
   {
     "uid": "the-uid",
     "phone": "+919999999999",
     "name": "Admin",
     "role": "admin",
     "createdAt": 1700000000000
   }
   ```

---

## 8. TESTING

### Auth Flow
- Open app → Splash screen → Login screen
- Enter phone → Send OTP → Check SMS → Enter OTP
- Select role (Guest/Host) → Redirected to respective dashboard

### Guest Features
- View HomeStay listings with shimmer loading
- Tap chip filters (All, Available, Verified, Low Price)
- Tap HomeStay card → Detail screen
- View daily menu, nearby places
- Send inquiry via bottom sheet dialog
- Open location in Google Maps

### Host Features
- Dashboard with quick action cards (Inquiries, Menu, Places)
- Add HomeStay: Fill form → Pick image → Submit
- View & manage inquiries (Call/WhatsApp guest)
- Add daily menu with AI generation
- Add nearby guide places with category

### Admin Features
- View all listings
- Filter by All/Verified/Pending
- Verify or delete listings

### Edge Cases
- No internet: Show error state
- Empty data: Show empty state with message
- Invalid OTP: Show error snackbar
- Image >5MB: Auto-compressed before upload
- Invalid coordinates: Graceful fallback in Maps intent

---

## 9. SIGNED APK (Release)

1. Build → Generate Signed Bundle / APK
2. Create new keystore (or use existing)
3. Fill in key alias, passwords
4. Select release build variant
5. APK will be at: `app/release/app-release.apk`

---

## 10. PROJECT STRUCTURE

```
NammaHomeStay/
├── app/
│   ├── build.gradle.kts          # App dependencies
│   ├── proguard-rules.pro        # ProGuard rules
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/nammahomestay/
│       │   ├── NammaHomeStayApp.kt          # Application class
│       │   ├── MainActivity.kt              # Single activity
│       │   ├── adapter/                     # RecyclerView adapters
│       │   │   ├── AdminHomeStayAdapter.kt
│       │   │   ├── GuidePlaceAdapter.kt
│       │   │   ├── HomeStayAdapter.kt
│       │   │   ├── HostHomeStayAdapter.kt
│       │   │   └── InquiryAdapter.kt
│       │   ├── data/
│       │   │   ├── model/                   # Data classes
│       │   │   │   ├── DailyMenu.kt
│       │   │   │   ├── FilterOptions.kt
│       │   │   │   ├── GuidePlace.kt
│       │   │   │   ├── HomeStay.kt
│       │   │   │   ├── Inquiry.kt
│       │   │   │   └── User.kt
│       │   │   └── repository/              # Firebase repositories
│       │   │       ├── AuthRepository.kt
│       │   │       ├── GeminiRepository.kt
│       │   │       ├── GuideRepository.kt
│       │   │       ├── HomeStayRepository.kt
│       │   │       ├── InquiryRepository.kt
│       │   │       └── MenuRepository.kt
│       │   ├── navigation/
│       │   ├── ui/
│       │   │   ├── admin/AdminPanelFragment.kt
│       │   │   ├── auth/                    # Auth screens
│       │   │   │   ├── LoginFragment.kt
│       │   │   │   ├── OtpFragment.kt
│       │   │   │   ├── RoleSelectionFragment.kt
│       │   │   │   └── SplashFragment.kt
│       │   │   ├── guest/                   # Guest screens
│       │   │   │   ├── FilterDialogFragment.kt
│       │   │   │   ├── GuestHomeFragment.kt
│       │   │   │   ├── HomeStayDetailFragment.kt
│       │   │   │   └── InquiryDialogFragment.kt
│       │   │   ├── host/                    # Host screens
│       │   │   │   ├── AddGuidePlaceFragment.kt
│       │   │   │   ├── AddHomeStayFragment.kt
│       │   │   │   ├── DailyMenuFragment.kt
│       │   │   │   ├── HostDashboardFragment.kt
│       │   │   │   └── InquiriesFragment.kt
│       │   │   └── common/
│       │   └── utils/                       # Utilities
│       │       ├── Constants.kt
│       │       ├── DateUtils.kt
│       │       ├── ImageUtils.kt
│       │       ├── NetworkUtils.kt
│       │       ├── SessionManager.kt
│       │       └── ValidationUtils.kt
│       └── res/
│           ├── drawable/                    # Vector icons
│           ├── font/                        # Poppins font config
│           ├── layout/                      # XML layouts
│           ├── menu/                        # Bottom nav menus
│           ├── navigation/nav_graph.xml
│           └── values/                      # Colors, strings, themes
├── build.gradle.kts            # Root build config
├── settings.gradle.kts         # Project settings
├── .gitignore
└── gradle.properties
```

---

## 11. KEY ARCHITECTURE DECISIONS

### MVVM + Repository Pattern
- **View (Fragment)**: Handles UI, binding, navigation
- **Repository**: All Firebase CRUD operations
- **No ViewModel**: Simplified for project scale (can be added)

### Authentication Flow
1. User enters phone → OTP sent via Firebase Auth
2. OTP verified → User selects Guest/Host role
3. User document created in Firestore
4. Session persisted via EncryptedSharedPreferences
5. App checks session on splash → auto-login

### Image Upload
1. User picks image via system picker
2. Image compressed to <500KB in background
3. Uploaded to Firebase Storage
4. Download URL stored in Firestore

### Gemini AI Integration
1. Host taps "AI Generate" button
2. API call made to Gemini Pro model
3. Response parsed (JSON array or formatted text)
4. Fields populated automatically

### Offline Support
- Firestore persistence enabled (`CACHE_SIZE_UNLIMITED`)
- Data cached locally for offline viewing
- Network state observable via Flow

---

## 12. COMMON ISSUES & SOLUTIONS

| Issue | Solution |
|-------|----------|
| Gradle sync fails | Check internet, verify JDK 17, clear cache |
| Firebase not connecting | Verify google-services.json is present |
| OTP not sending | Enable Phone Auth in Firebase Console |
| Image upload fails | Check Storage rules, check file size limit |
| Maps not opening | Install Google Maps on device/emulator |
| Gemini API fails | Replace API key, check billing |
| Bottom nav not showing | Check role is saved in SessionManager |
| App crashes on font | Remove @font/ references OR download Poppins |

---

## 13. PERFORMANCE OPTIMIZATIONS

- **Image Compression**: All images compressed to <500KB before upload
- **Lazy Loading**: RecyclerView with ViewHolder pattern
- **Caching**: Firestore offline persistence enabled
- **Shimmer**: Loading skeleton for better UX
- **ProGuard**: Enabled for release builds to shrink code
- **Material Design**: Efficient rendering with hardware acceleration

---

## 14. NEXT STEPS / FUTURE ENHANCEMENTS

- [ ] Add push notifications for inquiries
- [ ] Implement in-app chat between guest and host
- [ ] Add payment gateway integration (Razorpay/Stripe)
- [ ] Multi-language support (Kannada)
- [ ] Dark mode support
- [ ] Unit tests with Mockito + JUnit
- [ ] UI tests with Espresso
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Crashlytics + Analytics
- [ ] Advanced search with Algolia
