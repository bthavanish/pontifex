package com.pontifex.app.domain.model

data class TerminalColorScheme(
    val name: String,
    val background: Long,
    val foreground: Long,
    val cursor: Long,
    val selection: Long,
    val ansiColors: List<Long>
) {
    init {
        require(ansiColors.size == 16) { "TerminalColorScheme requires exactly 16 ANSI colors" }
    }
}

object TerminalColorSchemes {
    val Default = TerminalColorScheme(
        name = "Default",
        background = 0xFF1C1B1F,
        foreground = 0xFFE6E1E5,
        cursor = 0xFFCAC4D0,
        selection = 0x40CAC4D0,
        ansiColors = listOf(
            0xFF000000, 0xFFCD3131, 0xFF0DBC79, 0xFFE5E510,
            0xFF2472C8, 0xFFBC3FBC, 0xFF11A8CD, 0xFFE5E5E5,
            0xFF666666, 0xFFF14C4C, 0xFF23D18B, 0xFFF5F543,
            0xFF3B8EEA, 0xFF555555, 0xFF29B8DB, 0xFFE5E5E5
        )
    )

    val Dracula = TerminalColorScheme(
        name = "Dracula",
        background = 0xFF282A36,
        foreground = 0xFFF8F8F2,
        cursor = 0xFFF8F8F2,
        selection = 0x4444475A,
        ansiColors = listOf(
            0xFF21222C, 0xFFFF5555, 0xFF50FA7B, 0xFFF1FA8C,
            0xFFBD93F9, 0xFFFF79C6, 0xFF8BE9FD, 0xFFF8F8F2,
            0xFF6272A4, 0xFFFF6E6E, 0xFF69FF94, 0xFFFFFFA5,
            0xFFD6ACFF, 0xFFFF92DF, 0xFFA4FFFF, 0xFFFFFFFF
        )
    )

    val SolarizedDark = TerminalColorScheme(
        name = "Solarized Dark",
        background = 0xFF002B36,
        foreground = 0xFF839496,
        cursor = 0xFF839496,
        selection = 0x33839496,
        ansiColors = listOf(
            0xFF073642, 0xFFDC322F, 0xFF859900, 0xFFB58900,
            0xFF268BD2, 0xFFD33682, 0xFF2AA198, 0xFFEEE8D5,
            0xFF002B36, 0xFFCB4B16, 0xFF586E75, 0xFF657B83,
            0xFF839496, 0xFF93A1A1, 0xFF93A1A1, 0xFFFDF6E3
        )
    )

    val Nord = TerminalColorScheme(
        name = "Nord",
        background = 0xFF2E3440,
        foreground = 0xFFD8DEE9,
        cursor = 0xFFD8DEE9,
        selection = 0x444C564A,
        ansiColors = listOf(
            0xFF3B4252, 0xFFBF616A, 0xFFA3BE8C, 0xFFEBCB8B,
            0xFF81A1C1, 0xFFB48EAD, 0xFF88C0D0, 0xFFE5E9F0,
            0xFF4C566A, 0xFFBF616A, 0xFFA3BE8C, 0xFFEBCB8B,
            0xFF81A1C1, 0xFFB48EAD, 0xFF88C0D0, 0xFFECEFF4
        )
    )

    val GruvboxDark = TerminalColorScheme(
        name = "Gruvbox Dark",
        background = 0xFF282828,
        foreground = 0xFFEBDBB2,
        cursor = 0xFFEBDBB2,
        selection = 0x44928374,
        ansiColors = listOf(
            0xFF282828, 0xFFCC241D, 0xFF98971A, 0xFFD79921,
            0xFF458588, 0xFFB16286, 0xFF689D6A, 0xFFEBDBB2,
            0xFF928374, 0xFFFB4934, 0xFFB8BB26, 0xFFFABD2F,
            0xFF83A598, 0xFFD3869B, 0xFF8EC07C, 0xFFFBF1C7
        )
    )

    val Monokai = TerminalColorScheme(
        name = "Monokai",
        background = 0xFF272822,
        foreground = 0xFFF8F8F2,
        cursor = 0xFFF8F8F0,
        selection = 0x4449483E,
        ansiColors = listOf(
            0xFF272822, 0xFFF92672, 0xFFA6E22E, 0xFFF4BF75,
            0xFF66D9EF, 0xFFAE81FF, 0xFFA1EFE4, 0xFFF8F8F2,
            0xFF75715E, 0xFFF92672, 0xFFA6E22E, 0xFFF4BF75,
            0xFF66D9EF, 0xFFAE81FF, 0xFFA1EFE4, 0xFFF9F8F5
        )
    )

    fun all() = listOf(Default, Dracula, SolarizedDark, Nord, GruvboxDark, Monokai)
}
