package com.pontifex.app.data.binary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.security.MessageDigest

class ChecksumVerifierTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun computeSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val buffer = ByteArray(8192)
            var read: Int
            while (stream.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `computeSha256 returns correct hash`() {
        val file = tempFolder.newFile("test.bin")
        file.writeText("hello world")

        val hash = computeSha256(file)
        assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", hash)
    }

    @Test
    fun `computeSha256 returns consistent results`() {
        val file = tempFolder.newFile("test.bin")
        file.writeText("consistent content")

        val hash1 = computeSha256(file)
        val hash2 = computeSha256(file)
        assertEquals(hash1, hash2)
    }

    @Test
    fun `computeSha256 different files different hashes`() {
        val file1 = tempFolder.newFile("file1.bin")
        val file2 = tempFolder.newFile("file2.bin")
        file1.writeText("content A")
        file2.writeText("content B")

        val hash1 = computeSha256(file1)
        val hash2 = computeSha256(file2)
        assertTrue(hash1 != hash2)
    }

    @Test
    fun `computeSha256 empty file`() {
        val file = tempFolder.newFile("empty.bin")

        val hash = computeSha256(file)
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hash)
    }

    @Test
    fun `computeSha256 large file`() {
        val file = tempFolder.newFile("large.bin")
        file.writeBytes(ByteArray(1024 * 1024) { it.toByte() })

        val hash = computeSha256(file)
        assertTrue(hash.matches(Regex("^[0-9a-f]{64}$")))
    }
}
