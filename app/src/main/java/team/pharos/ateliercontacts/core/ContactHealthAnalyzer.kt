package team.pharos.ateliercontacts.core

data class ContactHealth(val score: Int, val issues: List<String>)

object ContactHealthAnalyzer {
    fun analyze(contact: ContactRecord): ContactHealth {
        val issues = buildList {
            if (contact.displayName != contact.displayName.trim() || contact.displayName == contact.displayName.lowercase() || contact.displayName == contact.displayName.uppercase()) add("Имя требует очистки")
            if (contact.phones.any { PhoneNormalizer.normalize(it) == null }) add("Телефон не нормализован")
            if (contact.birthday == null) add("Нет дня рождения")
            if (contact.note.isBlank() && contact.tags.isEmpty()) add("Нет заметки или тега")
        }
        return ContactHealth((100 - issues.size * 20).coerceAtLeast(0), issues)
    }
}
