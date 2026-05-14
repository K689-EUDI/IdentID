/*
 * Copyright (c) 2025 European Commission
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
package com.k689.identid.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.k689.identid.theme.sets.ThemeSet
import com.k689.identid.theme.templates.ThemeColorsTemplate
import com.k689.identid.theme.templates.ThemeColorsTemplate.Companion.toColorScheme
import com.k689.identid.theme.templates.ThemeDimensTemplate
import com.k689.identid.theme.templates.ThemeShapesTemplate
import com.k689.identid.theme.templates.ThemeShapesTemplate.Companion.toShapes
import com.k689.identid.theme.templates.ThemeTypographyTemplate
import com.k689.identid.theme.templates.ThemeTypographyTemplate.Companion.toTypography
import com.k689.identid.theme.values.ThemeColors
import com.k689.identid.theme.values.ThemeShapes
import com.k689.identid.theme.values.ThemeTypography

class ThemeManager {
    /**
     * Contains the data of the theme like colors, shapes, typography etc.
     */
    lateinit var set: ThemeSet
        private set

    /**
     * Defines if dynamic theming is supported. Notice that Dynamic color is available on Android 12+.
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    val dynamicThemeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    @Composable
    fun Theme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        disableDynamicTheming: Boolean = false,
        seedColor: Color? = null,
        themeStyle: ThemeStyle = ThemeStyle.TONAL,
        isOledMode: Boolean = false,
        useDynamicColor: Boolean = true,
        content: @Composable () -> Unit,
    ) {
        val lightColorScheme = set.lightColors
        val darkColorScheme = set.darkColors

        val colorScheme =
            when {
                !disableDynamicTheming && dynamicThemeSupported && useDynamicColor -> {
                    val context = LocalContext.current
                    if (darkTheme) {
                        dynamicDarkColorScheme(context)
                    } else {
                        dynamicLightColorScheme(context)
                    }
                }

                seedColor != null -> {
                    generateColorSchemeFromSeed(
                        seed = seedColor,
                        isDark = darkTheme,
                        base = if (darkTheme) darkColorScheme else lightColorScheme,
                        isOledMode = isOledMode,
                        style = themeStyle,
                    )
                }

                darkTheme -> {
                    set.darkColors
                }

                else -> {
                    set.lightColors
                }
            }

        set = set.copy(isInDarkMode = darkTheme)

        MaterialTheme(
            colorScheme = colorScheme,
            shapes = set.shapes,
            typography = set.typo,
            content = content,
        )
    }

    companion object {
        /**
         * Private instance of manager.
         */
        private lateinit var _instance: ThemeManager

        /**
         * Instance of theme manager. Built from [Builder].
         */
        val instance: ThemeManager
            get() {
                if (this::_instance.isInitialized.not()) {
                    _instance =
                        Builder()
                            .withLightColors(ThemeColors.lightColors)
                            .withDarkColors(ThemeColors.darkColors)
                            .withTypography(ThemeTypography.typo)
                            .withShapes(ThemeShapes.shapes)
                            .withDimensions(
                                ThemeDimensTemplate(
                                    screenPadding = 10.0,
                                ),
                            ).build()
                }

                return _instance
            }

        /**
         * Initializes the theme manager using the builder provided. This initializes the
         * static instance of the manager.
         */
        fun ThemeManager.build(builder: Builder): ThemeManager {
            set =
                ThemeSet(
                    isInDarkMode = builder.isInDarkMode == true,
                    lightColors = builder.lightColors.toColorScheme(),
                    darkColors = builder.darkColors.toColorScheme(),
                    typo = builder.typography.toTypography(),
                    shapes = builder.shapes.toShapes(),
                    dimens = builder.dimensions,
                )
            return this
        }
    }

    class Builder(
        val isInDarkMode: Boolean? = null,
    ) {
        lateinit var lightColors: ThemeColorsTemplate
        lateinit var darkColors: ThemeColorsTemplate
        lateinit var typography: ThemeTypographyTemplate
        lateinit var shapes: ThemeShapesTemplate
        lateinit var dimensions: ThemeDimensTemplate

        /**
         * Set the colors set for the theme configuration. These colors refer to the light theme
         * colors. You can set the dark mode colors by calling [withDarkColors]. If no dark colors are
         * set, light will be used for both modes.
         *
         * @param colors Set of colors to be used to construct the theme.
         *
         * @return This instance for chaining.
         */
        fun withLightColors(colors: ThemeColorsTemplate): Builder {
            this.lightColors = colors
            return this
        }

        /**
         * Set the colors set for the theme configuration. These colors refer to the light theme
         * colors. You can set the dark mode colors by calling [withDarkColors]. If no dark colors are
         * set, light will be used for both modes.
         *
         * @param colors Set of colors to be used to construct the theme.
         *
         * @return This instance for chaining.
         */
        fun withDarkColors(colors: ThemeColorsTemplate): Builder {
            this.darkColors = colors
            return this
        }

        /**
         * Set the typography for theme.
         *
         * @param typography Set of typography to be used to construct the theme.
         *
         * @return This instance for chaining.
         */
        fun withTypography(typography: ThemeTypographyTemplate): Builder {
            this.typography = typography
            return this
        }

        /**
         * Set the shapes for theme.
         *
         * @param shapes Set of shapes to be used to construct the theme.
         *
         * @return This instance for chaining.
         */
        fun withShapes(shapes: ThemeShapesTemplate): Builder {
            this.shapes = shapes
            return this
        }

        /**
         * Set the dimensions for theme.
         *
         * @param dimens Set of dimensions to be used to construct the theme.
         *
         * @return This instance for chaining.
         */
        fun withDimensions(dimens: ThemeDimensTemplate): Builder {
            this.dimensions = dimens
            return this
        }

        fun build(
            buildStatic: Boolean = true,
        ): ThemeManager {
            // Check light colors.
            if (this::lightColors.isInitialized.not()) {
                throw RuntimeException("lightColors is not initialized. Can not build theme manager.")
            }

            // If dark colors not initialized, set as light.
            if (this::darkColors.isInitialized.not()) {
                darkColors = lightColors
            }

            // Check typography.
            if (this::typography.isInitialized.not()) {
                throw RuntimeException("typography is not initialized. Can not build theme manager.")
            }

            // Check shapes.
            if (this::shapes.isInitialized.not()) {
                throw RuntimeException("shapes is not initialized. Can not build theme manager.")
            }

            // Check dimensions.
            if (this::dimensions.isInitialized.not()) {
                throw RuntimeException("dimensions is not initialized. Can not build theme manager.")
            }

            // Initialize instance.
            if (buildStatic) _instance = ThemeManager()

            // Initialize manager.
            return when (buildStatic) {
                true -> _instance.build(this)
                false -> ThemeManager().build(this)
            }
        }
    }

    private fun generateColorSchemeFromSeed(
        seed: Color,
        isDark: Boolean,
        base: ColorScheme,
        isOledMode: Boolean = false,
        style: ThemeStyle = ThemeStyle.TONAL,
    ): ColorScheme {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(seed.toArgb(), hsv)
        val hue = hsv[0]
        val saturation = hsv[1]
        val value = hsv[2]

        fun fromHsv(h: Float, s: Float, v: Float): Color {
            return Color(android.graphics.Color.HSVToColor(floatArrayOf(h % 360f, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))))
        }

        val toneSurface =
            if (isDark) {
                if (isOledMode) Color.Black else Color(0xFF1A1C1E) // Standard M3 Dark Surface (Tone 6-10)
            } else {
                Color.White
            }

        // Adjust palettes based on style
        val primaryHue = hue
        val secondaryHue = when (style) {
            ThemeStyle.ANALOGOUS -> hue + 30f
            ThemeStyle.VIBRANT -> hue + 45f
            ThemeStyle.EXPRESSIVE -> hue + 90f
            else -> hue
        }
        val tertiaryHue = when (style) {
            ThemeStyle.ANALOGOUS -> hue - 30f
            ThemeStyle.VIBRANT -> hue + 120f
            ThemeStyle.EXPRESSIVE -> hue + 180f
            else -> hue + 60f
        }

        val saturationMult = if (style == ThemeStyle.MONOCHROMATIC) 0.15f else 1f
        val contrastEnhance = if (style == ThemeStyle.VIBRANT || style == ThemeStyle.EXPRESSIVE) 1.3f else 1f

        return if (isDark) {
            val primary = fromHsv(primaryHue, (saturation * 0.5f * saturationMult * contrastEnhance).coerceIn(0f, 1f), 0.95f)
            val primaryContainer = fromHsv(primaryHue, (saturation * 0.4f * saturationMult).coerceIn(0f, 1f), 0.35f)
            val secondary = fromHsv(secondaryHue, (saturation * 0.4f * saturationMult * contrastEnhance).coerceIn(0f, 1f), 0.9f)
            val secondaryContainer = fromHsv(secondaryHue, (saturation * 0.3f * saturationMult).coerceIn(0f, 1f), 0.25f)
            val tertiary = fromHsv(tertiaryHue, (saturation * 0.4f * saturationMult * contrastEnhance).coerceIn(0f, 1f), 0.85f)

            // Tint surfaces with the selected primary hue to make styles more apparent
            val surfaceSeed = fromHsv(primaryHue, (saturation * 0.08f * saturationMult).coerceIn(0f, 1f), if (isOledMode) 0f else 0.08f)

            base.copy(
                primary = primary,
                onPrimary = Color.Black,
                primaryContainer = primaryContainer,
                onPrimaryContainer = fromHsv(primaryHue, 0.1f, 0.95f),
                secondary = secondary,
                onSecondary = Color.Black,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = fromHsv(secondaryHue, 0.1f, 0.9f),
                tertiary = tertiary,
                surface = lerp(surfaceSeed, toneSurface, 0.5f),
                onSurface = Color(0xFFE2E2E6),
                background = if (isOledMode) Color.Black else lerp(surfaceSeed, toneSurface, 0.7f),
                onBackground = Color(0xFFE2E2E6),
                surfaceVariant = lerp(surfaceSeed, Color(0xFF44474E), 0.6f),
                onSurfaceVariant = Color(0xFFC4C6D0),
                outline = lerp(surfaceSeed, Color(0xFF8E9099), 0.4f),
                surfaceContainerLowest = if (isOledMode) Color.Black else lerp(surfaceSeed, Color(0xFF0C0E11), 0.5f),
                surfaceContainerLow = if (isOledMode) Color.Black else lerp(surfaceSeed, Color(0xFF1A1C1E), 0.5f),
                surfaceContainer = if (isOledMode) Color.Black else lerp(surfaceSeed, Color(0xFF1E2023), 0.5f),
                surfaceContainerHigh = if (isOledMode) Color.Black else lerp(surfaceSeed, Color(0xFF282A2E), 0.5f),
                surfaceContainerHighest = if (isOledMode) Color.Black else lerp(surfaceSeed, Color(0xFF33353A), 0.5f),
            )
        } else {
            val primary = fromHsv(primaryHue, (saturation * 1.1f * saturationMult).coerceIn(0f, 1f), (value * 0.9f).coerceIn(0f, 1f))
            val primaryContainer = fromHsv(primaryHue, (saturation * 0.25f * saturationMult).coerceIn(0f, 1f), 0.92f)
            val secondary = fromHsv(secondaryHue, (saturation * 0.7f * saturationMult * contrastEnhance).coerceIn(0f, 1f), (value * 0.7f).coerceIn(0f, 1f))
            val secondaryContainer = fromHsv(secondaryHue, (saturation * 0.2f * saturationMult).coerceIn(0f, 1f), 0.94f)
            val tertiary = fromHsv(tertiaryHue, (saturation * 0.6f * saturationMult * contrastEnhance).coerceIn(0f, 1f), (value * 0.6f).coerceIn(0f, 1f))

            val surfaceSeed = fromHsv(primaryHue, (saturation * 0.04f * saturationMult).coerceIn(0f, 1f), 0.98f)

            base.copy(
                primary = primary,
                onPrimary = if (primary.luminance() > 0.5f) Color.Black else Color.White,
                primaryContainer = primaryContainer,
                onPrimaryContainer = fromHsv(primaryHue, (saturation * 0.8f).coerceIn(0f, 1f), 0.2f),
                secondary = secondary,
                onSecondary = Color.White,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = fromHsv(secondaryHue, (saturation * 0.8f).coerceIn(0f, 1f), 0.2f),
                tertiary = tertiary,
                surface = lerp(surfaceSeed, Color.White, 0.4f),
                onSurface = Color(0xFF1A1C1E),
                background = lerp(surfaceSeed, Color.White, 0.6f),
                onBackground = Color(0xFF1A1C1E),
                surfaceVariant = lerp(surfaceSeed, Color(0xFFE1E2EC), 0.6f),
                onSurfaceVariant = Color(0xFF44474E),
                outline = lerp(surfaceSeed, Color(0xFF74777F), 0.5f),
                surfaceContainerLowest = Color.White,
                surfaceContainerLow = lerp(surfaceSeed, Color(0xFFF7F3FA), 0.5f),
                surfaceContainer = lerp(surfaceSeed, Color(0xFFF1EEF7), 0.5f),
                surfaceContainerHigh = lerp(surfaceSeed, Color(0xFFEBE8F1), 0.5f),
                surfaceContainerHighest = lerp(surfaceSeed, Color(0xFFE5E2EB), 0.5f),
            )
        }
    }
}
