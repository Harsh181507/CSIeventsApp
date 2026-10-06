package com.example.csievent.presentation.results

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.csievent.data.remote.dto.score.LeaderboardResponseDto
import com.example.csievent.presentation.components.BottomSpacer
import com.example.csievent.presentation.components.CsiScreen
import com.example.csievent.presentation.components.CsiTopBar
import com.example.csievent.presentation.components.EmptyState
import com.example.csievent.presentation.components.LoadingState
import com.example.csievent.presentation.components.NoticeBanner
import com.example.csievent.presentation.components.ProgressBar
import com.example.csievent.presentation.components.RefreshableBox
import com.example.csievent.presentation.components.formatScore
import com.example.csievent.ui.theme.CardShape
import com.example.csievent.ui.theme.CsiTheme
import com.example.csievent.ui.theme.monoStyle

/**
 * Results for an event: podium for the top three, then everyone else.
 * Students see final results once scoring is locked; organizers and judges
 * also see live standings while judging is open.
 */
@Composable
fun ResultsScreen(
    eventId: Long,
    onBack: () -> Unit,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()

    LaunchedEffect(eventId) { viewModel.load(eventId) }

    val final = state.event?.scoringLocked == true
    val entries = state.entries
    val showPodium = entries.size >= 3
    val rest = if (showPodium) entries.drop(3) else entries
    val topScore = entries.maxOfOrNull { it.totalScore }?.takeIf { it > 0 } ?: 1.0
    val judges = entries.maxOfOrNull { it.judgeCount } ?: 0

    CsiScreen(
        topBar = {
            CsiTopBar(
                title = if (final || !state.canSeeLive) "Results" else "Live standings",
                subtitle = state.event?.let { e ->
                    buildString {
                        append(e.title)
                        if (entries.isNotEmpty()) append(" · ${entries.size} teams")
                        if (judges > 0) append(" · $judges judge" + if (judges == 1L) "" else "s")
                    }
                },
                onBack = onBack
            )
        }
    ) { padding ->
        when {
            state.isLoading && entries.isEmpty() && state.error == null -> LoadingState(Modifier.padding(padding))
            else -> RefreshableBox(
                refreshing = state.isLoading,
                onRefresh = { viewModel.load(eventId) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when {
                        state.error != null -> item(key = "error") {
                            EmptyState(
                                icon = Icons.Rounded.HourglassTop,
                                title = "Results aren't out yet",
                                message = state.error!!
                            )
                        }
                        entries.isEmpty() -> item(key = "empty") {
                            EmptyState(
                                icon = Icons.Rounded.Insights,
                                title = "No scores yet",
                                message = "Standings appear as soon as judges start scoring. Pull down to refresh."
                            )
                        }
                        else -> {
                            if (!final && state.canSeeLive) {
                                item(key = "live") {
                                    NoticeBanner(
                                        "Live standings — judging is still open. Students see results after you lock scoring.",
                                        icon = Icons.Rounded.Insights
                                    )
                                }
                            }
                            if (showPodium) {
                                item(key = "podium") {
                                    Podium(entries.take(3), state.myTeamId)
                                }
                            }
                            items(rest, key = { it.teamId }) { entry ->
                                RankRow(entry, entry.teamId == state.myTeamId, (entry.totalScore / topScore).toFloat())
                            }
                            item(key = "note") {
                                Text(
                                    "Score = average total across judges, so teams scored by different numbers of judges are compared fairly.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textSubtle,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                )
                            }
                        }
                    }
                    item(key = "bottom") { BottomSpacer() }
                }
            }
        }
    }
}

/** 2nd · 1st · 3rd columns of different heights. */
@Composable
private fun Podium(top: List<LeaderboardResponseDto>, myTeamId: Long?) {
    val c = CsiTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        PodiumColumn(top[1], 2, 112, c.raised, c.silver, myTeamId, Modifier.weight(1f))
        PodiumColumn(top[0], 1, 150, c.winnerContainer, c.onWinnerContainer, myTeamId, Modifier.weight(1f))
        PodiumColumn(top[2], 3, 92, c.raised, c.bronze, myTeamId, Modifier.weight(1f))
    }
}

@Composable
private fun PodiumColumn(
    entry: LeaderboardResponseDto,
    place: Int,
    height: Int,
    container: Color,
    numberColor: Color,
    myTeamId: Long?,
    modifier: Modifier
) {
    val c = CsiTheme.colors
    val mine = entry.teamId == myTeamId
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (place == 1) Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = c.highlight, modifier = Modifier.size(26.dp))
        Text(
            entry.teamName + if (mine) " (you)" else "",
            style = MaterialTheme.typography.titleSmall,
            color = c.text,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                .background(container)
                .then(if (mine) Modifier.border(2.dp, c.highlight, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 6.dp, bottomEnd = 6.dp)) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("${entry.rank}", style = monoStyle(if (place == 1) 32.sp else 26.sp), color = numberColor)
            Text(
                formatScore(entry.totalScore),
                style = monoStyle(14.sp),
                color = if (place == 1) numberColor.copy(alpha = 0.8f) else c.textMuted
            )
        }
    }
}

@Composable
private fun RankRow(entry: LeaderboardResponseDto, mine: Boolean, fraction: Float) {
    val c = CsiTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(if (mine) c.highlightContainer else c.surface)
            .border(1.dp, if (mine) c.highlightBorder else if (c.isDark) Color.Transparent else c.line, CardShape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${entry.rank}",
                style = monoStyle(15.sp),
                color = if (mine) c.highlight else c.textMuted,
                modifier = Modifier.width(30.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    entry.teamName + if (mine) " · you" else "",
                    style = MaterialTheme.typography.titleSmall,
                    color = c.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(formatScore(entry.totalScore), style = monoStyle(15.sp), color = c.text)
        }
        Box(Modifier.padding(start = 30.dp)) {
            ProgressBar(fraction = fraction, color = if (mine) c.highlight else c.textSubtle)
        }
    }
}
