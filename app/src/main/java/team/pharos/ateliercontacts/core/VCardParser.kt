package team.pharos.ateliercontacts.core

object VCardParser {
    private const val MAX_VCARD_BYTES = 4 * 1024 * 1024

    fun parse(bytes: ByteArray): List<ContactRecord> {
        require(bytes.size <= MAX_VCARD_BYTES) { "vCard payload is too large" }
        val unfolded = unfold(bytes.toString(Charsets.UTF_8))
        val result = mutableListOf<ContactRecord>()
        var current: MutableMap<String, MutableList<String>>? = null
        unfolded.split("\r\n", "\n").forEach { rawLine ->
            val line = rawLine.trimEnd('\r')
            when {
                line.equals("BEGIN:VCARD", ignoreCase = true) -> current = linkedMapOf()
                line.equals("END:VCARD", ignoreCase = true) -> current?.let { result += toContact(it, result.size.toLong()) }.also { current = null }
                current != null -> {
                    val separator = line.indexOf(':')
                    if (separator > 0) {
                        val key = line.substring(0, separator).substringBefore(';').uppercase()
                        val value = unescape(line.substring(separator + 1))
                        current!!.getOrPut(key) { mutableListOf() }.add(value)
                    }
                }
            }
        }
        return result
    }

    private fun toContact(fields: Map<String, List<String>>, fallbackId: Long): ContactRecord {
        val phones = fields["TEL"].orEmpty()
        return ContactRecord(
            id = fields["UID"]?.firstOrNull()?.toLongOrNull() ?: fallbackId,
            displayName = fields["FN"]?.firstOrNull().orEmpty().ifBlank { "Без имени" },
            phones = phones,
            emails = fields["EMAIL"].orEmpty(),
            note = fields["NOTE"]?.joinToString("\n").orEmpty(),
            organization = fields["ORG"]?.firstOrNull().orEmpty(),
            title = fields["TITLE"]?.firstOrNull().orEmpty(),
            birthday = fields["BDAY"]?.firstOrNull(),
            tags = fields["CATEGORIES"].orEmpty().flatMap { it.split(',') }.map(String::trim).filter(String::isNotEmpty).toSet()
        )
    }

    private fun unfold(value: String): String = value.replace("\r\n ", "").replace("\n ", "")
    private fun unescape(value: String): String {
        val out = StringBuilder(value.length)
        var escaped = false
        value.forEach { char ->
            if (escaped) {
                out.append(if (char == 'n' || char == 'N') '\n' else char)
                escaped = false
            } else if (char == '\\') escaped = true else out.append(char)
        }
        if (escaped) out.append('\\')
        return out.toString()
    }
}
