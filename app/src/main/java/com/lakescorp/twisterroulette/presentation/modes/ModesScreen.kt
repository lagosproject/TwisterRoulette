package com.lakescorp.twisterroulette.presentation.modes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lakescorp.twisterroulette.R
import com.lakescorp.twisterroulette.domain.model.AppSettings
import com.lakescorp.twisterroulette.domain.model.GameModeType
import com.lakescorp.twisterroulette.domain.model.TwisterColor
import com.lakescorp.twisterroulette.presentation.main.gameColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModesScreen(
    viewModel: ModesViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameModeType.entries.forEach { mode ->
            ModeCard(
                title = stringResource(mode.nameResId),
                description = stringResource(mode.descResId),
                selected = settings.gameMode == mode,
                onClick = { viewModel.selectMode(mode) },
                settingsContent = if (settings.gameMode == mode) {
                    { ModeSettings(mode = mode, settings = settings, viewModel = viewModel) }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModeSettings(
    mode: GameModeType,
    settings: AppSettings,
    viewModel: ModesViewModel
) {
    when (mode) {
        GameModeType.CLASSIC -> {
            ModeSettingInfo(text = stringResource(R.string.mode_classic_info))
        }
        GameModeType.ONE_COLOR -> {
            ModeSettingLabel(stringResource(R.string.mode_one_color_setting))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TwisterColor.entries.forEach { color ->
                    val isSelected = settings.oneColorColor == color
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(gameColor(color))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable { viewModel.setOneColor(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                painter = painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_check),
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
        GameModeType.REDUCING -> {
            val maxMinColors = settings.enabledColors.size.coerceAtLeast(2)
            val currentMinColors = settings.reducingMinColors.coerceIn(1, maxMinColors)
            if (currentMinColors != settings.reducingMinColors) {
                viewModel.setReducingMinColors(currentMinColors)
            }

            ModeSettingLabel(stringResource(R.string.mode_reducing_turns))
            NumberSelector(
                value = settings.reducingTurnsPerDrop,
                onValueChange = { viewModel.setReducingTurnsPerDrop(it) },
                minValue = 1,
                maxValue = 10
            )

            ModeSettingLabel(stringResource(R.string.mode_reducing_min_colors))
            NumberSelector(
                value = currentMinColors,
                onValueChange = { viewModel.setReducingMinColors(it) },
                minValue = 1,
                maxValue = maxMinColors
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.mode_reducing_loop),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = settings.reducingLoop,
                        onCheckedChange = { viewModel.setReducingLoop(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.secondary,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        GameModeType.CHALLENGE -> {
            ModeSettingInfo(text = stringResource(R.string.mode_challenge_info))
        }
        GameModeType.SEQUENCE -> {
            ModeSettingLabel(stringResource(R.string.mode_sequence_length))
            NumberSelector(
                value = settings.sequenceLength,
                onValueChange = { viewModel.setSequenceLength(it) },
                minValue = 2,
                maxValue = 6
            )
        }
    }
}

@Composable
private fun ModeSettingLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(
    options: List<Int>,
    selected: Int,
    label: (Int) -> String,
    onSelected: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { value ->
            val isSelected = selected == value
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(value) },
                label = { Text(label(value)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    selectedLabelColor = MaterialTheme.colorScheme.onBackground
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = MaterialTheme.colorScheme.secondary,
                    selectedBorderWidth = 2.dp
                )
            )
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    settingsContent: (@Composable () -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(24.dp),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        } else {
            null
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            AnimatedVisibility(
                visible = selected && settingsContent != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    settingsContent?.invoke()
                }
            }
        }
    }
}

@Composable
private fun ModeSettingInfo(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )
}

@Composable
private fun NumberSelector(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val minusEnabled = value > minValue
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (minusEnabled) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                )
                .clickable(enabled = minusEnabled) { onValueChange(value - 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_minus),
                contentDescription = "Decrease",
                tint = if (minusEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        val plusEnabled = value < maxValue
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (plusEnabled) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                )
                .clickable(enabled = plusEnabled) { onValueChange(value + 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_plus),
                contentDescription = "Increase",
                tint = if (plusEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
