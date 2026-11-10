package team.pharos.ateliercontacts

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import team.pharos.ateliercontacts.core.CryptoVaultEngine
import team.pharos.ateliercontacts.core.DecryptionAuthException

class CryptoVaultEngineSecurityTest {
    @Test fun roundTripAndPasswordWipe() {
        val password = "секрет 🔐 password".toCharArray()
        val payload = "Иван Петров\nNOTE:emoji 🎂\nBDAY:--05-18".toByteArray()
        val encrypted = CryptoVaultEngine.encrypt(payload, password)
        assertTrue(password.all { it == '\u0000' })
        val decryptPassword = "секрет 🔐 password".toCharArray()
        assertArrayEquals(payload, CryptoVaultEngine.decrypt(encrypted, decryptPassword))
        assertTrue(decryptPassword.all { it == '\u0000' })
    }

    @Test(expected = DecryptionAuthException::class)
    fun wrongPasswordFailsAuthenticationAndWipesInput() {
        val encrypted = CryptoVaultEngine.encrypt("data".toByteArray(), "correct".toCharArray())
        val wrong = "wrong".toCharArray()
        try {
            CryptoVaultEngine.decrypt(encrypted, wrong)
        } finally {
            assertTrue(wrong.all { it == '\u0000' })
        }
    }

    @Test fun tamperedCiphertextFails() {
        val encrypted = CryptoVaultEngine.encrypt("data".toByteArray(), "correct".toCharArray())
        val tampered = encrypted.copyOf().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
        var failed = false
        try { CryptoVaultEngine.decrypt(tampered, "correct".toCharArray()) } catch (_: DecryptionAuthException) { failed = true }
        assertTrue(failed)
    }

    @Test fun tamperedHeaderSaltAndIvFailAuthentication() {
        val encrypted = CryptoVaultEngine.encrypt("data".toByteArray(), "correct".toCharArray())
        listOf(11, 43, 54).forEach { index ->
            val tampered = encrypted.copyOf().also { it[index] = (it[index].toInt() xor 1).toByte() }
            var failed = false
            try { CryptoVaultEngine.decrypt(tampered, "correct".toCharArray()) } catch (_: DecryptionAuthException) { failed = true }
            assertTrue("tampered byte $index must fail", failed)
        }
    }

    @Test fun noncesAreUnique() {
        val first = CryptoVaultEngine.encrypt("same".toByteArray(), "correct".toCharArray())
        val second = CryptoVaultEngine.encrypt("same".toByteArray(), "correct".toCharArray())
        assertFalse(first.contentEquals(second))
        assertFalse(first.copyOfRange(11, 43).contentEquals(second.copyOfRange(11, 43)))
        assertFalse(first.copyOfRange(43, 55).contentEquals(second.copyOfRange(43, 55)))
    }
}
