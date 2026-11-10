package team.pharos.ateliercontacts.data

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.provider.ContactsContract
import team.pharos.ateliercontacts.core.ContactRecord
import team.pharos.ateliercontacts.core.PhoneNormalizer

class ContactsSystemWriteManager(private val resolver: ContentResolver) {
    fun updateContact(contact: ContactRecord): Boolean {
        val operations = buildList {
            add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection("${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?", arrayOf(contact.id.toString(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE))
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.displayName)
                .build())
            if (contact.note.isNotBlank()) add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection("${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?", arrayOf(contact.id.toString(), ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE))
                .withValue(ContactsContract.CommonDataKinds.Note.NOTE, contact.note)
                .build())
        }
        return applyInChunks(operations)
    }

    fun normalizeNumbers(contact: ContactRecord): List<String> = contact.phones.mapNotNull(PhoneNormalizer::normalize).distinct()

    private fun applyInChunks(operations: List<ContentProviderOperation>): Boolean = runCatching {
        operations.chunked(MAX_OPERATIONS).forEach { resolver.applyBatch(ContactsContract.AUTHORITY, ArrayList(it)) }
        true
    }.getOrDefault(false)

    private companion object { const val MAX_OPERATIONS = 400 }
}
