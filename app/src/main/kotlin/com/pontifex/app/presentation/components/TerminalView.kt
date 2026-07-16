package com.pontifex.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pontifex.app.domain.model.TerminalLine

@Composable
fun TerminalView(
    lines: List<TerminalLine>,
    fontSize: Int = 14,
    fontFamily: FontFamily = FontFamily.Monospace,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val textMeasurer = rememberTextMeasurer()
    var autoScroll by remember { mutableStateOf(true) }
    val onSurface = MaterialTheme.colorScheme.onSurface

    LaunchedEffect(lines.size) {
        if (autoScroll && lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                autoScroll = lastVisible >= lines.size - 2
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
    ) {
        items(lines) { line ->
            TerminalLineComposable(
                line = line,
                fontSize = fontSize,
                fontFamily = fontFamily,
                textMeasurer = textMeasurer,
                onSurfaceColor = onSurface
            )
        }
    }
}

@Composable
private fun TerminalLineComposable(
    line: TerminalLine,
    fontSize: Int,
    fontFamily: FontFamily,
    textMeasurer: TextMeasurer,
    onSurfaceColor: Color
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
            .background(MaterialTheme.colorScheme.surface)
    ) {
        var xOffset = 0f
        val yOffset = size.height / 2 - fontSize.sp.toPx() / 2

        line.segments.forEach { segment ->
            val segmentStyle = style.copy(
                color = segment.foreground.takeIf { it != Color.Unspecified } ?: onSurfaceColor,
                fontWeight = if (segment.bold) FontWeight.Bold else FontWeight.Normal
            )

            val measured = textMeasurer.measure(segment.text, segmentStyle)

            if (segment.background != Color.Unspecified) {
                drawRect(
                    color = segment.background,
                    topLeft = Offset(xOffset, 0f),
                    size = androidx.compose.ui.geometry.Size(
                        measured.size.width.toFloat(),
                        size.height
                    ),
                    style = Fill
                )
            }

            drawText(
                textLayoutResult = measured,
                topLeft = Offset(xOffset, yOffset)
            )

            xOffset += measured.size.width.toFloat()
        }
    }
}
