package com.example.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

object SecurityUtils {

    private const val PIN_APP_SALT = "HKPAY_LOCK_V1_HK_IT_LAB"
    private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun isValidFourDigitPin(pin: String): Boolean {
        return pin.length == 4 && pin.all { it.isDigit() }
    }

    fun isValidPin(pin: String): Boolean = isValidFourDigitPin(pin)

    fun generateSalt(bytes: Int = 16): String {
        val raw = ByteArray(bytes)
        SecureRandom().nextBytes(raw)
        return raw.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(uid: String, pin: String): String {
        val clean = pin.trim()
        require(isValidFourDigitPin(clean)) { "PIN must be exactly 4 digits." }
        val input = "$PIN_APP_SALT:${uid.ifBlank { "device" }}:$clean".toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(input)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(uid: String, pin: String, expectedHash: String): Boolean {
        if (!isValidFourDigitPin(pin) || expectedHash.isBlank()) return false
        val actual = hashPin(uid, pin)
        if (actual.length != expectedHash.length) return false
        var diff = 0
        for (i in actual.indices) {
            diff = diff or (actual[i].code xor expectedHash[i].code)
        }
        return diff == 0
    }

    fun generateTotpSecret(): String = generateBase32Secret(16)

    fun generateBase32Secret(length: Int = 16): String {
        val random = SecureRandom()
        return buildString(length) {
            repeat(length) {
                append(BASE32_ALPHABET[random.nextInt(BASE32_ALPHABET.length)])
            }
        }
    }

    fun buildOtpAuthUri(email: String, secret: String): String {
        val cleanEmail = email.ifBlank { "member@hkpay.app" }
        return "otpauth://totp/HK%20PAY:$cleanEmail?secret=$secret&issuer=HK%20PAY&digits=6&period=30"
    }

    fun generateTotpCode(
        base32Secret: String,
        timeMillis: Long = System.currentTimeMillis(),
        periodSeconds: Long = 30L,
        digits: Int = 6
    ): String {
        val keyBytes = decodeBase32(base32Secret)
        if (keyBytes.isEmpty()) return ""
        val counter = (timeMillis / 1000L) / periodSeconds
        return generateHotp(keyBytes, counter, digits)
    }

    fun verifyTotpCode(
        base32Secret: String,
        code: String,
        timeMillis: Long = System.currentTimeMillis(),
        windowSteps: Int = 1
    ): Boolean {
        val cleanCode = code.trim()
        if (cleanCode.length != 6 || !cleanCode.all { it.isDigit() }) return false
        val keyBytes = decodeBase32(base32Secret)
        if (keyBytes.isEmpty()) return false
        val currentStep = (timeMillis / 1000L) / 30L
        for (offset in -windowSteps..windowSteps) {
            val expected = generateHotp(keyBytes, currentStep + offset, 6)
            if (expected == cleanCode) return true
        }
        return false
    }

    fun secondsRemainingInTotpWindow(timeMillis: Long = System.currentTimeMillis()): Int {
        val elapsed = ((timeMillis / 1000L) % 30L).toInt()
        return (30 - elapsed).coerceIn(1, 30)
    }

    private fun generateHotp(key: ByteArray, counter: Long, digits: Int): String {
        val data = ByteArray(8)
        var value = counter
        for (i in 7 downTo 0) {
            data[i] = (value and 0xFFL).toByte()
            value = value ushr 8
        }
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(data)
        val offset = (hash[hash.size - 1] and 0x0F).toInt()
        val binary =
            ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)
        var mod = 1
        repeat(digits) { mod *= 10 }
        val otp = binary % mod
        return otp.toString().padStart(digits, '0')
    }

    private fun decodeBase32(input: String): ByteArray {
        val clean = input.uppercase().filter { it in BASE32_ALPHABET }
        if (clean.isEmpty()) return ByteArray(0)
        val out = ArrayList<Byte>((clean.length * 5) / 8)
        var buffer = 0
        var bitsLeft = 0
        for (ch in clean) {
            val val5 = BASE32_ALPHABET.indexOf(ch)
            if (val5 < 0) continue
            buffer = (buffer shl 5) or val5
            bitsLeft += 5
            if (bitsLeft >= 8) {
                out.add(((buffer shr (bitsLeft - 8)) and 0xFF).toByte())
                bitsLeft -= 8
            }
        }
        return out.toByteArray()
    }
}
