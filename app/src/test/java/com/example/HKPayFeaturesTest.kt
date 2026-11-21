package com.example

import com.example.data.model.CardPackage
import com.example.data.repository.AuthRepository
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
}
