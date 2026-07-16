package com.pontifex.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.termux.terminal.TerminalBuffer
import com.termux.terminal.TerminalRow
import com.termux.terminal.TerminalEmulator
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val ANSI_COLORS = intArrayOf(
    0xFF000000.toInt(), 0xFFCD3131.toInt(), 0xFF0DBC79.toInt(), 0xFFE5E510.toInt(),
    0xFF2472C8.toInt(), 0xFFBC3FBC.toInt(), 0xFF11A8CD.toInt(), 0xFFE5E5E5.toInt(),
    0xFF666666.toInt(), 0xFFF14C4C.toInt(), 0xFF23D18B.toInt(), 0xFFF5F543.toInt(),
    0xFF3B8EEA.toInt(), 0xFF555555.toInt(), 0xFF29B8DB.toInt(), 0xFFFFFFFF.toInt()
)

private val ANSI_BRIGHT_COLORS = intArrayOf(
    0xFF666666.toInt(), 0xFFF14C4C.toInt(), 0xFF23D18B.toInt(), 0xFFF5F543.toInt(),
    0xFF3B8EEA.toInt(), 0xFFD33682.toInt(), 0xFF29B8DB.toInt(), 0xFFFFFFFF.toInt()
)

@Composable
fun TerminalView(
    screen: TerminalBuffer?,
    fontSize: Int = 14,
    fontFamily: FontFamily = FontFamily.Monospace,
    modifier: Modifier = Modifier,
    onRequestCopy: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val textMeasurer = rememberTextMeasurer()
    var autoScroll by remember { mutableStateOf(true) }
    var currentFontSize by remember { mutableFloatStateOf(fontSize.toFloat()) }
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val scope = rememberCoroutineScope()

    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        currentFontSize = (currentFontSize * zoomChange).coerceIn(8f, 24f)
    }

    LaunchedEffect(screen) {
        if (autoScroll && screen != null) {
            val rows = screen.activeRows
            if (rows > 0) {
                listState.animateScrollToItem(rows - 1)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                autoScroll = lastVisible >= (screen?.activeRows ?: 0) - 2
            }
    }

    Box(
        modifier = modifier
            .background(surface)
            .transformable(state = transformableState)
    ) {
        val rows = screen?.activeRows ?: 0
        if (rows > 0) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = rows,
                    key = { it }
                ) { rowIndex ->
                    TerminalRowComposable(
                        rowIndex = rowIndex,
                        screen = screen!!,
                        fontSize = currentFontSize.roundToInt(),
                        fontFamily = fontFamily,
                        textMeasurer = textMeasurer,
                        onSurfaceColor = onSurface,
                        surfaceColor = surface,
                        cursorColor = primaryColor
                    )
                }
            }
        } else {
            // Empty state when no terminal session is active
            Text(
                text = "No active session. Tap + to create one.",
                style = TextStyle(
                    color = onSurface.copy(alpha = 0.5f),
                    fontSize = currentFontSize.roundToInt().sp,
                    fontFamily = fontFamily
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }

        if (!autoScroll && rows > 0) {
            SmallFloatingActionButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(rows - 1)
                        autoScroll = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(36.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = "Jump to bottom",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TerminalRowComposable(
    rowIndex: Int,
    screen: TerminalBuffer,
    fontSize: Int,
    fontFamily: FontFamily,
    textMeasurer: TextMeasurer,
    onSurfaceColor: Color,
    surfaceColor: Color,
    cursorColor: Color
) {
    val style = TextStyle(
        fontSize = fontSize.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height((fontSize * 1.4).dp)
            .background(surfaceColor)
    ) {
        val row = screen.getRow(rowIndex) ?: return@Canvas
        val lineHeight = size.height
        val cellWidth = textMeasurer.measure("W", style).size.width.toFloat()
        val cursorRow = screen.cursorRow

        var xOffset = 0f
        val chars = row.mText
        val styles = row.mStyle
        val columns = screen.columns

        for (col in 0 until columns) {
            if (col >= chars.size) break

            val char = chars[col]
            val textStyle = if (col < styles.size) styles[col] else 0L

            if (char == '\u0000' || char == ' ') {
                xOffset += cellWidth
                continue
            }

            val fgIndex = ((TextStyle.ForegroundColor(textStyle).toLong() and 0xFFFFFF) - 256).toInt()
            val bgIndex = ((TextStyle.BackgroundColor(textStyle).toLong() and 0xFFFFFF) - 256).toInt()

            val fgColor = when {
                fgIndex in 0..7 -> Color(ANSI_COLORS[fgIndex])
                fgIndex in 8..15 -> Color(ANSI_BRIGHT_COLORS[fgIndex - 8])
                fgIndex == TerminalEmulator.COLOR_INDEX_CURSOR -> cursorColor
                fgIndex == TerminalEmulator.COLOR_INDEX_FOREGROUND -> onSurfaceColor
                else -> onSurfaceColor
            }

            val bgColor = when {
                bgIndex in 0..7 -> Color(ANSI_COLORS[bgIndex])
                bgIndex in 8..15 -> Color(ANSI_BRIGHT_COLORS[bgIndex - 8])
                bgIndex == TerminalEmulator.COLOR_INDEX_BACKGROUND -> surfaceColor
                bgIndex == TerminalEmulator.COLOR_INDEX_CURSOR -> cursorColor
                else -> Color.Transparent
            }

            val isBold = TextStyle.isBold(textStyle)
            val isItalic = TextStyle.isItalic(textStyle)
            val isUnderline = TextStyle.isUnderline(textStyle)
            val isStrikethrough = TextStyle.isStrikethrough(textStyle)

            if (bgColor != Color.Transparent) {
                drawRect(
                    color = bgColor,
                    topLeft = Offset(xOffset, 0f),
                    size = androidx.compose.ui.geometry.Size(cellWidth, lineHeight),
                    style = Fill
                )
            }

            if (rowIndex == cursorRow && col == screen.cursorCol) {
                drawRect(
                    color = cursorColor.copy(alpha = 0.7f),
                    topLeft = Offset(xOffset, 0f),
                    size = androidx.compose.ui.geometry.Size(cellWidth, lineHeight),
                    style = Fill
                )
            }

            val charStyle = style.copy(
                color = fgColor,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = when {
                    isUnderline && isStrikethrough -> TextDecoration.combine(
                        listOf(TextDecoration.Underline, TextDecoration.LineThrough)
                    )
                    isUnderline -> TextDecoration.Underline
                    isStrikethrough -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                }
            )

            val measured = textMeasurer.measure(char.toString(), charStyle)
            val yOffset = lineHeight / 2 - measured.size.height / 2

            drawText(
                textLayoutResult = measured,
                topLeft = Offset(xOffset, yOffset)
            )

            xOffset += cellWidth
        }
    }
}
