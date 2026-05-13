/*
 * Copyright (c) 2026 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

package com.k689.identid.ui.dashboard.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.k689.identid.R
import com.k689.identid.theme.AppLanguage
import com.k689.identid.theme.AppTheme
import com.k689.identid.theme.ThemeStyle
import com.k689.identid.ui.component.AppIcons
import com.k689.identid.ui.component.ListItemDataUi
import com.k689.identid.ui.component.ListItemMainContentDataUi
import com.k689.identid.ui.component.ListItemTrailingContentDataUi
import com.k689.identid.ui.component.SectionTitle
import com.k689.identid.ui.component.content.ContentScreen
import com.k689.identid.ui.component.content.ScreenNavigateAction
import com.k689.identid.ui.component.content.ToolbarConfig
import com.k689.identid.ui.component.utils.SPACING_MEDIUM
import com.k689.identid.ui.component.utils.SPACING_SMALL
import com.k689.identid.ui.component.utils.VSpacer
import com.k689.identid.ui.component.wrap.SwitchDataUi
import com.k689.identid.ui.component.wrap.WrapListItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun PreferencesScreen(
    navController: NavController,
    viewModel: PreferencesViewModel,
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    ContentScreen(
        navigatableAction = ScreenNavigateAction.CANCELABLE,
        onBack = { viewModel.setEvent(Event.Pop) },
        toolBarConfig = ToolbarConfig(title = state.screenTitle),
    ) { paddingValues ->
        PreferencesContent(
            state = state,
            onEvent = { viewModel.setEvent(it) },
            paddingValues = paddingValues,
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect
            .onEach { effect ->
                when (effect) {
                    is Effect.Navigation.Pop -> navController.popBackStack()
                }
            }.collect()
    }
}

@Composable
private fun PreferencesContent(
    state: State,
    onEvent: (Event) -> Unit,
    paddingValues: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingValues,
    ) {
        item {
            ThemePreviewSection()
        }

        item {
            ThemeModeSection(state, onEvent)
        }

        item {
            ColorSection(state, onEvent)
        }

        item {
            LanguageSection(state, onEvent)
        }
    }
}

@Composable
private fun ThemePreviewSection() {
    SectionTitle(
        modifier = Modifier.padding(bottom = SPACING_SMALL.dp),
        text = stringResource(R.string.preferences_theme_preview_label),
    )
    ElevatedCard(
        modifier = Modifier.padding(horizontal = SPACING_MEDIUM.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(SPACING_MEDIUM.dp),
            verticalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
                Column {
                    Text(
                        text = "Main Heading",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Secondary supporting text",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp)) {
                ElevatedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Box(modifier = Modifier.padding(SPACING_SMALL.dp)) {
                        Text(text = "Primary", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                ElevatedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Box(modifier = Modifier.padding(SPACING_SMALL.dp)) {
                        Text(text = "Secondary", color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    }
    VSpacer.Medium()
}

@Composable
private fun ThemeModeSection(state: State, onEvent: (Event) -> Unit) {
    SectionTitle(
        modifier = Modifier.padding(bottom = SPACING_SMALL.dp),
        text = stringResource(R.string.preferences_theme_label)
    )
    Column(modifier = Modifier.padding(horizontal = SPACING_MEDIUM.dp)) {
        AppTheme.entries.forEach { theme ->
            WrapListItem(
                item = ListItemDataUi(
                    itemId = theme.name,
                    mainContentData = ListItemMainContentDataUi.Text(text = stringResource(theme.labelRes)),
                    trailingContentData = if (state.selectedTheme == theme) {
                        ListItemTrailingContentDataUi.Icon(iconData = AppIcons.Check)
                    } else null,
                ),
                onItemClick = { onEvent(Event.OnThemeSelected(theme)) },
            )
        }

        VSpacer.Small()

        WrapListItem(
            item = ListItemDataUi(
                itemId = "oled_mode",
                mainContentData = ListItemMainContentDataUi.Text(text = stringResource(R.string.preferences_oled_mode_label)),
                trailingContentData = ListItemTrailingContentDataUi.Switch(
                    switchData = SwitchDataUi(isChecked = state.isOledMode)
                ),
            ),
            onItemClick = { onEvent(Event.OnOledModeChanged(!state.isOledMode)) },
        )
    }
    VSpacer.Medium()
}

@Composable
private fun ColorSection(state: State, onEvent: (Event) -> Unit) {
    SectionTitle(
        modifier = Modifier.padding(bottom = SPACING_SMALL.dp),
        text = stringResource(R.string.preferences_seed_color_label)
    )
    Column(modifier = Modifier.padding(horizontal = SPACING_MEDIUM.dp)) {
        WrapListItem(
            item = ListItemDataUi(
                itemId = "dynamic_color",
                mainContentData = ListItemMainContentDataUi.Text(text = stringResource(R.string.preferences_dynamic_color_label)),
                trailingContentData = ListItemTrailingContentDataUi.Switch(
                    switchData = SwitchDataUi(isChecked = state.useDynamicColor)
                ),
            ),
            onItemClick = { onEvent(Event.OnUseDynamicColorChanged(!state.useDynamicColor)) },
        )

        if (!state.useDynamicColor) {
            VSpacer.Medium()
            Text(
                text = stringResource(R.string.preferences_color_style_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            VSpacer.Small()

            ThemeStyle.entries.forEach { style ->
                WrapListItem(
                    item = ListItemDataUi(
                        itemId = style.name,
                        mainContentData = ListItemMainContentDataUi.Text(text = stringResource(style.labelRes)),
                        trailingContentData = if (state.selectedThemeStyle == style) {
                            ListItemTrailingContentDataUi.Icon(iconData = AppIcons.Check)
                        } else null,
                    ),
                    onItemClick = { onEvent(Event.OnThemeStyleSelected(style)) },
                )
            }

            VSpacer.Medium()
            Text(
                text = stringResource(R.string.preferences_color_tuning_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            VSpacer.Small()

            TuningSlider(
                label = stringResource(R.string.preferences_hue_label),
                value = state.seedHue,
                onValueChange = { onEvent(Event.OnHueChanged(it)) },
                valueRange = 0f..360f,
            )
            TuningSlider(
                label = stringResource(R.string.preferences_saturation_label),
                value = state.seedSaturation,
                onValueChange = { onEvent(Event.OnSaturationChanged(it)) },
                valueRange = 0f..1f,
            )
            TuningSlider(
                label = stringResource(R.string.preferences_value_label),
                value = state.seedValue,
                onValueChange = { onEvent(Event.OnValueChanged(it)) },
                valueRange = 0f..1f,
            )
        }
    }
    VSpacer.Medium()
}

@Composable
private fun TuningSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}

@Composable
private fun LanguageSection(state: State, onEvent: (Event) -> Unit) {
    SectionTitle(
        modifier = Modifier.padding(bottom = SPACING_SMALL.dp),
        text = stringResource(R.string.preferences_language_label)
    )
    Column(modifier = Modifier.padding(horizontal = SPACING_MEDIUM.dp)) {
        AppLanguage.entries.forEach { language ->
            WrapListItem(
                item = ListItemDataUi(
                    itemId = language.name,
                    mainContentData = ListItemMainContentDataUi.Text(text = language.displayName),
                    trailingContentData = if (state.selectedLanguage == language) {
                        ListItemTrailingContentDataUi.Icon(iconData = AppIcons.Check)
                    } else null,
                ),
                onItemClick = { onEvent(Event.OnLanguageSelected(language)) },
            )
        }
    }
    VSpacer.Medium()
}
