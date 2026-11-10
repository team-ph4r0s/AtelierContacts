package team.pharos.ateliercontacts.core

data class ContactRecord(
    val id: Long,
    val displayName: String,
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
    val note: String = "",
    val organization: String = "",
    val title: String = "",
    val birthday: String? = null,
    val favorite: Boolean = false,
    val tags: Set<String> = emptySet()
)

data class ContactSuggestion(val cleanName: String, val note: String, val score: Int)

data class DuplicateCluster(val contacts: List<ContactRecord>, val key: String)
