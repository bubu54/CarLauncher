
package com.carlauncher.joying.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import com.carlauncher.joying.ui.ButtonBackground
import com.carlauncher.joying.ui.BlueBorder
import com.carlauncher.joying.ui.White

@Composable
fun SideButtons(
    buttons: List<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        buttons.forEach { button ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(ButtonBackground)
                    .border(
                        width = 1.dp,
                        color = BlueBorder,
                        shape = RoundedCornerShape(11.dp)
                    )
                    .clickable {
                        Toast.makeText(
                            context,
                            "$button: función pendiente",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = button,
                    color = White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
