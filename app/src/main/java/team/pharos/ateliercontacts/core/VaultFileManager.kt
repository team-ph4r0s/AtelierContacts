package team.pharos.ateliercontacts.core

import android.content.ContentResolver
import android.net.Uri

class VaultFileManager(private val resolver: ContentResolver) {
    fun writeEncrypted(uri: Uri, payload: ByteArray, password: CharArray): Boolean = runCatching {
        resolver.openOutputStream(uri)?.use { it.write(CryptoVaultEngine.encrypt(payload, password)) } ?: error("Cannot open vault destination")
        true
    }.getOrDefault(false)

    fun readEncrypted(uri: Uri, password: CharArray): ByteArray {
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Cannot open vault source")
        return CryptoVaultEngine.decrypt(bytes, password)
    }
}
