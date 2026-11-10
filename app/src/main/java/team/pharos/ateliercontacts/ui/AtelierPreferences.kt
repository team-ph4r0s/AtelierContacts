package team.pharos.ateliercontacts.ui

import android.content.Context

class AtelierPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("atelier_preferences", Context.MODE_PRIVATE)
    var theme: AtelierTheme
        get() = preferences.getString("theme", AtelierTheme.CUPERTINO_ALABASTER.name)?.let { runCatching { AtelierTheme.valueOf(it) }.getOrNull() } ?: AtelierTheme.CUPERTINO_ALABASTER
        set(value) { preferences.edit().putString("theme", value.name).apply() }
    var hapticsEnabled: Boolean
        get() = preferences.getBoolean("haptics", true)
        set(value) { preferences.edit().putBoolean("haptics", value).apply() }
}
