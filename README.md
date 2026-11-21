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

- **Fast Startup, Sign Up & Sign In:** Instant startup without artificial splash delays, non-blocking IO-backed Firebase RTDB queries, and parallelized KYC Storage + RTDB persistence.
- **2-Step Account Creation (Email Link, Phone No-OTP, NID & Liveness):**
  - Step 1: Full Name, Email (Firebase verification link with `"Open Email Inbox"` launcher), Phone Number (direct entry — no SMS OTP required), Password, and optional Referral Code.
  - Step 2: Optional or admin-required NID Number, NID Front & Back photos (zero-permission Photo Picker), and live Camera Liveness Verification (`FileProvider` + randomized expression prompt).
  - Plus Sign In, Forgot Password, Password visibility toggle, Password change, and optional **Biometric Authentication** (Fingerprint & Face Unlock via AndroidX `BiometricPrompt`) for app lock and sensitive actions.
- **3-Theme System (`Dark`, `Light`, `Navy`) + Header Theme Toggle:** Consistent high-contrast typography and cyan/blue accents tuned per theme, switchable instantly from the clickable theme icon in every screen header or in Profile Settings.
- **User Dashboard & Card-Linked Balance Show/Hide:** Displays total & per-card balance linked directly to `/cards/{uid}` with a one-tap Show/Hide toggle, Welcome header with HK PAY branding, Unique User ID (`HKP-XXXXXX`), Account & KYC status badges, KPIs, Quick Actions grid, Referral Credit card, Live Currency Rates preview, Recent Card Activity, Recent Orders, and Notification Center badge.
- **In-App Tutorials:** Interactive 4-step walkthrough (`HorizontalPager`) covering Account Registration + NID & Liveness, Card Packages & Balance Privacy, Ordering & Live Notifications, and KYC & Themes—with Back, Next, Complete, and Skip controls.
- **Referral Program:** Unique `HKR...` referral code in Profile & Refer & Earn, redeemable at Sign Up or Referral Center for dual-sided card fee credits tracked in Firebase RTDB (`/referral_codes`, `/referrals`, `/referral_rewards`).
- **Physical Card Selection (`HorizontalPager`) & Store (`/card_packages`):** Swipeable physical card finish showcase (`Obsidian Navy`, `Electric Cyan`, `Midnight Graphite`, `Royal Aurora`) paired with live Firebase card packages showing full price breakdowns, KYC requirement, and availability.
- **Buy Physical Card & Payment System:** Auto-prefilled shipping checkout from the user's Profile address, referral credit toggle, configurable payment methods from `/app_settings/paymentMethods`, receipt upload via Photo Picker, and backend-controlled payment confirmation.
- **My Orders & 14-Step Tracking:** Real-time order status timeline (`Order Placed` through `Active`, `Cancelled`, `Refunded`), selected finish, courier tracking, and payment proof upload.
- **My Physical Cards (`/cards/{uid}`):** Displays issued cards with linked balance show/hide toggle, Cardholder Name, Type, Network, Status, Activation Status, Issuing Provider, embedded Recent Card Activity, and biometric- & HTTPS-gated Reveal/Hide/Copy details.
- **Recent Card Activity / Transaction History (`/transactions/{uid}`):** Reusable `RecentCardActivitySection` component on Dashboard & My Cards plus a dedicated filterable & searchable Transaction History screen (filter by transaction type and linked card).
- **Premium Edit Profile (13 Fields + Completion Progress):** Profile completion progress bar (`%`), Profile Photo upload, Full Name, Phone, Date of Birth picker, Gender, NID Number, Occupation, Monthly Income Range, Preferred Currency, and Full Delivery Address (`Street`, `City/Upazila`, `District`, `Postal Code`, `Country`).
- **Personalized FCM Order Notifications (`HKPayMessagingService` & `NotificationHelper`):** Subscribes to `all_users`, `user_{uid}`, and `orders_{uid}` topics; parses personalized order status payloads (`orderId`, `orderStatus`, `customerName`, `cardName`, `trackingNumber`) as well as live RTDB status transitions, with tap-to-open deep linking straight to `OrderDetailRoute`.
- **Full Admin Remote Control (`/app_settings`):** Dynamically enforces Maintenance Mode shields, Registration toggle, Card Ordering pause, Referral Program & Bonus Amount, KYC/Selfie/Liveness requirements, Global System Banners, Support contact info (`supportEmail`, `supportPhone`, `supportWhatsApp`, `supportHours`), and Payment Methods without any Admin UI inside the user app.
- **APK Size Optimization:** Removed heavy unused dependencies (`material-icons-extended`, `logging-interceptor`), replaced with lightweight custom Material vector icons (`HKPayIcons`), enabled R8 minification + resource shrinking + PNG crunching on release builds, and filtered locales (`en`, `bn`).

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
3. **Cloud Messaging (FCM):** Enable Firebase Cloud Messaging for personalized push notifications on order status changes (`orderId`, `orderStatus`, `customerName`, `cardName`, `trackingNumber`).

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
    "maintenanceMode": false,
    "maintenanceMessage": "",
    "registrationEnabled": true,
    "cardOrderingEnabled": true,
    "referralProgramEnabled": true,
    "referralBonusAmount": 5.0,
    "kycRequiredForOrder": true,
    "kycSelfieEnabled": true,
    "livenessCheckRequired": true,
    "bannerTitle": "Welcome to HK PAY",
    "systemNotice": "",
    "supportEmail": "support@hkpay.example",
    "supportPhone": "+8801700000000",
    "supportWhatsApp": "+8801700000000",
    "supportHours": "09:00 - 21:00 BDT",
    "minSupportedVersionCode": 1,
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
