package team.pharos.ateliercontacts.core

import java.util.Locale

object NameAndNoteNormalizer {
    private val roleWords = setOf(
        "аварийка", "авто", "врач", "доставка", "евакуатор", "эвакуатор", "клиент", "курьер", "мастер", "мэи",
        "работа", "сантехник", "сервис", "такси", "универ", "юрист", "стоматолог", "круглосуточно",
        "client", "delivery", "doctor", "lawyer", "taxi", "work"
    )
    private val transliteration = mapOf(
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'е' to "e", 'ё' to "e", 'ж' to "zh",
        'з' to "z", 'и' to "i", 'й' to "j", 'к' to "k", 'л' to "l", 'м' to "m", 'н' to "n", 'о' to "o",
        'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t", 'у' to "u", 'ф' to "f", 'х' to "h", 'ц' to "c",
        'ч' to "ch", 'ш' to "sh", 'щ' to "sch", 'ы' to "y", 'э' to "e", 'ю' to "yu", 'я' to "ya"
    )

    fun suggest(raw: String): ContactSuggestion {
        val tokens = raw.trim().split(Regex("\\s+")).filter(String::isNotBlank)
        if (tokens.isEmpty()) return ContactSuggestion("Без имени", "", 0)
        val cleaned = tokens.map { it.trim { c -> !c.isLetterOrDigit() } }
        val nameParts = cleaned.filter { it.lowercase(Locale.ROOT) !in roleWords && !it.any(Char::isDigit) }
        val context = cleaned.filter { it.lowercase(Locale.ROOT) in roleWords || it.any(Char::isDigit) }
        val name = nameParts.ifEmpty { listOf(cleaned.first()) }.joinToString(" ") { titleCase(it) }
        return ContactSuggestion(name, context.map(::titleCase).distinct().joinToString(", "), score = context.size)
    }

    fun transliterate(value: String): String = value.lowercase(Locale.ROOT).map { transliteration[it] ?: it.toString() }.joinToString("")

    fun matches(query: String, contact: ContactRecord): Boolean {
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) return true
        val haystack = buildString {
            append(contact.displayName.lowercase(Locale.ROOT)).append(' ')
            append(transliterate(contact.displayName)).append(' ')
            append(contact.note.lowercase(Locale.ROOT)).append(' ')
            contact.phones.forEach { append(PhoneNormalizer.searchableDigits(it)).append(' ') }
            contact.tags.forEach { append(it.lowercase(Locale.ROOT)).append(' ') }
        }
        return haystack.contains(q) || haystack.contains(transliterate(q))
    }

    private fun titleCase(token: String): String = token.lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
}
