package com.pontifex.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ExtraKey(
    val label: String,
    val action: String,
    val isModifier: Boolean = false
)

@Composable
fun ExtraKeysRow(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var ctrlLatched by remember { mutableStateOf(false) }
    var altLatched by remember { mutableStateOf(false) }

    val keys = listOf(
        ExtraKey("ESC", "\u001b"),
        ExtraKey("TAB", "\t"),
        ExtraKey("CTRL", "CTRL", isModifier = true),
        ExtraKey("ALT", "ALT", isModifier = true),
        ExtraKey("FN", "FN"),
        ExtraKey("|", "|"),
        ExtraKey("/", "/"),
        ExtraKey("\\", "\\"),
        ExtraKey("-", "-"),
        ExtraKey("_", "_"),
        ExtraKey("~", "~"),
        ExtraKey("\u2191", "\u001b[A"),
        ExtraKey("\u2193", "\u001b[B"),
        ExtraKey("\u2190", "\u001b[D"),
        ExtraKey("\u2192", "\u001b[C"),
        ExtraKey("HOME", "\u001b[H"),
        ExtraKey("END", "\u001b[F"),
        ExtraKey("PGUP", "\u001b[5~"),
        ExtraKey("PGDN", "\u001b[6~")
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(vertical = 4.dp)
    ) {
        items(keys) { key ->
            val isSelected = when {
                key.label == "CTRL" -> ctrlLatched
                key.label == "ALT" -> altLatched
                else -> false
            }

            FilterChip(
                selected = isSelected,
                onClick = {
                    when (key.label) {
                        "CTRL" -> ctrlLatched = !ctrlLatched
                        "ALT" -> altLatched = !altLatched
                        else -> {
                            val prefix = buildString {
                                if (ctrlLatched) append("\u0003")
                                if (altLatched) append("\u001b")
                            }
                            onKeyPress(prefix + key.action)
                            ctrlLatched = false
                            altLatched = false
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
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
