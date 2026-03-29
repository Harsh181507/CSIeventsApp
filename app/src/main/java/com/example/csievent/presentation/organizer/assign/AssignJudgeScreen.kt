package com.example.csievent.presentation.organizer.assign

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.presentation.components.ErrorBanner
import com.example.csievent.presentation.components.SuccessBanner

/**
 * Screen for organizers to assign a judge to an event (and optionally to
 * a specific team within that event).
 *
 * Flow:
 * 1. The screen loads all JUDGE-role users and all teams for this event.
 * 2. The organizer selects a judge (required) and optionally a team.
 * 3. Tapping "Assign" calls POST /judge-assignments with the selection.
 *
 * FIX: The previous version had three critical layout issues:
 * - LazyColumns nested inside a Column (causes infinite height constraint crash)
 * - No visual feedback for which judge/team is selected
 * - No Scaffold / TopAppBar (no back navigation)
 * - No loading indicator while data loads
 * - Success/error banners missing
 *
 * Layout solution: Use a single LazyColumn with multiple sections using
 * [LazyListScope.item] for section headers and [LazyListScope.items] for
 * the data rows — this is the recommended pattern for mixed-content lists.
 *
 * Reference — Nested scrollable containers (avoid them):
 * https://developer.android.com/develop/ui/compose/lists#avoid-nesting-scrollable
 *
 * Reference — LazyColumn with multiple item types:
 * https://developer.android.com/develop/ui/compose/lists#lazylistscope
 *
 * File: app/src/main/java/com/example/csievent/presentation/organizer/assign/AssignJudgeScreen.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignJudgeScreen(
    eventId:       Long,
    navController: NavHostController,
    viewModel:     AssignJudgeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Track which judge and team the organizer has selected
    var selectedJudgeId by remember { mutableStateOf<Long?>(null) }
    var selectedTeamId  by remember { mutableStateOf<Long?>(null) }

    // Load judges + teams when screen appears
    LaunchedEffect(eventId) {
        viewModel.loadData(eventId)
    }

    // Auto-dismiss success after assignment
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            // Reset selection after successful assignment
            selectedJudgeId = null
            selectedTeamId  = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚖️ Assign Judge") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector        = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {

                // Single LazyColumn for the entire screen — avoids nested scrollable crash
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 16.dp,
                        bottom = 100.dp  // leave room for the fixed Assign button
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // ----------------------------------------------------------
                    // SECTION: Judge selection (required)
                    // ----------------------------------------------------------
                    item(key = "judge_header") {
                        SectionHeader(
                            title    = "Select Judge *",
                            subtitle = "Required"
                        )
                    }

                    if (state.judges.isEmpty()) {
                        item(key = "no_judges") {
                            Text(
                                text  = "No judges available. Ask users to register and have their role upgraded.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(
                            items = state.judges,
                            key   = { "judge_${it.id}" }
                        ) { judge ->
                            SelectableRow(
                                label      = judge.name,
                                subtitle   = judge.email,
                                isSelected = selectedJudgeId == judge.id,
                                onClick    = { selectedJudgeId = judge.id }
                            )
                        }
                    }

                    // ----------------------------------------------------------
                    // SECTION: Team selection (optional)
                    // ----------------------------------------------------------
                    item(key = "team_header") {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title    = "Select Team",
                            subtitle = "Optional — assigns judge to score a specific team"
                        )
                    }

                    if (state.teams.isEmpty()) {
                        item(key = "no_teams") {
                            Text(
                                text  = "No teams have registered for this event yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // "None" option — allows clearing a team selection
                        item(key = "team_none") {
                            SelectableRow(
                                label      = "None (event-level only)",
                                subtitle   = "Judge can see the event but is not assigned to any team",
                                isSelected = selectedTeamId == null,
                                onClick    = { selectedTeamId = null }
                            )
                        }

                        items(
                            items = state.teams,
                            key   = { "team_${it.id}" }
                        ) { team ->
                            SelectableRow(
                                label      = team.teamName,
                                subtitle   = team.leaderName?.let { "Led by $it" } ?: "",
                                isSelected = selectedTeamId == team.id,
                                onClick    = { selectedTeamId = team.id }
                            )
                        }
                    }
                }

                // ----------------------------------------------------------
                // FIXED ASSIGN BUTTON at the bottom of the screen
                // ----------------------------------------------------------
                Button(
                    onClick  = {
                        selectedJudgeId?.let { judgeId ->
                            viewModel.assignJudge(eventId, judgeId, selectedTeamId)
                        }
                    },
                    enabled  = selectedJudgeId != null,   // require a judge selection
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (selectedTeamId != null) {
                            "Assign Judge to Event + Team"
                        } else {
                            "Assign Judge to Event"
                        }
                    )
                }
            }

            // Error and success banners
            state.error?.let { errorMessage ->
                ErrorBanner(
                    message   = errorMessage,
                    onDismiss = { viewModel.clearMessage() }
                )
            }

            state.successMessage?.let { successMessage ->
                SuccessBanner(
                    message   = successMessage,
                    onDismiss = { viewModel.clearMessage() }
                )
            }
        }
    }
}

// =============================================================================
// PRIVATE COMPOSABLES
// =============================================================================

/**
 * Section header with a title and an optional subtitle.
 */
@Composable
private fun SectionHeader(
    title:    String,
    subtitle: String = ""
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text  = title,
            style = MaterialTheme.typography.titleSmall
        )
        if (subtitle.isNotBlank()) {
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * A tappable row that shows a checkmark when selected.
 *
 * @param label      the primary text
 * @param subtitle   the secondary text (shown below label)
 * @param isSelected whether this row is currently selected
 * @param onClick    called when the row is tapped
 */
@Composable
private fun SelectableRow(
    label:      String,
    subtitle:   String = "",
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = "Selected",
                tint               = MaterialTheme.colorScheme.primary
            )
        }
    }
}