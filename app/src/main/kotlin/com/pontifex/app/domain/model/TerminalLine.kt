package com.pontifex.app.domain.model

data class TerminalLine(
    val segments: List<StyledSegment>,
    val isPrompt: Boolean = false
)
