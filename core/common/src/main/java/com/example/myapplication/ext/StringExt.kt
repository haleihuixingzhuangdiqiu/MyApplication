package com.example.myapplication.ext

import android.util.Patterns
import java.math.BigInteger
import java.security.MessageDigest

fun String.toMD5(): String {
    val digest = MessageDigest.getInstance("MD5").digest(toByteArray())
    return BigInteger(1, digest).toString(16).padStart(32, '0')
}

fun CharSequence?.isValidEmail(): Boolean =
    !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.centerLimit(len: Int, ellipsis: String = "..."): String =
    if (length > len && len > 1) {
        "${substring(0, len / 2)}$ellipsis${substring(length - len / 2)}"
    } else {
        this
    }

fun String.endLimit(len: Int, ellipsis: String = "..."): String =
    if (length > len && len >= 0) {
        "${substring(0, len)}$ellipsis"
    } else {
        this
    }

fun String.maskEmail(): String {
    if (length <= 6) return this
    val firstThree = substring(0, 3)
    val lastThree = substring(length - 3)
    return firstThree + "*".repeat(length - 6) + lastThree
}

fun String?.formatEmail(): String {
    if (this == null || !contains("@")) return this ?: ""
    val parts = split("@")
    if (parts.size != 2) return this
    val prefix = parts[0]
    val domain = parts[1]
    return if (prefix.length <= 3) this else "${prefix.take(3)}***@$domain"
}

fun String.limitLineBreaks(): String = replace(Regex("\\r\\n|\\r|\\n"), "")

fun String.formatWithComma(): String {
    if (isEmpty()) return ""
    return try {
        val clean = replace(",", "")
        val parts = clean.split(".")
        val intPart = parts[0]
        val decimalPart = parts.getOrNull(1)
        val formattedInt = intPart.reversed().chunked(3).joinToString(",").reversed()
        if (decimalPart != null) "$formattedInt.$decimalPart" else formattedInt
    } catch (_: Exception) {
        this
    }
}
