package com.pontifex.app.presentation.components

import com.pontifex.app.domain.model.StyledSegment
import com.pontifex.app.service.terminal.AnsiParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnsiParserTest {

    private lateinit var parser: AnsiParser

    @Before
    fun setup() {
        parser = AnsiParser()
    }

    @Test
    fun `plain text returns single segment`() {
        val result = parser.parse("hello world")
        assertEquals(1, result.size)
        assertEquals("hello world", result[0].text)
    }

    @Test
    fun `empty text returns empty list`() {
        val result = parser.parse("")
        assertEquals(0, result.size)
    }

    @Test
    fun `SGR reset clears all attributes`() {
        val result = parser.parse("\u001b[0mtext")
        assertEquals(1, result.size)
        assertEquals("text", result[0].text)
        assertFalse(result[0].bold)
    }

    @Test
    fun `SGR bold sets bold attribute`() {
        val result = parser.parse("\u001b[1mbold\u001b[0m normal")
        assertEquals(2, result.size)
        assertTrue(result[0].bold)
        assertFalse(result[1].bold)
    }

    @Test
    fun `SGR dim sets dim attribute`() {
        val result = parser.parse("\u001b[2mdim\u001b[0m normal")
        assertTrue(result[0].dim)
        assertFalse(result[1].dim)
    }

    @Test
    fun `SGR italic sets italic attribute`() {
        val result = parser.parse("\u001b[3mitalic\u001b[0m normal")
        assertTrue(result[0].italic)
    }

    @Test
    fun `SGR underline sets underline attribute`() {
        val result = parser.parse("\u001b[4munderline\u001b[0m normal")
        assertTrue(result[0].underline)
    }

    @Test
    fun `SGR strikethrough sets strikethrough attribute`() {
        val result = parser.parse("\u001b[9mstrike\u001b[0m normal")
        assertTrue(result[0].strikethrough)
    }

    @Test
    fun `SGR inverse sets inverse attribute`() {
        val result = parser.parse("\u001b[7minverse\u001b[0m normal")
        assertTrue(result[0].inverse)
    }

    @Test
    fun `SGR foreground red sets color`() {
        val result = parser.parse("\u001b[31mred\u001b[0m")
        assertEquals(1, result.size)
        assertTrue(result[0].foreground != androidx.compose.ui.graphics.Color.Unspecified)
    }

    @Test
    fun `256-color foreground works`() {
        val result = parser.parse("\u001b[38;5;196mred\u001b[0m")
        assertEquals(1, result.size)
        assertTrue(result[0].foreground != androidx.compose.ui.graphics.Color.Unspecified)
    }

    @Test
    fun `truecolor foreground works`() {
        val result = parser.parse("\u001b[38;2;255;128;0morange\u001b[0m")
        assertEquals(1, result.size)
        assertTrue(result[0].foreground != androidx.compose.ui.graphics.Color.Unspecified)
    }

    @Test
    fun `multiple SGR params combined`() {
        val result = parser.parse("\u001b[1;31mbold red\u001b[0m")
        assertEquals(1, result.size)
        assertTrue(result[0].bold)
        assertTrue(result[0].foreground != androidx.compose.ui.graphics.Color.Unspecified)
    }

    @Test
    fun `CSI clear screen clears buffer`() {
        val result = parser.parse("line1\u001b[2Jline2")
        assertEquals(1, result.size)
        assertEquals("line2", result[0].text)
    }

    @Test
    fun `CSI clear line clears buffer`() {
        val result = parser.parse("before\u001b[Kafter")
        assertEquals(1, result.size)
        assertEquals("after", result[0].text)
    }

    @Test
    fun `cursor movement sequences are handled`() {
        val result = parser.parse("\u001b[2A\u001b[3B\u001b[4C\u001b[5Dtext")
        assertEquals(1, result.size)
        assertEquals("text", result[0].text)
    }

    @Test
    fun `mixed plain and escape sequences`() {
        val result = parser.parse("before\u001b[1m bold \u001b[0m after")
        assertEquals(3, result.size)
        assertEquals("before", result[0].text)
        assertEquals(" bold ", result[1].text)
        assertTrue(result[1].bold)
        assertEquals(" after", result[2].text)
        assertFalse(result[2].bold)
    }

    @Test
    fun `bright foreground colors`() {
        val result = parser.parse("\u001b[90mgray\u001b[0m")
        assertEquals(1, result.size)
        assertTrue(result[0].foreground != androidx.compose.ui.graphics.Color.Unspecified)
    }

    @Test
    fun `bright background colors`() {
        val result = parser.parse("\u001b[100mgray bg\u001b[0m")
        assertEquals(1, result.size)
        assertTrue(result[0].background != androidx.compose.ui.graphics.Color.Unspecified)
    }

    @Test
    fun `background color`() {
        val result = parser.parse("\u001b[41mred bg\u001b[0m")
        assertEquals(1, result.size)
        assertTrue(result[0].background != androidx.compose.ui.graphics.Color.Unspecified)
    }
}
