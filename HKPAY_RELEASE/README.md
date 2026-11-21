# HK PAY — Release & Source Package

This directory packages the **HK PAY** Physical Payment Card Android User Application.

## Folder Structure

- `APK/HKPAY.apk` — Compiled Android APK (`com.hkpay.app`)
- `AAB/HKPAY.aab` — Compiled Android App Bundle (`com.hkpay.app`)
- `SOURCE/` — Complete buildable Android Studio project source
- `README.md` — Build, Firebase, and release instructions

## Included Capabilities

- Fast Splash, Sign In, Email Verification (link + Open Inbox), Forgot Password & Biometric Unlock
- 2-Step Account Creation with Phone (no SMS OTP), NID Front/Back upload & Live Camera Liveness Check
- 3-Theme System (`Dark`, `Light`, `Navy`) with instant Header Theme Icon switcher & matched contrast
- Dashboard Card-Linked Balance Hero with Show/Hide toggle & embedded Recent Card Activity
- 13-Field Premium Profile Editor with Completion Percentage, Avatar Upload & Pre-filled Shipping Checkout
- Personalized FCM Order Status Notifications (`orderId`, `orderStatus`, `customerName`, `trackingNumber`) & Deep Linking
- Full Firebase Admin Control (`/app_settings` maintenance shield, registration/ordering switches, referral bonus, support contact)
- Swipeable Card Selection (`HorizontalPager`), Card Store, 14-Step Order Tracking, Referral Program & Support Ticketing

## Firebase Configuration (`money-6e7f2`)

- **Project ID:** `money-6e7f2`
- **Database URL:** `https://money-6e7f2-default-rtdb.firebaseio.com`
- **Storage Bucket:** `money-6e7f2.firebasestorage.app`
- **Android App ID:** `1:98055922055:android:265d59ed426b10f0e97322`
- **Package Name:** `com.hkpay.app`

Deploy security rules before going live:
```bash
firebase use money-6e7f2
firebase deploy --only database,storage
```

## Installing the APK

On a connected Android device with USB Debugging enabled:
```bash
adb install -r APK/HKPAY.apk
```

## Building Signed Release APK & AAB for Google Play

1. Generate or provide your Play Upload Keystore (`alias: upload`).
2. Export environment variables:
   ```bash
   export KEYSTORE_PATH=/path/to/my-upload-key.jks
   export STORE_PASSWORD=your_store_password
   export KEY_PASSWORD=your_key_password
   ```
3. Run Gradle inside `SOURCE/`:
   ```bash
   gradle :app:assembleRelease
   gradle :app:bundleRelease
   ```
4. Upload `app/build/outputs/bundle/release/app-release.aab` to Google Play Console.
