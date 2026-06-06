package com.anonymous.peep.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.anonymous.peep.ui.screens.auth.LoginScreen
import com.anonymous.peep.ui.screens.auth.SignupScreen
import com.anonymous.peep.ui.screens.friends.FriendsScreen
import com.anonymous.peep.ui.screens.home.HomeScreen
import com.anonymous.peep.ui.screens.notifications.NotificationsScreen
import com.anonymous.peep.ui.screens.profile.ProfileScreen
import com.anonymous.peep.ui.theme.PeepBlack
import com.anonymous.peep.ui.theme.PeepMuted
import com.anonymous.peep.ui.theme.PeepSurfaceBorder
import com.anonymous.peep.ui.theme.PeepWhite
import com.anonymous.peep.viewmodel.AuthViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Signup : Screen("signup", "Signup")
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Notifications : Screen("notifications", "Alerts", Icons.Filled.Notifications)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object Friends : Screen("friends", "Friends")
}

val BottomNavItems = listOf(Screen.Home, Screen.Notifications, Screen.Profile)

@Composable
fun PeepNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.state.collectAsState()

    if (!authState.isInitialized) return

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) // Clear back stack
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (authState.isLoggedIn) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Hide bottom bar on nested screens like Friends
                if (currentDestination?.route in BottomNavItems.map { it.route }) {
                    NavigationBar(
                        containerColor = PeepBlack,
                        contentColor = PeepWhite,
                        tonalElevation = 0.dp
                    ) {
                        BottomNavItems.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PeepWhite,
                                    unselectedIconColor = PeepMuted,
                                    indicatorColor = PeepSurfaceBorder
                                ),
                                alwaysShowLabel = false
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (authState.isLoggedIn) Screen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToSignup = { navController.navigate(Screen.Signup.route) }
                )
            }
            composable(Screen.Signup.route) {
                SignupScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { navController.navigateUp() }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToFriends = { navController.navigate(Screen.Friends.route) }
                )
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(viewModel = hiltViewModel())
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    profileViewModel = hiltViewModel(),
                    authViewModel = authViewModel
                )
            }
            composable(Screen.Friends.route) {
                FriendsScreen(
                    viewModel = hiltViewModel(),
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }
    }
}
