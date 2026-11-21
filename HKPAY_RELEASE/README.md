# HK PAY — Release & Source Package

This directory packages the **HK PAY** Physical Payment Card Android User Application.

## Folder Structure

- `APK/HKPAY.apk` — Compiled Android APK (`com.hkpay.app`)
- `AAB/HKPAY.aab` — Compiled Android App Bundle (`com.hkpay.app`)
- `SOURCE/` — Complete buildable Android Studio project source
- `README.md` — Build, Firebase, and release instructions

## Included Capabilities

- Firebase Authentication (Email/Password, Verification, Reset, Biometric Unlock)
- Biometric Fingerprint / Face Unlock gating for app lock & sensitive actions
- 4-Step In-App Tutorials (`HorizontalPager`) for Registration, Packages, Orders, and KYC
- Referral Program (`HKR...` user codes, `$5.00` dual-sided credits, Firebase RTDB tracking)
- Swipeable Card Selection (`HorizontalPager` card finish selector + live card packages)
- Full Physical Card Ordering, Receipt Upload, 14-Step Tracking, KYC & Support

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
