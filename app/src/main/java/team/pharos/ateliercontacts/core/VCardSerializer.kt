package team.pharos.ateliercontacts.core

object VCardSerializer {
    private const val MAX_VCARD_BYTES = 4 * 1024 * 1024

    fun serialize(contacts: List<ContactRecord>): ByteArray {
        val body = buildString {
            contacts.forEach { contact ->
                append("BEGIN:VCARD\r\nVERSION:3.0\r\n")
                append("UID:").append(contact.id).append("\r\n")
                append("FN:").append(escape(contact.displayName)).append("\r\n")
                contact.phones.mapNotNull(PhoneNormalizer::normalize).distinct().forEach { phone ->
                    append("TEL;TYPE=CELL:").append(escape(phone)).append("\r\n")
                }
                contact.emails.map(String::trim).filter(String::isNotEmpty).distinct().forEach { email ->
                    append("EMAIL:").append(escape(email)).append("\r\n")
                }
                contact.birthday?.let { append("BDAY:").append(escape(it)).append("\r\n") }
                if (contact.organization.isNotBlank()) append("ORG:").append(escape(contact.organization)).append("\r\n")
                if (contact.title.isNotBlank()) append("TITLE:").append(escape(contact.title)).append("\r\n")
                if (contact.note.isNotBlank()) append("NOTE:").append(escape(contact.note)).append("\r\n")
                if (contact.tags.isNotEmpty()) append("CATEGORIES:").append(contact.tags.joinToString(",", transform = ::escape)).append("\r\n")
                append("END:VCARD\r\n")
            }
        }
        val bytes = body.toByteArray(Charsets.UTF_8)
        require(bytes.size <= MAX_VCARD_BYTES) { "vCard payload is too large" }
        return bytes
    }

    internal fun escape(value: String): String = value.replace("\\", "\\\\").replace("\r\n", "\\n").replace("\n", "\\n").replace("\r", "\\n").replace(";", "\\;").replace(",", "\\,")
}
