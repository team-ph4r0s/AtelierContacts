package team.pharos.ateliercontacts.data

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "triage_state")
data class ContactTriageStateEntity(
    @PrimaryKey val contactId: Long,
    val healthScore: Int,
    val skippedAt: Long? = null,
    val cleanedAt: Long? = null
)

@Entity(tableName = "undo_operations")
data class UndoOperationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationType: String,
    val snapshot: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_tags")
data class CustomTagEntity(@PrimaryKey val tag: String)

@Entity(primaryKeys = ["contactId", "tag"], tableName = "contact_tag_cross_ref")
data class ContactTagCrossRef(val contactId: Long, val tag: String)

class AtelierConverters {
    @TypeConverter fun tagsToString(tags: Set<String>): String = tags.sorted().joinToString("\u001F")
    @TypeConverter fun stringToTags(value: String): Set<String> = value.split("\u001F").filter(String::isNotEmpty).toSet()
}

@androidx.room.Dao
interface AtelierDao {
    @androidx.room.Query("SELECT * FROM triage_state WHERE contactId = :contactId")
    suspend fun triage(contactId: Long): ContactTriageStateEntity?

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun saveTriage(state: ContactTriageStateEntity)

    @androidx.room.Insert
    suspend fun addUndo(entry: UndoOperationLogEntity)

    @androidx.room.Query("SELECT * FROM undo_operations ORDER BY createdAt DESC LIMIT 1")
    suspend fun latestUndo(): UndoOperationLogEntity?

    @androidx.room.Query("DELETE FROM undo_operations WHERE id = :id")
    suspend fun deleteUndo(id: Long)
}

@Database(
    entities = [ContactTriageStateEntity::class, UndoOperationLogEntity::class, CustomTagEntity::class, ContactTagCrossRef::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(AtelierConverters::class)
abstract class AtelierDatabase : RoomDatabase() {
    abstract fun atelierDao(): AtelierDao
}
