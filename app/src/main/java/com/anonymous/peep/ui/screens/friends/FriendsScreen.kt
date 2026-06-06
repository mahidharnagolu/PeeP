package com.anonymous.peep.ui.screens.friends

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anonymous.peep.data.model.FriendRequestWithProfile
import com.anonymous.peep.data.model.Profile
import com.anonymous.peep.ui.components.PeepToast
import com.anonymous.peep.ui.theme.PeepBlack
import com.anonymous.peep.ui.theme.PeepMuted
import com.anonymous.peep.ui.theme.PeepSurface
import com.anonymous.peep.ui.theme.PeepSurfaceBorder
import com.anonymous.peep.ui.theme.PeepWhite
import com.anonymous.peep.viewmodel.FriendsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // Auto-hide toast after showing it
    LaunchedEffect(state.successMessage, state.error) {
        if (state.successMessage != null || state.error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("PeeP.", style = MaterialTheme.typography.headlineLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PeepBlack,
                    titleContentColor = PeepWhite,
                    navigationIconContentColor = PeepWhite,
                ),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)
            ) {
                // Search
                item {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = { Text("Search username...", color = MaterialTheme.colorScheme.secondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PeepSurface,
                            unfocusedContainerColor = PeepSurface,
                            focusedBorderColor = PeepWhite,
                            unfocusedBorderColor = PeepSurfaceBorder,
                            focusedTextColor = PeepWhite,
                            unfocusedTextColor = PeepWhite,
                            cursorColor = PeepWhite
                        ),
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = PeepMuted)
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Search Results
                if (state.searchQuery.isNotEmpty()) {
                    if (state.isSearching) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.padding(20.dp), color = PeepWhite)
                            }
                        }
                    } else if (state.searchResults.isEmpty()) {
                        item {
                            Text(
                                "No users found",
                                color = PeepMuted,
                                modifier = Modifier.padding(vertical = 20.dp)
                            )
                        }
                    } else {
                        items(state.searchResults, key = { "search_${it.id}" }) { profile ->
                            SearchUserCard(
                                profile = profile,
                                onAdd = { viewModel.sendFriendRequest(profile.username) }
                            )
                        }
                    }
                } else {
                    // Requests Section
                    if (state.pendingRequests.isNotEmpty()) {
                        item {
                            Text(
                                "FRIEND REQUESTS · ${state.pendingRequests.size} NEW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        items(state.pendingRequests, key = { "req_${it.id}" }) { req ->
                            RequestCard(
                                request = req,
                                onAccept = { viewModel.acceptRequest(req.id) },
                                onReject = { viewModel.rejectRequest(req.id) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Friends Section
                    item {
                        Text(
                            "YOUR FRIENDS · ${state.friends.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    if (state.friends.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PeepSurface, RoundedCornerShape(14.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No friends yet. Search above to add some!", color = PeepMuted)
                            }
                        }
                    } else {
                        items(state.friends, key = { "friend_${it.id}" }) { profile ->
                            SimpleUserCard(profile)
                        }
                    }
                }
            }
        }

        // Toast messages
        PeepToast(
            message = state.successMessage ?: state.error ?: "",
            visible = state.successMessage != null || state.error != null,
            onHide = { viewModel.clearMessages() }
        )
    }
}

@Composable
fun SearchUserCard(profile: Profile, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(color = PeepSurface, shape = RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PeepSurfaceBorder),
            contentAlignment = Alignment.Center
        ) {
            Text(profile.username.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, color = PeepWhite)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(profile.username, style = MaterialTheme.typography.titleMedium, color = PeepWhite)
            Text("@${profile.username.lowercase()}", style = MaterialTheme.typography.bodySmall, color = PeepMuted)
        }
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = PeepWhite, contentColor = PeepBlack),
            shape = RoundedCornerShape(20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text("Add", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun RequestCard(request: FriendRequestWithProfile, onAccept: () -> Unit, onReject: () -> Unit) {
    val username = request.user?.username ?: "Unknown"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(color = PeepSurfaceBorder, shape = RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PeepSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(username.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, color = PeepWhite)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(username, style = MaterialTheme.typography.titleMedium, color = PeepWhite, modifier = Modifier.weight(1f))
        
        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onReject,
                colors = ButtonDefaults.buttonColors(containerColor = PeepSurface, contentColor = PeepWhite),
                shape = RoundedCornerShape(20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.width(60.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(16.dp))
            }
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = PeepWhite, contentColor = PeepBlack),
                shape = RoundedCornerShape(20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("Accept", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun SimpleUserCard(profile: Profile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(color = PeepSurface, shape = RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PeepSurfaceBorder),
            contentAlignment = Alignment.Center
        ) {
            Text(profile.username.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, color = PeepWhite)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(profile.username, style = MaterialTheme.typography.titleMedium, color = PeepWhite)
    }
}
