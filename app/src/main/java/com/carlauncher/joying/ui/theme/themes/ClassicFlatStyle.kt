package com.carlauncher.joying.ui.theme.themes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.carlauncher.joying.ui.Background
import com.carlauncher.joying.ui.BlueBorder
import com.carlauncher.joying.ui.ButtonBackground
import com.carlauncher.joying.ui.Panel
import com.carlauncher.joying.ui.White

/** Estilo original: fondo azul marino y botones planos. */
object ClassicFlatStyle : LauncherStyle {

    override val id = "classic_flat"
    override val name = "Clásico plano"

    override val buttonContentColor = White
    override val buttonDisabledContentColor = androidx.compose.ui.graphics.Color(0xFF829BB6)
    override val buttonTextShadow: Shadow? = null

    override fun screenBackground(modifier: Modifier): Modifier =
        modifier.background(Background)

    override fun buttonSurface(modifier: Modifier, shape: Shape, pressed: Boolean): Modifier =
        modifier
            .clip(shape)
            .background(ButtonBackground)
            .border(1.dp, BlueBorder, shape)

    override fun panelSurface(modifier: Modifier, shape: Shape): Modifier =
        modifier
            .clip(shape)
            .background(Panel)
            .border(1.dp, BlueBorder, shape)
}
