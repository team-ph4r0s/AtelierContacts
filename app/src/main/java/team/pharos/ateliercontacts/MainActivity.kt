package team.pharos.ateliercontacts

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Diversity3
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import team.pharos.ateliercontacts.core.ContactHealthAnalyzer
import team.pharos.ateliercontacts.core.ContactRecord
import team.pharos.ateliercontacts.core.DuplicateDetector
import team.pharos.ateliercontacts.core.NameAndNoteNormalizer
import team.pharos.ateliercontacts.core.VCardSerializer
import team.pharos.ateliercontacts.core.SupportLinks
import team.pharos.ateliercontacts.data.AtelierRepository
import team.pharos.ateliercontacts.ui.AtelierPreferences
import team.pharos.ateliercontacts.ui.AtelierTheme
import team.pharos.ateliercontacts.ui.AtelierThemeProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = AtelierPreferences(this)
        setContent { AtelierThemeProvider(preferences.theme) { AtelierApp(preferences) } }
    }
}

private enum class AtelierTab(val title: String) { CONTACTS("Контакты"), TRIAGE("Поток"), DUPLICATES("Дубликаты"), BIRTHDAYS("Календарь"), SETTINGS("Бутик") }

@Composable
private fun AtelierApp(preferences: AtelierPreferences) {
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var contacts by remember { mutableStateOf<List<ContactRecord>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf(preferences.theme) }
    LaunchedEffect(Unit) { contacts = withContext(Dispatchers.IO) { AtelierRepository(null).loadContacts() } }
    val filtered = remember(contacts, query) { contacts.filter { NameAndNoteNormalizer.matches(query, it) } }
    val tabs = AtelierTab.entries
    AtelierThemeProvider(selectedTheme) {
        Scaffold(bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = {
                        Icon(when (item) { AtelierTab.CONTACTS -> Icons.Outlined.Contacts; AtelierTab.TRIAGE -> Icons.Outlined.Diversity3; AtelierTab.DUPLICATES -> Icons.Outlined.ContentCopy; AtelierTab.BIRTHDAYS -> Icons.Outlined.CalendarMonth; AtelierTab.SETTINGS -> Icons.Outlined.Settings }, item.title)
                    }, label = { Text(item.title, fontSize = 10.sp) })
                }
            }
        }) { padding ->
            Surface(modifier = Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
                AnimatedContent(targetState = tabs[tab], label = "tab") { current ->
                    when (current) {
                        AtelierTab.CONTACTS -> ContactsScreen(filtered, query, { query = it }, contacts)
                        AtelierTab.TRIAGE -> TriageScreen(contacts)
                        AtelierTab.DUPLICATES -> DuplicateScreen(contacts)
                        AtelierTab.BIRTHDAYS -> BirthdayScreen(contacts)
                        AtelierTab.SETTINGS -> SettingsScreen(selectedTheme, { selectedTheme = it; preferences.theme = it }, context)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp)) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ContactsScreen(filtered: List<ContactRecord>, query: String, onQueryChange: (String) -> Unit, all: List<ContactRecord>) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Atelier Contacts", "Ваша адресная книга")
        OutlinedTextField(value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), singleLine = true, label = { Text("Поиск имени, номера или заметки") })
        Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text("Все ${all.size}") })
            AssistChip(onClick = {}, label = { Text("★ Избранные") })
            AssistChip(onClick = {}, label = { Text("⚡ Очистка") })
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
            items(filtered, key = { it.id }) { contact -> ContactCard(contact) }
        }
    }
}

@Composable
private fun ContactCard(contact: ContactRecord) {
    val health = ContactHealthAnalyzer.analyze(contact)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(48.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondaryContainer) { BoxCenter { Text(contact.displayName.firstOrNull()?.uppercase() ?: "?", fontWeight = FontWeight.Bold) } }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(contact.displayName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(contact.phones.firstOrNull().orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                if (contact.note.isNotBlank()) Text(contact.note, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            }
            Text("${health.score}", color = if (health.score > 60) Color(0xFF2D8A62) else Color(0xFFB36A34), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TriageScreen(contacts: List<ContactRecord>) {
    val issues = contacts.map { it to ContactHealthAnalyzer.analyze(it) }.filter { it.second.issues.isNotEmpty() }
    val progress = if (contacts.isEmpty()) 0f else (contacts.size - issues.size).toFloat() / contacts.size
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        ScreenHeader("Поток очистки", "Небольшие шаги к аккуратным контактам")
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        Text("Обработано ${contacts.size - issues.size} из ${contacts.size} • Осталось ${issues.size}", modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        issues.take(3).forEach { (contact, health) ->
            val suggestion = NameAndNoteNormalizer.suggest(contact.displayName)
            Card(Modifier.fillMaxWidth().padding(bottom = 12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text(contact.displayName, fontWeight = FontWeight.SemiBold)
                    Text(health.issues.joinToString(" • "), color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                    if (suggestion.note.isNotBlank()) Text("Предложение: ${suggestion.cleanName} + ${suggestion.note}", modifier = Modifier.padding(top = 10.dp))
                    Button(onClick = {}, modifier = Modifier.padding(top = 10.dp)) { Text("Применить умную правку") }
                }
            }
        }
    }
}

@Composable
private fun DuplicateScreen(contacts: List<ContactRecord>) {
    val clusters = remember(contacts) { DuplicateDetector.findClusters(contacts) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        ScreenHeader("Дубликаты", "Единое имя, все номера и заметки")
        if (clusters.isEmpty()) Text("Дубликаты не найдены", color = MaterialTheme.colorScheme.onSurfaceVariant)
        clusters.forEach { cluster ->
            Card(Modifier.fillMaxWidth().padding(bottom = 12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("${cluster.contacts.size} контакта", fontWeight = FontWeight.SemiBold)
                    cluster.contacts.forEach { Text("• ${it.displayName} — ${it.phones.firstOrNull().orEmpty()}", fontSize = 13.sp) }
                    OutlinedButton(onClick = {}, modifier = Modifier.padding(top = 10.dp)) { Text("Объединить автоматически") }
                }
            }
        }
    }
}

@Composable
private fun BirthdayScreen(contacts: List<ContactRecord>) {
    val birthdays = contacts.filter { it.birthday != null }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        ScreenHeader("Календарь ДР", "Тёплые напоминания без облака")
        Text("${birthdays.size} контакта с датой рождения", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        birthdays.forEach { contact -> Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text("🎂", fontSize = 24.sp); Column(Modifier.padding(start = 12.dp)) { Text(contact.displayName, fontWeight = FontWeight.SemiBold); Text(contact.birthday.orEmpty(), color = MaterialTheme.colorScheme.secondary) } } } }
    }
}

@Composable
private fun SettingsScreen(theme: AtelierTheme, onTheme: (AtelierTheme) -> Unit, context: Context) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        ScreenHeader("Бутик и бэкап", "Приватность как часть дизайна")
        Text("Темы Atelier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        AtelierTheme.entries.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { item -> FilterChip(selected = theme == item, onClick = { onTheme(item) }, label = { Text(item.title, maxLines = 1) }, modifier = Modifier.weight(1f)) } } }
        Spacer(Modifier.height(20.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.padding(18.dp)) { Text("Нулевая телеметрия", fontWeight = FontWeight.SemiBold); Text("Данные остаются на устройстве. Сетевые разрешения отсутствуют.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp)) } }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { shareVCard(context) }, modifier = Modifier.fillMaxWidth()) { Text("Экспорт демо vCard") }
        OutlinedButton(onClick = { openSupport(context) }, modifier = Modifier.fillMaxWidth()) { Text("Поддержать Pharos Team") }
    }
}

private fun shareVCard(context: Context) {
    val bytes = VCardSerializer.serialize(listOf(ContactRecord(1, "Atelier Demo", listOf("+79991234567"), note = "Локальный экспорт")))
    val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newRawUri("Atelier vCard", Uri.parse("data:text/vcard;base64,${android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)}")))
}

private fun openSupport(context: Context) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SupportLinks.DONATION_URL))) }.onFailure { error ->
        if (error is ActivityNotFoundException) context.getSystemService(android.content.ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText("Support link", SupportLinks.DONATION_URL))
    }
}

@Composable
private fun BoxCenter(content: @Composable () -> Unit) { androidx.compose.foundation.layout.Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center, content = { content() }) }
