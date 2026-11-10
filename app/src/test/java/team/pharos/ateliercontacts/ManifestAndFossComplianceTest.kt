package team.pharos.ateliercontacts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import team.pharos.ateliercontacts.core.SupportLinks

class ManifestAndFossComplianceTest {
    @Test fun manifestHasNoNetworkPermissions() {
        val candidates = listOf(File("src/main/AndroidManifest.xml"), File("app/src/main/AndroidManifest.xml"))
        val manifest = candidates.firstOrNull(File::exists)?.readText() ?: error("manifest not found")
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertFalse(manifest.contains("android.permission.ACCESS_NETWORK_STATE"))
    }

    @Test fun supportUrlIsStable() {
        assertEquals("https://app.lava.top/pharosteam", SupportLinks.DONATION_URL)
    }
}
