package team.pharos.ateliercontacts.core

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DecryptionAuthException(message: String, cause: Throwable? = null) : Exception(message, cause)

data class VaultEnvelope(
    val iterations: Int,
    val salt: ByteArray,
    val iv: ByteArray,
    val ciphertext: ByteArray
) {
    fun encode(): ByteArray {
        require(salt.size == SALT_BYTES && iv.size == IV_BYTES)
        val header = CryptoVaultEngine.header(iterations, salt, iv)
        return header + ciphertext
    }

    companion object {
        private const val SALT_BYTES = 32
        private const val IV_BYTES = 12
        fun decode(bytes: ByteArray): VaultEnvelope {
            require(bytes.size > CryptoVaultEngine.HEADER_BYTES) { "Vault is truncated" }
            require(bytes.copyOfRange(0, 6).contentEquals(CryptoVaultEngine.MAGIC)) { "Unsupported vault format" }
            val iterations = ByteBuffer.wrap(bytes, 7, 4).int
            require(iterations in 100_000..2_000_000) { "Invalid KDF cost" }
            val salt = bytes.copyOfRange(11, 43)
            val iv = bytes.copyOfRange(43, 55)
            val ciphertext = bytes.copyOfRange(55, bytes.size)
            return VaultEnvelope(iterations, salt, iv, ciphertext)
        }
    }
}

object CryptoVaultEngine {
    internal val MAGIC = "ATEL01".toByteArray(Charsets.US_ASCII)
    internal const val HEADER_BYTES = 55
    private const val KDF_ID: Byte = 0x01
    private const val ITERATIONS = 600_000
    private const val KEY_BITS = 256
    private const val SALT_BYTES = 32
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128
    private val random = SecureRandom()

    fun encrypt(plaintext: ByteArray, password: CharArray): ByteArray {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val iv = ByteArray(IV_BYTES).also(random::nextBytes)
        val header = header(ITERATIONS, salt, iv)
        var keyBytes = ByteArray(0)
        try {
            keyBytes = deriveKey(password, salt, ITERATIONS)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, "AES"), GCMParameterSpec(TAG_BITS, iv))
            cipher.updateAAD(header)
            return header + cipher.doFinal(plaintext)
        } finally {
            password.fill('\u0000')
            keyBytes.fill(0)
        }
    }

    fun decrypt(envelopeBytes: ByteArray, password: CharArray): ByteArray {
        var keyBytes = ByteArray(0)
        try {
            val envelope = VaultEnvelope.decode(envelopeBytes)
            val header = header(envelope.iterations, envelope.salt, envelope.iv)
            keyBytes = deriveKey(password, envelope.salt, envelope.iterations)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyBytes, "AES"), GCMParameterSpec(TAG_BITS, envelope.iv))
            cipher.updateAAD(header)
            return try {
                cipher.doFinal(envelope.ciphertext)
            } catch (error: AEADBadTagException) {
                throw DecryptionAuthException("Vault authentication failed", error)
            }
        } catch (error: DecryptionAuthException) {
            throw error
        } catch (error: Exception) {
            throw DecryptionAuthException("Vault cannot be opened", error)
        } finally {
            password.fill('\u0000')
            keyBytes.fill(0)
        }
    }

    internal fun header(iterations: Int, salt: ByteArray, iv: ByteArray): ByteArray {
        val buffer = ByteBuffer.allocate(HEADER_BYTES)
        buffer.put(MAGIC)
        buffer.put(KDF_ID)
        buffer.putInt(iterations)
        buffer.put(salt)
        buffer.put(iv)
        return buffer.array()
    }

    private fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
