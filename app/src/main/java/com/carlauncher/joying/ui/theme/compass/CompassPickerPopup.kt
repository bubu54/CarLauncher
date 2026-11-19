package com.carlauncher.joying.ui.theme.compass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.carlauncher.joying.ui.PickerItem
import com.carlauncher.joying.ui.StylePickerPopup

/** Popup de selección de brújula (pulsación larga sobre el widget BRÚJULA). */
@Composable
fun CompassPickerPopup(
    selectedId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val items = remember {
        CompassRegistry.all.map { compass ->
            PickerItem(
                id = compass.id,
                name = compass.name,
                caption = compass.caption,
                preview = compass.preview
            )
        }
    }

    StylePickerPopup(
        title = "ELEGIR BRÚJULA",
        hint = "Desliza para ver más brújulas",
        items = items,
        selectedId = selectedId,
        onSelect = onSelect,
        onDismiss = onDismiss
    )
}
