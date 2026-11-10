package team.pharos.ateliercontacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import team.pharos.ateliercontacts.core.ContactRecord
import team.pharos.ateliercontacts.core.DuplicateDetector
import team.pharos.ateliercontacts.core.NameAndNoteNormalizer
import team.pharos.ateliercontacts.core.PhoneNormalizer

class DuplicateDetectorAndNormalizerTest {
    @Test fun russianPhonesShareE164() {
        val expected = "+79991234567"
        assertEquals(expected, PhoneNormalizer.normalize("8 (999) 123-45-67"))
        assertEquals(expected, PhoneNormalizer.normalize("+7-999-123-45-67"))
        assertEquals(expected, PhoneNormalizer.normalize("89991234567"))
    }

    @Test fun messyNameSuggestionExtractsContext() {
        val suggestion = NameAndNoteNormalizer.suggest("андрей эвакуатор круглосуточно")
        assertEquals("Андрей", suggestion.cleanName)
        assertTrue(suggestion.note.contains("Эвакуатор"))
        assertTrue(suggestion.note.contains("Круглосуточно"))
    }

    @Test fun duplicateMergeKeepsUniqueData() {
        val contacts = listOf(
            ContactRecord(1, "Иван Петров", listOf("89991234567"), note = "клиент"),
            ContactRecord(2, "Петров Иван", listOf("+79161234567"), note = "работа", birthday = "--04-20")
        )
        val cluster = DuplicateDetector.findClusters(contacts).first { it.contacts.size == 2 }
        val merged = DuplicateDetector.merge(cluster)
        assertEquals(2, merged.phones.size)
        assertTrue(merged.note.contains("клиент"))
        assertTrue(merged.note.contains("работа"))
        assertEquals("--04-20", merged.birthday)
    }
}
