package com.anonymous.peep.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.anonymous.peep.ui.theme.PeepBlack
import com.anonymous.peep.ui.theme.PeepMuted
import com.anonymous.peep.ui.theme.PeepSurface
import com.anonymous.peep.ui.theme.PeepSurfaceBorder
import com.anonymous.peep.ui.theme.PeepWhite

@Composable
fun FriendCard(
    name: String,
    status: String?,
    onPeep: () -> Unit,
    peepsRemaining: Int,
    isPeeping: Boolean = false,
    cooldownSeconds: Int = 0,
) {
    val isOnCooldown = cooldownSeconds > 0
    val isDisabled = peepsRemaining <= 0 || isOnCooldown || isPeeping

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(color = PeepSurface, shape = RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PeepSurfaceBorder),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = PeepWhite
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = PeepWhite
            )
            Text(
                text = status ?: "Offline",
                style = MaterialTheme.typography.bodySmall,
                color = PeepMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Peep Button
        Button(
            onClick = onPeep,
            enabled = !isDisabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = PeepWhite,
                contentColor = PeepBlack,
                disabledContainerColor = PeepSurfaceBorder,
                disabledContentColor = PeepMuted
            ),
            shape = RoundedCornerShape(20.dp),
        ) {
            if (isOnCooldown) {
                Text(
                    text = "${cooldownSeconds}s",
                    style = MaterialTheme.typography.labelMedium
                )
            } else if (isPeeping) {
                Text(
                    text = "...",
                    style = MaterialTheme.typography.labelMedium
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Peep",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (peepsRemaining > 0) "Peep" else "Limit",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
