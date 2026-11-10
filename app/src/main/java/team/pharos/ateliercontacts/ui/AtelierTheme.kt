package team.pharos.ateliercontacts.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

enum class AtelierTheme(val title: String, val dark: Boolean, val background: Color, val surface: Color, val primary: Color, val accent: Color, val secondary: Color, val border: Color) {
    CUPERTINO_ALABASTER("Cupertino Alabaster", false, Color(0xFFFBFBFD), Color.White, Color(0xFF1D1D1F), Color(0xFF0066CC), Color(0xFF6E6E73), Color(0xFFE5E5EA)),
    CASHMERE_CAPPUCCINO("Cashmere Cappuccino", false, Color(0xFFF7F3EE), Color(0xFFFCF9F5), Color(0xFF2C221E), Color(0xFF8C5A3C), Color(0xFF7A6A5E), Color(0xFFDFD3C6)),
    GINZA_SILK("Ginza Silk", false, Color(0xFFF6F5F0), Color.White, Color(0xFF141413), Color(0xFF9E2A2B), Color(0xFF666560), Color(0xFFDCD9D0)),
    SAVILE_ROW_LINEN("Savile Row Linen", false, Color(0xFFF4F1EA), Color(0xFFFAF8F5), Color(0xFF1B2621), Color(0xFF2D5A43), Color(0xFF5C6660), Color(0xFFD5CFC2)),
    NORDIC_PORCELAIN("Nordic Porcelain", false, Color(0xFFF2F5F8), Color.White, Color(0xFF0F172A), Color(0xFF3B6E8C), Color(0xFF64748B), Color(0xFFCBD5E1)),
    OBSIDIAN_TITANIUM("Obsidian Titanium", true, Color.Black, Color(0xFF111113), Color(0xFFF5F5F7), Color(0xFF64D2FF), Color(0xFF8E8E93), Color(0xFF2C2C2E)),
    CARBON_ESPRESSO("Carbon Espresso", true, Color(0xFF0D0A08), Color(0xFF17120F), Color(0xFFF2EAE4), Color(0xFFD4A373), Color(0xFF9C8B7E), Color(0xFF332822)),
    MIDNIGHT_VELVET("Midnight Velvet", true, Color(0xFF070913), Color(0xFF101424), Color(0xFFEEF2FF), Color(0xFFA5B4FC), Color(0xFF818CF8), Color(0xFF29324E)),
    EMERALD_NOIR("Emerald Noir", true, Color(0xFF060E0B), Color(0xFF0D1A15), Color(0xFFECFDF5), Color(0xFFD4AF37), Color(0xFF86A397), Color(0xFF233D32)),
    MONACO_ONYX("Monaco Onyx", true, Color(0xFF0A0A0B), Color(0xFF141416), Color(0xFFFAFAFA), Color(0xFFE5B8A8), Color(0xFF9CA3AF), Color(0xFF2E2E34))
}

@Composable
fun AtelierThemeProvider(theme: AtelierTheme = AtelierTheme.CUPERTINO_ALABASTER, content: @Composable () -> Unit) {
    val background by animateColorAsState(theme.background, tween(450), label = "background")
    val surface by animateColorAsState(theme.surface, tween(450), label = "surface")
    val primary by animateColorAsState(theme.primary, tween(450), label = "primary")
    val accent by animateColorAsState(theme.accent, tween(450), label = "accent")
    val colors = if (theme.dark || isSystemInDarkTheme() && false) darkColorScheme(background = background, surface = surface, primary = primary, secondary = accent) else lightColorScheme(background = background, surface = surface, primary = primary, secondary = accent)
    MaterialTheme(colorScheme = colors, content = content)
}
