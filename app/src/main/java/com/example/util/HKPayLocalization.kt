package com.example.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.model.AppLanguage

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

object HKPayStrings {

    @Composable
    fun pick(
        en: String,
        bn: String = en,
        ar: String = en,
        hi: String = en,
        ms: String = en
    ): String {
        return when (LocalAppLanguage.current) {
            AppLanguage.ENGLISH -> en
            AppLanguage.BENGALI -> bn
            AppLanguage.ARABIC -> ar
            AppLanguage.HINDI -> hi
            AppLanguage.MALAY -> ms
        }
    }

    @Composable
    fun appLockTitle(): String = pick(
        en = "HK PAY Security Lock",
        bn = "HK PAY সিকিউরিটি লক",
        ar = "قفل أمان HK PAY",
        hi = "HK PAY सुरक्षा लॉक",
        ms = "Kunci Keselamatan HK PAY"
    )

    @Composable
    fun languageLabel(): String = pick(
        en = "Select Language",
        bn = "ভাষা নির্বাচন করুন",
        ar = "اختر اللغة",
        hi = "भाषा चुनें",
        ms = "Pilih Bahasa"
    )

    @Composable
    fun welcomeMember(name: String): String = pick(
        en = "Welcome, $name",
        bn = "স্বাগতম, $name",
        ar = "مرحباً، $name",
        hi = "स्वागत है, $name",
        ms = "Selamat datang, $name"
    )

    @Composable
    fun cardLinkedBalance(): String = pick(
        en = "CARD-LINKED BALANCE",
        bn = "কার্ড-লিঙ্কড ব্যালেন্স",
        ar = "الرصيد المرتبط بالبطاقة",
        hi = "कार्ड-लिंक्ड बैलेंस",
        ms = "BAKI PAUTAN KAD"
    )

    @Composable
    fun quickActions(): String = pick(
        en = "Quick Actions",
        bn = "দ্রুত অ্যাকশন",
        ar = "إجراءات سريعة",
        hi = "त्वरित कार्य",
        ms = "Tindakan Pantas"
    )

    @Composable
    fun currencyRates(): String = pick(
        en = "Currency Rates",
        bn = "কারেন্সি রেট",
        ar = "أسعار الصرف",
        hi = "मुद्रा दरें",
        ms = "Kadar Mata Wang"
    )

    @Composable
    fun recentOrders(): String = pick(
        en = "Recent Orders",
        bn = "সাম্প্রতিক অর্ডার",
        ar = "الطلبات الأخيرة",
        hi = "हाल के ऑर्डर",
        ms = "Pesanan Terkini"
    )

    @Composable
    fun myPhysicalCards(): String = pick(
        en = "My Physical Cards",
        bn = "আমার ফিজিক্যাল কার্ড",
        ar = "بطاقاتي الفعلية",
        hi = "मेरे फिजिकल कार्ड",
        ms = "Kad Fizikal Saya"
    )

    @Composable
    fun freezeCard(): String = pick(
        en = "Freeze Card",
        bn = "কার্ড ফ্রিজ করুন",
        ar = "تجميد البطاقة",
        hi = "कार्ड फ्रीज़ करें",
        ms = "Bekukan Kad"
    )

    @Composable
    fun sessionLogout(): String = pick(
        en = "Session Logout",
        bn = "সেশন লগআউট",
        ar = "تسجيل الخروج",
        hi = "सत्र लॉगआउट",
        ms = "Log Keluar Sesi"
    )

    fun navHome(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "হোম"
        AppLanguage.ARABIC -> "الرئيسية"
        AppLanguage.HINDI -> "होम"
        AppLanguage.MALAY -> "Utama"
        AppLanguage.ENGLISH -> "Home"
    }

    fun navCards(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "কার্ড"
        AppLanguage.ARABIC -> "البطاقات"
        AppLanguage.HINDI -> "कार्ड"
        AppLanguage.MALAY -> "Kad"
        AppLanguage.ENGLISH -> "Cards"
    }

    fun navBuy(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "কিনুন"
        AppLanguage.ARABIC -> "شراء"
        AppLanguage.HINDI -> "खरीदें"
        AppLanguage.MALAY -> "Beli"
        AppLanguage.ENGLISH -> "Buy"
    }

    fun navOrders(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "অর্ডার"
        AppLanguage.ARABIC -> "الطلبات"
        AppLanguage.HINDI -> "ऑर्डर"
        AppLanguage.MALAY -> "Pesanan"
        AppLanguage.ENGLISH -> "Orders"
    }

    fun navProfile(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "প্রোফাইল"
        AppLanguage.ARABIC -> "الملف"
        AppLanguage.HINDI -> "प्रोफ़ाइल"
        AppLanguage.MALAY -> "Profil"
        AppLanguage.ENGLISH -> "Profile"
    }
}
