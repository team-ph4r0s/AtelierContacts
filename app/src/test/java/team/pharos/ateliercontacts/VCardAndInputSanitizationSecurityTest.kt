package team.pharos.ateliercontacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import team.pharos.ateliercontacts.core.ContactRecord
import team.pharos.ateliercontacts.core.VCardParser
import team.pharos.ateliercontacts.core.VCardSerializer

class VCardAndInputSanitizationSecurityTest {
    @Test fun fieldsRoundTripAndAreEscaped() {
        val original = ContactRecord(42, "Иван; Петров, \"дом\"", listOf("+79991234567"), note = "Строка 1\nСтрока 2")
        val encoded = VCardSerializer.serialize(listOf(original)).toString(Charsets.UTF_8)
        assertTrue(encoded.contains("Иван\\; Петров\\, \"дом\""))
        assertTrue(encoded.contains("Строка 1\\nСтрока 2"))
        val parsed = VCardParser.parse(encoded.toByteArray()).single()
        assertEquals(original.displayName, parsed.displayName)
        assertEquals(original.note, parsed.note)
    }

    @Test fun malformedLargePayloadIsRejected() {
        val huge = ByteArray(4 * 1024 * 1024 + 1) { 'A'.code.toByte() }
        var rejected = false
        try { VCardParser.parse(huge) } catch (_: IllegalArgumentException) { rejected = true }
        assertTrue(rejected)
    }
}
