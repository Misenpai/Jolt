package com.example.jolt.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface Dimensions {
    val paddingTooSmall: Dp
    val paddingExtraSmall: Dp
    val paddingSmall: Dp
    val paddingNormal: Dp
    val paddingLarge: Dp
    val paddingExtraLarge: Dp
    val normalButtonHeight: Dp
    val minButtonWidth: Dp
}

val normalDimensions: Dimensions = object : Dimensions {
    override val paddingTooSmall: Dp = 2.dp
    override val paddingExtraSmall: Dp = 4.dp
    override val paddingSmall: Dp = 8.dp
    override val paddingNormal: Dp = 16.dp
    override val paddingLarge: Dp = 24.dp
    override val paddingExtraLarge: Dp = 32.dp
    override val normalButtonHeight: Dp = 56.dp
    override val minButtonWidth: Dp = 120.dp
}

val LocalAppDimens = staticCompositionLocalOf { normalDimensions }

object AppTheme {
    val dimens: Dimensions
        @Composable
        get() = LocalAppDimens.current
}