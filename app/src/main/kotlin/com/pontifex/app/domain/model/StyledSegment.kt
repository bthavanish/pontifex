package com.pontifex.app.domain.model

import androidx.compose.ui.graphics.Color

data class StyledSegment(
    val text: String,
    val foreground: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val bold: Boolean = false,
    val dim: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val blink: Boolean = false,
    val inverse: Boolean = false,
    val strikethrough: Boolean = false
)
