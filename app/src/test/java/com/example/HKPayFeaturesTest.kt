package com.example

import com.example.data.model.AppLanguage
import com.example.data.model.CardOrder
import com.example.data.model.CardPackage
import com.example.data.model.HKPayConstants
import com.example.data.model.IssuedCard
import com.example.data.model.OrderTrackStage
import com.example.data.model.ThemeMode
import com.example.data.model.UserProfile
import com.example.data.model.resolvedTrackStage
import com.example.data.model.trackingProgressFraction
import com.example.data.repository.AuthRepository
import com.example.notifications.NotificationHelper
import com.example.ui.screens.HKPayCardDesigns
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.LocationCatalog
import com.example.util.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HKPayFeaturesTest {

    @Test
    fun `generatePublicUserId produces deterministic HKP prefix`() {
        val id = AuthRepository.generatePublicUserId("abc123def456")
        assertEquals("HKP-ABC123", id)
    }

    @Test
    fun `generateReferralCode produces deterministic HKR code`() {
        val code = AuthRepository.generateReferralCode("abc123def456")
        assertEquals("HKRDEF456", code)
        assertEquals(9, code.length)
    }

    @Test
    fun `cardPackage computedTotal falls back to component sum when totalPrice is zero`() {
        val pkg = CardPackage(
            id = "pkg_metal",
            name = "HK PAY Platinum",
            cardPrice = 25.0,
            issuingFee = 10.0,
            deliveryFee = 5.0,
            totalPrice = 0.0
        )
        assertEquals(40.0, pkg.computedTotal, 0.001)
    }

    @Test
    fun `cardDesignCatalog exposes four unique designs for pager`() {
        assertEquals(4, HKPayCardDesigns.size)
        val ids = HKPayCardDesigns.map { it.id }.toSet()
        assertEquals(4, ids.size)
        assertTrue(HKPayCardDesigns.all { it.name.isNotBlank() && it.finishLabel.isNotBlank() })
    }

    @Test
    fun `themeMode next cycles cleanly across dark navy and light`() {
        assertEquals(listOf(ThemeMode.DARK, ThemeMode.LIGHT, ThemeMode.NAVY), ThemeMode.entries)
        assertEquals(ThemeMode.NAVY, ThemeMode.DARK.next())
        assertEquals(ThemeMode.LIGHT, ThemeMode.NAVY.next())
        assertEquals(ThemeMode.DARK, ThemeMode.LIGHT.next())
    }

    @Test
    fun `userProfile completionPercent reflects filled personal address and liveness fields`() {
        val minimal = UserProfile(country = "")
        assertEquals(0, minimal.completionPercent)

        val complete = UserProfile(
            fullName = "Hasan Karim",
            phone = "+8801712345678",
            dateOfBirth = "1998-05-14",
            gender = "Male",
            nidNumber = "1234567890",
            occupation = "Software Engineer",
            streetAddress = "Road 12, Dhanmondi",
            cityUpazila = "Dhanmondi",
            district = "Dhaka",
            country = "Bangladesh",
            photoUrl = "https://example.com/avatar.jpg",
            livenessVerified = true
        )
        assertEquals(100, complete.completionPercent)
    }

    @Test
    fun `buildPersonalizedOrderMessage includes customer first name card status and tracking`() {
        val (title, body) = NotificationHelper.buildPersonalizedOrderMessage(
            customerName = "Hasan Karim",
            packageName = "HK PAY Platinum",
            orderId = "ORD-9021",
            orderStatus = "Shipped",
            trackingNumber = "TRK-7788"
        )
        assertTrue(title.contains("HK PAY Platinum"))
        assertTrue(body.contains("Hasan"))
        assertTrue(body.contains("ORD-9021"))
        assertTrue(body.contains("Shipped"))
        assertTrue(body.contains("TRK-7788"))
    }

    @Test
    fun `securityUtils pin hashing validates 4-digit pin and rejects wrong pins`() {
        assertTrue(SecurityUtils.isValidPin("4829"))
        assertFalse(SecurityUtils.isValidPin("482"))
        assertFalse(SecurityUtils.isValidPin("abcd"))

        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPin(uid = salt, pin = "4829")
        assertTrue(SecurityUtils.verifyPin(uid = salt, pin = "4829", expectedHash = hash))
        assertFalse(SecurityUtils.verifyPin(uid = salt, pin = "0000", expectedHash = hash))
    }

    @Test
    fun `securityUtils totp generates valid 6-digit code and verifies within window`() {
        val secret = SecurityUtils.generateTotpSecret()
        assertTrue(secret.length >= 16)
        val epochMs = 1_750_000_000_000L
        val code = SecurityUtils.generateTotpCode(secret, timeMillis = epochMs)
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
        assertTrue(SecurityUtils.verifyTotpCode(secret, code, timeMillis = epochMs))
        assertTrue(SecurityUtils.verifyTotpCode(secret, code, timeMillis = epochMs + 25_000L))
        assertFalse(SecurityUtils.verifyTotpCode(secret, "999999", timeMillis = epochMs + 600_000L))
    }

    @Test
    fun `locationCatalog provides worldwide flags and Bangladesh Zila Upazila hierarchy`() {
        assertTrue(LocationCatalog.countries.size >= 15)
        val bd = LocationCatalog.countryFor("Bangladesh")
        assertEquals("🇧🇩", bd.flagEmoji)
        assertEquals("+880", bd.dialCode)

        val bdZilas = LocationCatalog.regionsForCountry("Bangladesh")
        assertTrue(bdZilas.contains("Dhaka"))
        assertTrue(bdZilas.contains("Chattogram"))
        assertTrue(LocationCatalog.subRegionsFor("Bangladesh", "Dhaka").contains("Dhanmondi"))
    }

    @Test
    fun `appLanguage shortTags include flag and two-letter code`() {
        assertEquals("🇧🇩 bn", AppLanguage.BENGALI.shortTag)
        assertEquals("🇺🇸 en", AppLanguage.ENGLISH.shortTag)
        assertEquals(AppLanguage.BENGALI, AppLanguage.fromCode("bn"))
    }

    @Test
    fun `default dashboard sliders and card freeze resolution behave deterministically`() {
        assertTrue(HKPayConstants.DEFAULT_DASHBOARD_SLIDERS.size >= 3)
        val card1 = IssuedCard(cardId = "c1", cardStatus = "Active", isFrozen = false)
        val card2 = IssuedCard(cardId = "c2", cardStatus = "Active", isFrozen = false)
        val state = MainDashboardState(
            userProfile = UserProfile(frozenCardIds = setOf("c1")),
            issuedCards = listOf(card1, card2)
        )
        assertTrue(state.isCardFrozen(card1))
        assertFalse(state.isCardFrozen(card2))
        assertEquals(1, state.activeCardsCount)
        assertTrue(state.activeSliders.isNotEmpty())
    }

    @Test
    fun `cardOrder resolvedTrackStage maps Processing Shipped Delivered and Cancelled accurately`() {
        assertEquals(OrderTrackStage.PLACED, CardOrder(orderStatus = "Order Placed").resolvedTrackStage())
        assertEquals(OrderTrackStage.PROCESSING, CardOrder(orderStatus = "Processing").resolvedTrackStage())
        assertEquals(OrderTrackStage.PROCESSING, CardOrder(orderStatus = "Card Processing").resolvedTrackStage())
        assertEquals(OrderTrackStage.PROCESSING, CardOrder(orderStatus = "Card Issued").resolvedTrackStage())
        assertEquals(OrderTrackStage.SHIPPED, CardOrder(orderStatus = "Shipped").resolvedTrackStage())
        assertEquals(OrderTrackStage.SHIPPED, CardOrder(orderStatus = "In Transit").resolvedTrackStage())
        assertEquals(OrderTrackStage.DELIVERED, CardOrder(orderStatus = "Delivered").resolvedTrackStage())
        assertEquals(OrderTrackStage.DELIVERED, CardOrder(orderStatus = "Active").resolvedTrackStage())
        assertEquals(OrderTrackStage.CANCELLED, CardOrder(orderStatus = "Cancelled").resolvedTrackStage())

        assertEquals(0.25f, CardOrder(orderStatus = "Order Placed").trackingProgressFraction(), 0.001f)
        assertEquals(0.50f, CardOrder(orderStatus = "Card Processing").trackingProgressFraction(), 0.001f)
        assertEquals(0.75f, CardOrder(orderStatus = "Shipped").trackingProgressFraction(), 0.001f)
        assertEquals(1.0f, CardOrder(orderStatus = "Delivered").trackingProgressFraction(), 0.001f)
        assertEquals(0f, CardOrder(orderStatus = "Cancelled").trackingProgressFraction(), 0.001f)
    }
}
