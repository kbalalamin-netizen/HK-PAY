# HK PAY — Physical Payment Card User Application

A native Android application built with **Kotlin** and **Jetpack Compose (Material Design 3)** for ordering, paying for, tracking, and managing **HK PAY Physical Payment Cards**, connected directly to **Firebase Authentication**, **Firebase Realtime Database**, **Firebase Storage**, **Firebase Cloud Messaging (FCM)**, and **Firebase App Check**.

---

## Project Identity

- **App Name:** `HK PAY`
- **Android Application ID (Package Name):** `[here]`
- **Firebase Project ID:** `[here]`
- **Realtime Database URL:** `[here]`
- **Storage Bucket:** `[here]`
- **Android App ID:** `[here]`

---

## Key Features Implemented

- **Authentication & Biometric Unlock:** Splash screen, Welcome, Sign In, Sign Up (with optional Referral Code), Forgot Password, Email Verification flow, Password visibility toggle, Password change, persistent auth state, and optional **Biometric Authentication** (Fingerprint & Face Unlock via AndroidX BiometricPrompt) toggleable in Profile settings for app-launch unlock and sensitive actions (revealing card details, confirming orders, submitting payment proof, changing password).
- **In-App Tutorials:** Interactive 4-step walkthrough (`HorizontalPager`) with visual UI mockups covering User Registration, Browsing Card Packages, Card Ordering & Delivery Tracking, and KYC Verification—with Back, Next, Complete, and Skip controls accessible from Dashboard and Profile.
- **Referral Program:** Every user receives a unique `HKR...` referral code in Profile & Refer & Earn. New users can apply a friend's referral code at Sign Up (or claim once in the Referral Center); both referrer and referee receive a `$5.00` card fee credit tracked in Firebase RTDB (`/referral_codes`, `/referrals`, `/referral_rewards`) and redeemable at checkout.
- **Physical Card Selection (`HorizontalPager`):** Swipeable physical card finish showcase (`Obsidian Navy`, `Electric Cyan`, `Midnight Graphite`, `Royal Aurora`) with animated depth/scale preview paired directly with live Firebase card packages.
- **User Dashboard:** Welcome header with HK PAY branding, Unique User ID (`HKP-XXXXXX`), Account & KYC status badges, KPIs (Orders, Active Cards, Pending), Quick Actions grid, Referral Credit card, Live Currency Rates preview, Recent Orders, and Notification Center badge.
- **Physical Card Store (`/card_packages`):** Streams live card packages from Firebase Realtime Database with full pricing breakdown (Card Price, Issuing Fee, Delivery Fee, Total Price), KYC requirement, and availability sync.
- **Buy Physical Card & Payment System:** Full shipping checkout form with selected card finish preview, referral fee credit toggle, unique Order ID generation, configurable payment methods (`bKash`, `Nagad`, `Rocket`, `Bank Transfer`, `Provider Gateway`) loaded from `/app_settings/paymentMethods`, receipt image upload to Firebase Storage via Android Photo Picker, and backend-controlled payment confirmation.
- **My Orders & 14-Step Tracking:** Live order status timeline (`Order Placed` through `Active`, `Cancelled`, `Refunded`), selected finish, referral credit breakdown, courier tracking, and payment completion flow.
- **My Physical Cards (`/cards/{uid}`):** Displays issued cards with HK PAY branding, Cardholder Name, Type, Network, Status, Activation Status, Issuing Provider, Balance (when provider-supported), and biometric- & HTTPS-gated Reveal/Hide/Copy card details (never storing plaintext PAN/CVV in Firebase).
- **KYC Verification:** NID / Passport form, Date of Birth picker, Photo Picker document & optional selfie upload to `/kyc_uploads/{uid}` in Firebase Storage, and live KYC status sync.
- **Currency Exchange Rates (`/currency_rates`):** Live USD/EUR/GBP to BDT rates with Card Purchase Rate, Service Charge, Last Updated timestamp, and interactive Card Purchase Calculator.
- **Profile, Transactions, Notifications & Support:** Profile photo upload, Edit Profile, Biometric Unlock toggle, Referral Code sharing, App Tutorials replay, Light/Dark/System theme selector, Notification preferences, FCM push notifications (`HKPayMessagingService`), Transaction History filterable by type, and Customer Support ticketing with real-time reply history.

---

## Firebase Setup & Security Rules

### 1. Deploy Realtime Database & Storage Rules
Zero-trust rules are included in the root project:
- `database.rules.json` — Blocks unauthorized reads/writes, enforces per-user isolation on `/users/$uid`, `/orders/$uid`, `/cards/$uid`, `/kyc_submissions/$uid`, `/transactions/$uid`, `/notifications/$uid`, `/support_tickets/$uid`, `/referrals/$uid`, and `/referral_rewards/$uid`, restricts `/referral_codes` to exact-code lookups (preventing list enumeration), forbids client-side admin privilege escalation, and blocks client-side payment/KYC/card approval.
- `storage.rules` — Enforces per-user access and image size/MIME limits on `profile_photos/{userId}`, `user_uploads/{userId}`, and `kyc_uploads/{userId}`.

Deploy with Firebase CLI:
```bash
firebase login
firebase use money-6e7f2
firebase deploy --only database,storage
```

### 2. Firebase Console Checklist
1. **Authentication → Sign-in method:** Enable **Email/Password**.
2. **App Check:** Register your SHA-256 fingerprint for `com.hkpay.app` with **Play Integrity** (and register debug tokens during local testing).
3. **Cloud Messaging (FCM):** Enable Firebase Cloud Messaging for push notifications on order, payment, KYC, and security events.

### 3. Admin-Managed Realtime Database Nodes (Backend / Admin Controlled)
This user app does **not** include an admin panel. Populate these read-only nodes from your authorized backend/admin service:

- `/card_packages/{packageId}`:
  ```json
  {
    "name": "HK PAY Platinum Metal",
    "cardType": "Physical Prepaid Card",
    "network": "Visa",
    "usdPackage": 100,
    "cardPrice": 25.0,
    "issuingFee": 10.0,
    "deliveryFee": 5.0,
    "totalPrice": 40.0,
    "currency": "USD",
    "description": "Global contactless physical card.",
    "kycRequired": true,
    "available": true,
    "imageUrl": ""
  }
  ```
- `/currency_rates/{rateId}`:
  ```json
  {
    "baseCurrency": "USD",
    "targetCurrency": "BDT",
    "currentRate": 120.0,
    "cardPurchaseRate": 122.5,
    "serviceChargePercent": 1.5,
    "serviceChargeFlat": 50.0,
    "lastUpdated": 1763718000000,
    "active": true
  }
  ```
- `/app_settings`:
  ```json
  {
    "kycSelfieEnabled": true,
    "supportEmail": "support@hkpay.example",
    "supportHours": "09:00 - 21:00 BDT",
    "systemNotice": "",
    "paymentMethods": {
      "bkash": {
        "name": "bKash",
        "type": "Mobile Wallet",
        "accountNumber": "01XXXXXXXXX",
        "accountTitle": "HK PAY Merchant",
        "instructions": "Send payment via bKash and enter the TrxID below.",
        "enabled": true
      }
    }
  }
  ```

---

## Card Provider & Payment Webhook Integration

- **Payment Verification:** The mobile app only submits payment proof (`paymentStatus = "Pending"`, `orderStatus = "Payment Pending"`). Your authorized backend / payment webhook must verify the transaction and update `paymentStatus = "Paid"` and `orderStatus = "Payment Confirmed"`.
- **Secure Card Data Display:** Plaintext PAN and CVV are never stored in Firebase. When an issued card in `/cards/{uid}/{cardId}` has `providerSecureViewSupported: true` and an `https://` `providerCardApiUrl` (or `hostedCardViewUrl`), the app fetches ephemeral card details using the user's Firebase ID Token (`Authorization: Bearer <token>`).

---

## Build Instructions (Android Studio & CLI)

### Prerequisites
- Android Studio Ladybug / Meerkat or newer (JDK 11+, Android SDK 36)
- Gradle CLI or Android Studio Gradle sync

### Build APK (Debug / Testing)
```bash
gradle :app:assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK & Release AAB (Google Play)
Configure release signing environment variables (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`) pointing to your upload keystore (`alias: upload`), then run:
```bash
gradle :app:assembleRelease
gradle :app:bundleRelease
```
Outputs:
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

### Install APK via ADB
```bash
adb install -r HKPAY_RELEASE/APK/HKPAY.apk
```
