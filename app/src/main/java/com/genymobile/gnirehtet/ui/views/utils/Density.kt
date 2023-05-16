package com.genymobile.gnirehtet.ui.views.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

val TextUnit.asDp: Dp
    @Composable get() = with(LocalDensity.current) {
        return this@asDp.toDp()
    }
