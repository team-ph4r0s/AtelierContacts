package team.pharos.ateliercontacts.data

import android.content.ContentResolver
import android.content.ContentValues
import android.provider.CalendarContract
import java.util.TimeZone
import team.pharos.ateliercontacts.core.ContactRecord

data class BirthdayEvent(val contactId: Long, val title: String, val monthDay: String, val age: Int?)

class BirthdayCalendarSyncManager(private val resolver: ContentResolver) {
    fun eventFor(contact: ContactRecord, currentYear: Int): BirthdayEvent? {
        val birthday = contact.birthday ?: return null
        val monthDay = birthday.takeLast(5).takeIf { it.matches(Regex("\\d{2}-\\d{2}")) } ?: return null
        val year = birthday.takeIf { it.length >= 10 }?.take(4)?.toIntOrNull()
        return BirthdayEvent(contact.id, "День рождения: ${contact.displayName}", monthDay, year?.let { currentYear - it })
    }

    fun upsert(event: BirthdayEvent, calendarId: Long, year: Int): Boolean = runCatching {
        val marker = "atelier_contact_birthday_${event.contactId}"
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, event.age?.let { "Возраст: $it" }.orEmpty())
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getTimeZone("UTC").id)
            put(CalendarContract.Events.RRULE, "FREQ=YEARLY")
            put(CalendarContract.Events.DTSTART, java.time.LocalDate.parse("$year-${event.monthDay}").toEpochDay() * 86_400_000)
            put(CalendarContract.Events.DTEND, java.time.LocalDate.parse("$year-${event.monthDay}").plusDays(1).toEpochDay() * 86_400_000)
            put(CalendarContract.Events.UID_2445, marker)
        }
        val existingId = resolver.query(
            CalendarContract.Events.CONTENT_URI,
            arrayOf(CalendarContract.Events._ID),
            "${CalendarContract.Events.UID_2445} = ?",
            arrayOf(marker),
            null
        )?.use { cursor -> if (cursor.moveToFirst()) cursor.getLong(0) else null }
        if (existingId == null) resolver.insert(CalendarContract.Events.CONTENT_URI, values) != null
        else resolver.update(CalendarContract.Events.CONTENT_URI, values, "${CalendarContract.Events._ID} = ?", arrayOf(existingId.toString())) == 1
    }.getOrDefault(false)
}
