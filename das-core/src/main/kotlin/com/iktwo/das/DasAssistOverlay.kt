package com.iktwo.das

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iktwo.das.core.R

/**
 * Complete selection UI: a [TextSelectionLayer] with a close button and a
 * bottom card showing the selection, Select All / Deselect All and a confirm
 * button. Tapping outside every region calls [onClose].
 *
 * @param error shown instead of the hint when the screen text is unavailable.
 * @param title the card's title; defaults to a localized "Assistant".
 * @param actionLabel the confirm button's label; defaults to a localized "Use Text".
 */
@Composable
fun DasAssistOverlay(
    regions: List<TextRegion>,
    isSearching: Boolean,
    error: String?,
    onClose: () -> Unit,
    onConfirm: (String) -> Unit,
    theme: DasOverlayTheme = DasOverlayTheme(),
    title: String = stringResource(R.string.das_default_title),
    actionLabel: String = stringResource(R.string.das_default_action),
) {
    var selectedIndices by remember(regions) { mutableStateOf(emptySet<Int>()) }
    val selectedText = remember(selectedIndices, regions) { regions.textOf(selectedIndices) }
    val hasSelection = selectedText.isNotEmpty()
    val allSelected = regions.isNotEmpty() && selectedIndices.size == regions.size

    MaterialTheme(colorScheme = theme.colorScheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            TextSelectionLayer(
                regions = regions,
                selected = selectedIndices,
                onToggle = { index ->
                    selectedIndices =
                        if (index in selectedIndices) selectedIndices - index else selectedIndices + index
                },
                onTapOutside = onClose,
                theme = theme
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.das_close),
                    tint = Color.White
                )
            }

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (hasSelection) {
                        Text(
                            text = selectedText,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        )
                    } else {
                        Text(
                            text = when {
                                isSearching -> stringResource(R.string.das_analyzing)
                                error != null -> error
                                regions.isEmpty() -> stringResource(R.string.das_no_text)
                                else -> stringResource(R.string.das_tap_hint)
                            },
                            color = if (error != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedIndices = if (allSelected) emptySet() else regions.indices.toSet()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = regions.isNotEmpty(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(regions.isNotEmpty()).copy(
                                brush = SolidColor(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        ) {
                            Icon(
                                imageVector = if (allSelected) Icons.Default.ClearAll else Icons.Default.SelectAll,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(
                                    if (allSelected) R.string.das_deselect_all else R.string.das_select_all
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onConfirm(selectedText) },
                            modifier = Modifier.weight(1f),
                            enabled = hasSelection,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(actionLabel, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}
