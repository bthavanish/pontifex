package com.pontifex.app.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pontifex.app.service.terminal.PontifexTerminalSession

private data class ExtraKey(
    val label: String,
    val action: String,
    val isModifier: Boolean = false
)

private val row1Keys = listOf(
    ExtraKey("ESC", "\u001b"),
    ExtraKey("TAB", "\t"),
    ExtraKey("CTRL", "CTRL", isModifier = true),
    ExtraKey("ALT", "ALT", isModifier = true),
    ExtraKey("\u2191", "\u001b[A"),
    ExtraKey("\u2193", "\u001b[B"),
    ExtraKey("\u2190", "\u001b[D"),
    ExtraKey("\u2192", "\u001b[C")
)

private val row2Keys = listOf(
    ExtraKey("-", "-"),
    ExtraKey("_", "_"),
    ExtraKey("|", "|"),
    ExtraKey("&", "&"),
    ExtraKey("'", "'"),
    ExtraKey("\"", "\""),
    ExtraKey(";", ";"),
    ExtraKey("(", "("),
    ExtraKey(")", ")"),
    ExtraKey("{", "{"),
    ExtraKey("}", "}"),
    ExtraKey("[", "["),
    ExtraKey("]", "]"),
    ExtraKey("#", "#"),
    ExtraKey("$", "$"),
    ExtraKey("~", "~"),
    ExtraKey("/", "/"),
    ExtraKey("\\", "\\"),
    ExtraKey("*", "*"),
    ExtraKey("%", "%"),
    ExtraKey("+", "+"),
    ExtraKey("=", "="),
    ExtraKey("<", "<"),
    ExtraKey(">", ">")
)

@Composable
fun ExtraKeysRow(
    session: PontifexTerminalSession?,
    modifier: Modifier = Modifier
) {
    var ctrlLatched by remember { mutableStateOf(false) }
    var altLatched by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
        ) {
            items(row1Keys) { key ->
                ExtraKeyButton(
                    key = key,
                    ctrlLatched = ctrlLatched,
                    altLatched = altLatched,
                    session = session,
                    onCtrlToggle = { ctrlLatched = !ctrlLatched },
                    onAltToggle = { altLatched = !altLatched },
                    onConsume = { ctrlLatched = false; altLatched = false }
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
        ) {
            items(row2Keys) { key ->
                ExtraKeyButton(
                    key = key,
                    ctrlLatched = ctrlLatched,
                    altLatched = altLatched,
                    session = session,
                    onCtrlToggle = { ctrlLatched = !ctrlLatched },
                    onAltToggle = { altLatched = !altLatched },
                    onConsume = { ctrlLatched = false; altLatched = false }
                )
            }
        }
    }
}

@Composable
private fun ExtraKeyButton(
    key: ExtraKey,
    ctrlLatched: Boolean,
    altLatched: Boolean,
    session: PontifexTerminalSession?,
    onCtrlToggle: () -> Unit,
    onAltToggle: () -> Unit,
    onConsume: () -> Unit
) {
    val isSelected = when {
        key.label == "CTRL" -> ctrlLatched
        key.label == "ALT" -> altLatched
        else -> false
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "keyScale"
    )

    FilterChip(
        selected = isSelected,
        onClick = {
            isPressed = true
            when (key.label) {
                "CTRL" -> onCtrlToggle()
                "ALT" -> onAltToggle()
                else -> {
                    val prefix = buildString {
                        if (ctrlLatched) append("\u0003")
                        if (altLatched) append("\u001b")
                    }
                    session?.write(prefix + key.action)
                    onConsume()
                }
            }
        },
        label = {
            Text(
                text = key.label,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}
