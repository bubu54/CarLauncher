package com.carlauncher.joying.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.carlauncher.joying.SystemActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class DrawerApp(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: ImageBitmap
)

/** Cajón con todas las aplicaciones instaladas (incluidos los ajustes del sistema). */
@Composable
fun AppDrawerScreen(onClose: () -> Unit) {

    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<DrawerApp>?>(null) }

    BackHandler(onBack = onClose)

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.Default) { loadApps(context) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            // Bloquea los toques hacia el launcher que hay debajo
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APLICACIONES",
                    color = White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        SystemActions.openSystemSettings(context)
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue,
                        contentColor = White
                    )
                ) {
                    Text("AJUSTES DEL SISTEMA")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonBackground,
                        contentColor = White
                    )
                ) {
                    Text("CERRAR")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val list = apps

            if (list == null) {
                Text(
                    text = "Cargando aplicaciones…",
                    color = Secondary,
                    fontSize = 15.sp
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 112.dp),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(list, key = { it.packageName + "/" + it.activityName }) { app ->
                        AppItem(
                            app = app,
                            onClick = {
                                SystemActions.launchApp(context, app.packageName, app.activityName)
                                onClose()
                            },
                            onLongClick = {
                                SystemActions.openAppInfo(context, app.packageName)
                                onClose()
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppItem(
    app: DrawerApp,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = app.icon,
            contentDescription = app.label,
            modifier = Modifier.size(60.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = app.label,
            color = White,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Suppress("DEPRECATION")
private fun loadApps(context: Context): List<DrawerApp> {

    val pm = context.packageManager

    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    return pm.queryIntentActivities(intent, 0)
        .filter { it.activityInfo.packageName != context.packageName }
        .mapNotNull { info ->
            try {
                DrawerApp(
                    label = info.loadLabel(pm).toString(),
                    packageName = info.activityInfo.packageName,
                    activityName = info.activityInfo.name,
                    icon = info.loadIcon(pm).toBitmap(96, 96).asImageBitmap()
                )
            } catch (e: Exception) {
                null
            }
        }
        .sortedBy { it.label.lowercase() }
}
