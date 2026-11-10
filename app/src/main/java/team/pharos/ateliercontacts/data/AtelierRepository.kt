package team.pharos.ateliercontacts.data

import android.content.ContentResolver
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import team.pharos.ateliercontacts.core.ContactRecord
import team.pharos.ateliercontacts.core.PhoneNormalizer

class AtelierRepository(private val resolver: ContentResolver?) {
    suspend fun loadContacts(): List<ContactRecord> = withContext(Dispatchers.IO) {
        val contentResolver = resolver ?: return@withContext demoContacts()
        runCatching {
            val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.STARRED)
            contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC")?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
                val hasPhoneIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                val starredIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.STARRED)
                buildList {
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idIndex)
                        val phones = if (cursor.getInt(hasPhoneIndex) == 1) loadPhones(contentResolver, id) else emptyList()
                        add(ContactRecord(id, cursor.getString(nameIndex).orEmpty().ifBlank { "Без имени" }, phones, favorite = cursor.getInt(starredIndex) == 1))
                    }
                }
            } ?: emptyList()
        }.getOrElse { demoContacts() }.ifEmpty { demoContacts() }
    }

    private fun loadPhones(resolver: ContentResolver, contactId: Long): List<String> {
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        return resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(contactId.toString()), null)?.use { cursor ->
            val index = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            buildList { while (cursor.moveToNext()) cursor.getString(index)?.let { add(PhoneNormalizer.normalize(it) ?: it) } }
        }.orEmpty()
    }

    private fun demoContacts() = listOf(
        ContactRecord(1, "иван петров", listOf("8 (999) 123-45-67"), note = "работа"),
        ContactRecord(2, "Иван Петров", listOf("+7 999 123 45 67"), note = "клиент"),
        ContactRecord(3, "Андрей эвакуатор круглосуточно", listOf("89991239876")),
        ContactRecord(4, "Мария Соколова", listOf("+7 916 555 12 12"), birthday = "--05-18", tags = setOf("Семья"))
    )
}
