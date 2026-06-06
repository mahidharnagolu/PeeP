package com.anonymous.peep.ui.components

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.anonymous.peep.ui.theme.PeepSurface
import com.anonymous.peep.ui.theme.PeepSurfaceBorder
import kotlinx.coroutines.delay

@Composable
fun PeepToast(
    message: String,
    visible: Boolean,
    onHide: () -> Unit,
    durationMs: Long = 3000L,
) {
    val context = LocalContext.current

    LaunchedEffect(visible) {
        if (visible) {
            val vibrator = context.getSystemService(Vibrator::class.java)
            if (vibrator.hasVibrator()) {
                val timings = longArrayOf(0, 50, 30, 50)
                val amplitudes = intArrayOf(0, 255, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            }

            delay(durationMs)
            onHide()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(9999f)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(durationMillis = 300)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(14.dp),
                        spotColor = MaterialTheme.colorScheme.onBackground
                    )
                    .background(
                        color = PeepSurface.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        width = 0.5.dp,
                        color = PeepSurfaceBorder,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 14.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
