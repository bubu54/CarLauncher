package com.carlauncher.joying.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.carlauncher.joying.ui.theme.clocks.ClockRegistry

/** Popup de selección de reloj (pulsación larga sobre el widget RELOJ). */
@Composable
fun ClockPickerPopup(
    selectedId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val items = remember {
        ClockRegistry.all.map { clock ->
            PickerItem(
                id = clock.id,
                name = clock.name,
                caption = "TOCA PARA USAR"
            ) {
                // El reloj real, a su tamaño nativo, reducido
                ScaledPreview(nativeSize = clock.nativeHeight.dp) {
                    clock.content()
                }
            }
        }
    }

    StylePickerPopup(
        title = "ELEGIR RELOJ",
        hint = "Desliza para ver más relojes",
        items = items,
        selectedId = selectedId,
        onSelect = onSelect,
        onDismiss = onDismiss
    )
}
