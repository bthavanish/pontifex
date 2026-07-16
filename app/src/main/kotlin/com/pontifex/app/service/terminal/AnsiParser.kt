package com.pontifex.app.service.terminal

import com.pontifex.app.domain.model.StyledSegment
import androidx.compose.ui.graphics.Color

class AnsiParser {
    private enum class State {
        NORMAL,
        ESC_SEEN,
        CSI_PARAMS,
        CSI_FINAL,
        OSC_SEEN
    }

    private var state = State.NORMAL
    private val params = StringBuilder()
    private val buffer = StringBuilder()
    private var currentFg = Color.Unspecified
    private var currentBg = Color.Unspecified
    private var bold = false
    private var dim = false
    private var italic = false
    private var underline = false
    private var blink = false
    private var inverse = false
    private var strikethrough = false

    private val segments = mutableListOf<StyledSegment>()

    fun parse(line: String): List<StyledSegment> {
        segments.clear()
        state = State.NORMAL
        params.clear()
        buffer.clear()
        currentFg = Color.Unspecified
        currentBg = Color.Unspecified
        bold = false
        dim = false
        italic = false
        underline = false
        blink = false
        inverse = false
        strikethrough = false

        for (ch in line) {
            when (state) {
                State.NORMAL -> {
                    if (ch.code == 27) {
                        if (buffer.isNotEmpty()) {
                            flushSegment()
                        }
                        state = State.ESC_SEEN
                    } else {
                        buffer.append(ch)
                    }
                }
                State.ESC_SEEN -> {
                    when (ch) {
                        '[' -> {
                            state = State.CSI_PARAMS
                            params.clear()
                        }
                        ']' -> {
                            state = State.OSC_SEEN
                        }
                        else -> {
                            state = State.NORMAL
                        }
                    }
                }
                State.CSI_PARAMS -> {
                    if (ch in '0'..'9' || ch == ';') {
                        params.append(ch)
                    } else {
                        processCsi(ch)
                        state = State.NORMAL
                    }
                }
                State.OSC_SEEN -> {
                    if (ch.code == 7 || ch.code == 27) {
                        state = State.NORMAL
                    }
                }
                else -> {
                    state = State.NORMAL
                }
            }
        }

        if (buffer.isNotEmpty()) {
            flushSegment()
        }

        if (segments.isEmpty() && line.isNotEmpty()) {
            segments.add(StyledSegment(text = line))
        }

        return segments.toList()
    }

    private fun processCsi(finalByte: Char) {
        val paramStr = params.toString()
        val paramList = if (paramStr.isEmpty()) {
            listOf(0)
        } else {
            paramStr.split(";").map { it.toIntOrNull() ?: 0 }
        }

        when (finalByte) {
            'm' -> processSgr(paramList)
            'A' -> { }
            'B' -> { }
            'C' -> { }
            'D' -> { }
            'J' -> {
                if (paramList.contains(2)) {
                    buffer.clear()
                }
            }
            'K' -> {
                if (paramList.isEmpty() || paramList.contains(0)) {
                    buffer.clear()
                }
            }
        }
    }

    private fun processSgr(params: List<Int>) {
        if (params.isEmpty() || params.all { it == 0 }) {
            resetAttributes()
            return
        }

        var i = 0
        while (i < params.size) {
            when (val param = params[i]) {
                0 -> resetAttributes()
                1 -> bold = true
                2 -> dim = true
                3 -> italic = true
                4 -> underline = true
                5 -> blink = true
                7 -> inverse = true
                9 -> strikethrough = true
                22 -> { bold = false; dim = false }
                23 -> italic = false
                24 -> underline = false
                25 -> blink = false
                27 -> inverse = false
                29 -> strikethrough = false
                in 30..37 -> currentFg = ansiToColor(param - 30, bright = false)
                38 -> {
                    i = parseExtendedColor(params, i, isForeground = true)
                }
                39 -> currentFg = Color.Unspecified
                in 40..47 -> currentBg = ansiToColor(param - 40, bright = false)
                48 -> {
                    i = parseExtendedColor(params, i, isForeground = false)
                }
                49 -> currentBg = Color.Unspecified
                in 90..97 -> currentFg = ansiToColor(param - 90, bright = true)
                in 100..107 -> currentBg = ansiToColor(param - 100, bright = true)
            }
            i++
        }
    }

    private fun parseExtendedColor(params: List<Int>, startIndex: Int, isForeground: Boolean): Int {
        if (startIndex + 1 >= params.size) return startIndex + 1

        return when (params[startIndex + 1]) {
            5 -> {
                if (startIndex + 2 < params.size) {
                    val colorIndex = params[startIndex + 2]
                    val color = xterm256ToColor(colorIndex)
                    if (isForeground) currentFg = color else currentBg = color
                }
                startIndex + 2
            }
            2 -> {
                if (startIndex + 4 < params.size) {
                    val r = params[startIndex + 2]
                    val g = params[startIndex + 3]
                    val b = params[startIndex + 4]
                    val color = Color(0xFF000000 + (r.toLong() shl 16) + (g.toLong() shl 8) + b.toLong())
                    if (isForeground) currentFg = color else currentBg = color
                }
                startIndex + 4
            }
            else -> startIndex + 1
        }
    }

    private fun resetAttributes() {
        currentFg = Color.Unspecified
        currentBg = Color.Unspecified
        bold = false
        dim = false
        italic = false
        underline = false
        blink = false
        inverse = false
        strikethrough = false
    }

    private fun flushSegment() {
        val text = buffer.toString()
        if (text.isNotEmpty()) {
            segments.add(
                StyledSegment(
                    text = text,
                    foreground = if (inverse) currentBg else currentFg,
                    background = if (inverse) currentFg else currentBg,
                    bold = bold,
                    dim = dim,
                    italic = italic,
                    underline = underline,
                    blink = blink,
                    inverse = inverse,
                    strikethrough = strikethrough
                )
            )
        }
        buffer.clear()
    }

    private fun ansiToColor(index: Int, bright: Boolean): Color {
        val colors = if (bright) brightColors else standardColors
        return colors.getOrElse(index) { Color.Unspecified }
    }

    private fun xterm256ToColor(index: Int): Color {
        return when {
            index < 16 -> standardColors.getOrElse(index) { Color.Unspecified }
            index < 232 -> {
                val adjustedIndex = index - 16
                val r = adjustedIndex / 36
                val g = (adjustedIndex % 36) / 6
                val b = adjustedIndex % 6
                val red = if (r == 0) 0 else 55 + r * 40
                val green = if (g == 0) 0 else 55 + g * 40
                val blue = if (b == 0) 0 else 55 + b * 40
                Color(0xFF000000 + (red.toLong() shl 16) + (green.toLong() shl 8) + blue.toLong())
            }
            index < 256 -> {
                val grayIndex = index - 232
                val gray = 8 + grayIndex * 10
                Color(0xFF000000 + (gray.toLong() shl 16) + (gray.toLong() shl 8) + gray.toLong())
            }
            else -> Color.Unspecified
        }
    }

    companion object {
        private val standardColors = listOf(
            Color(0xFF000000), Color(0xFFCD3131), Color(0xFF0DBC79), Color(0xFFE5E510),
            Color(0xFF2472C8), Color(0xFFBC3FBC), Color(0xFF11A8CD), Color(0xFFE5E5E5),
            Color(0xFF666666), Color(0xFFF14C4C), Color(0xFF23D18B), Color(0xFFF5F543),
            Color(0xFF3B8EEA), Color(0xFF555555), Color(0xFF29B8DB), Color(0xFFE5E5E5)
        )

        private val brightColors = listOf(
            Color(0xFF666666), Color(0xFFF14C4C), Color(0xFF23D18B), Color(0xFFF5F543),
            Color(0xFF3B8EEA), Color(0xFFD33682), Color(0xFF29B8DB), Color(0xFFFFFFFF)
        )
    }
}
