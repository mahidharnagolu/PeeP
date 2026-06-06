package com.anonymous.peep.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anonymous.peep.ui.components.FriendCard
import com.anonymous.peep.ui.components.PeepToast
import com.anonymous.peep.ui.theme.PeepBlack
import com.anonymous.peep.ui.theme.PeepWhite
import com.anonymous.peep.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToFriends: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PeeP.",
                    style = MaterialTheme.typography.headlineLarge,
                )
            }

            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.loadData() },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.friends.isEmpty() && !state.isLoading) {
                    // Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Wow, it's really calm in here!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PeepWhite
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Add some friends to start peeping.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateToFriends,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PeepWhite,
                                contentColor = PeepBlack
                            )
                        ) {
                            Text("Add Friends", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                } else {
                    // Friends List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        items(state.friends, key = { it.profile.id }) { friendUi ->
                            FriendCard(
                                name = friendUi.profile.username,
                                status = friendUi.status?.friendlyName,
                                onPeep = { viewModel.peepFriend(friendUi.profile.id) },
                                peepsRemaining = state.peepsRemaining,
                                isPeeping = friendUi.isPeeping,
                                cooldownSeconds = friendUi.cooldownSeconds,
                            )
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onNavigateToFriends,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = PeepWhite,
            contentColor = PeepBlack,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Friends", modifier = Modifier.size(24.dp))
        }

        // Toast
        PeepToast(
            message = state.toastMessage ?: "",
            visible = state.toastMessage != null,
            onHide = { viewModel.hideToast() }
        )
    }
}
