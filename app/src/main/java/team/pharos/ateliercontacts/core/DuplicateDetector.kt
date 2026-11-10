package team.pharos.ateliercontacts.core

object DuplicateDetector {
    fun findClusters(contacts: List<ContactRecord>): List<DuplicateCluster> {
        val groups = linkedMapOf<String, MutableList<ContactRecord>>()
        contacts.forEach { contact ->
            val keys = contact.phones.mapNotNull(PhoneNormalizer::normalize).distinct()
            val key = keys.firstOrNull()?.let { "phone:$it" } ?: "name:${canonicalName(contact.displayName)}"
            groups.getOrPut(key) { mutableListOf() }.add(contact)
        }
        val phoneClusters = groups.values.filter { it.size > 1 }.map { DuplicateCluster(it.toList(), clusterKey(it)) }.toMutableList()
        val fuzzy = contacts.filter { current ->
            contacts.any { other -> current.id != other.id && nameSimilarity(current.displayName, other.displayName) >= 0.88 }
        }
        if (fuzzy.size > 1 && phoneClusters.none { cluster -> cluster.contacts.map(ContactRecord::id).toSet() == fuzzy.map(ContactRecord::id).toSet() }) {
            phoneClusters += DuplicateCluster(fuzzy.distinctBy(ContactRecord::id), "fuzzy:${canonicalName(fuzzy.first().displayName)}")
        }
        return phoneClusters
    }

    fun merge(cluster: DuplicateCluster, canonicalName: String? = null): ContactRecord {
        val contacts = cluster.contacts
        val name = canonicalName?.trim().takeUnless(String?::isNullOrEmpty)
            ?: contacts.maxByOrNull { it.displayName.count(Char::isLetter) }?.displayName.orEmpty()
        return ContactRecord(
            id = contacts.minOf(ContactRecord::id),
            displayName = name,
            phones = contacts.flatMap(ContactRecord::phones).mapNotNull(PhoneNormalizer::normalize).distinct(),
            emails = contacts.flatMap(ContactRecord::emails).map(String::trim).filter(String::isNotEmpty).distinct(),
            note = contacts.map(ContactRecord::note).map(String::trim).filter(String::isNotEmpty).distinct().joinToString("\n"),
            organization = contacts.map(ContactRecord::organization).firstOrNull(String::isNotEmpty).orEmpty(),
            title = contacts.map(ContactRecord::title).firstOrNull(String::isNotEmpty).orEmpty(),
            birthday = contacts.firstNotNullOfOrNull(ContactRecord::birthday),
            favorite = contacts.any(ContactRecord::favorite),
            tags = contacts.flatMap(ContactRecord::tags).toSet()
        )
    }

    private fun clusterKey(contacts: List<ContactRecord>): String = contacts.flatMap(ContactRecord::phones).mapNotNull(PhoneNormalizer::normalize).sorted().firstOrNull() ?: "name"
    private fun canonicalName(value: String): String = value.lowercase().split(Regex("\\s+")).filter(String::isNotBlank).sorted().joinToString(" ")
    private fun nameSimilarity(left: String, right: String): Double {
        val a = canonicalName(left)
        val b = canonicalName(right)
        val distance = levenshtein(a, b)
        return 1.0 - distance.toDouble() / maxOf(a.length, b.length, 1)
    }

    private fun levenshtein(a: String, b: String): Int {
        val row = IntArray(b.length + 1) { it }
        a.forEachIndexed { i, ca ->
            var diagonal = row[0]
            row[0] = i + 1
            b.forEachIndexed { j, cb ->
                val old = row[j + 1]
                row[j + 1] = if (ca == cb) diagonal else 1 + minOf(diagonal, row[j], old)
                diagonal = old
            }
        }
        return row[b.length]
    }
}
