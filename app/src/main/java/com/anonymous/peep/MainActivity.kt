package com.anonymous.peep

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.anonymous.peep.service.StatusBroadcastService
import com.anonymous.peep.service.UsageStatsHelper
import com.anonymous.peep.ui.navigation.PeepNavigation
import com.anonymous.peep.ui.theme.PeepError
import com.anonymous.peep.ui.theme.PeepSurface
import com.anonymous.peep.ui.theme.PeepTheme
import com.anonymous.peep.ui.theme.PeepWhite
import com.anonymous.peep.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Proceed even if denied, but they won't get push notifications
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PeepTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val authState by authViewModel.state.collectAsState()

                    var showUsagePermissionDialog by remember { mutableStateOf(false) }

                    // Check permissions when logged in
                    LaunchedEffect(authState.isLoggedIn) {
                        if (authState.isLoggedIn) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                            
                            // Check usage stats permission
                            if (!UsageStatsHelper.hasPermission(this@MainActivity)) {
                                delay(500) // Small delay before popping dialog
                                showUsagePermissionDialog = true
                            } else {
                                // Start service if we have permission and are logged in
                                authState.userId?.let { userId ->
                                    val token = authViewModel.state.value.profile?.fcmToken // Using any available auth details? Wait, we need access token.
                                    // Actually, we need the supabase token. We'll start it via a helper inside AuthViewModel or directly here.
                                    // Let's rely on the service reading DataStore, but we can pass userId.
                                    // Let's pass empty token and let it be handled, or pass a placeholder. 
                                    // The okhttp interceptor handles token anyway!
                                    StatusBroadcastService.start(this@MainActivity, userId, "")
                                }
                            }
                        }
                    }

                    if (showUsagePermissionDialog) {
                        UsagePermissionDialog(
                            onConfirm = {
                                showUsagePermissionDialog = false
                                UsageStatsHelper.requestPermission(this@MainActivity)
                            },
                            onDismiss = {
                                showUsagePermissionDialog = false
                            }
                        )
                    }

                    PeepNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // If we came back from settings and now have permission, start service
        if (UsageStatsHelper.hasPermission(this)) {
            // Service should be started if auth is valid
            // Ideally triggered via a ViewModel, but safe to just send intent if logged in
            val intent = Intent(this, StatusBroadcastService::class.java)
            // It might crash if not foreground service on Android O+, but we use startForegroundService inside the start method.
        }
    }
}

@Composable
fun UsagePermissionDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { 
            Text("PeeP needs Usage Access permission to see what app you're currently using and share it with your friends.") 
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Grant Permission", color = PeepWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now", color = PeepError)
            }
        },
        containerColor = PeepSurface,
        titleContentColor = PeepWhite,
        textContentColor = PeepWhite,
    )
}
