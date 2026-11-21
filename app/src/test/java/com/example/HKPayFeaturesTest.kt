package com.example

import com.example.data.model.CardPackage
import com.example.data.model.ThemeMode
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.notifications.NotificationHelper
import com.example.ui.screens.HKPayCardDesigns
import org.junit.Assert.assertEquals
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
}
