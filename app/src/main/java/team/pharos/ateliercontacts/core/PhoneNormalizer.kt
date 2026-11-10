package team.pharos.ateliercontacts.core

object PhoneNormalizer {
    fun normalize(raw: String, defaultRegion: String = "RU"): String? {
        val value = raw.trim()
        if (value.isEmpty()) return null
        val digits = value.filter(Char::isDigit)
        if (digits.length < 7) return null
        return when {
            value.startsWith("+") -> "+$digits"
            defaultRegion == "RU" && digits.length == 11 && digits.startsWith('8') -> "+7${digits.drop(1)}"
            defaultRegion == "RU" && digits.length == 10 && digits.startsWith('9') -> "+7$digits"
            else -> "+$digits"
        }
    }

    fun suffix10(raw: String): String? = normalize(raw)?.takeLast(10)

    fun searchableDigits(raw: String): String = raw.filter(Char::isDigit)
}
