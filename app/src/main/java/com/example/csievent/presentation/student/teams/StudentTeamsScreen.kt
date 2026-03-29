package com.example.csievent.presentation.student.teams

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.presentation.student.CSILoader
import com.example.csievent.presentation.student.LoaderSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

// =============================================================================
// TEAM PALETTES
// =============================================================================

private data class TeamPalette(
    val accent: Color, val bg1: Color,
    val bg2: Color, val glow: Color
)

private val TEAM_PALETTES = listOf(
    TeamPalette(Color(0xFF4B3CC8), Color(0xFF0A0820), Color(0xFF060412), Color(0xFF4B3CC8)),
    TeamPalette(Color(0xFF06B6D4), Color(0xFF051520), Color(0xFF030C14), Color(0xFF06B6D4)),
    TeamPalette(Color(0xFF10B981), Color(0xFF051510), Color(0xFF030C08), Color(0xFF10B981)),
    TeamPalette(Color(0xFFD4AF37), Color(0xFF151008), Color(0xFF0C0A04), Color(0xFFD4AF37)),
    TeamPalette(Color(0xFFEC4899), Color(0xFF150810), Color(0xFF0C0408), Color(0xFFEC4899)),
    TeamPalette(Color(0xFF8B5CF6), Color(0xFF0E0820), Color(0xFF080412), Color(0xFF8B5CF6)),
)

// =============================================================================
// STUDENT TEAMS SCREEN
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTeamsScreen(
    eventId:       Long,
    navController: NavHostController,
    viewModel:     StudentTeamsViewModel = hiltViewModel()
) {
    val state    by viewModel.state.collectAsState()
    var teamName by remember { mutableStateOf("") }
    var showJoinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) { viewModel.loadTeams(eventId) }

    val inf = rememberInfiniteTransition(label = "teams_bg")

    val nebDrift by inf.animateFloat(
        0f, 30f, infiniteRepeatable(
            tween(10000, easing = EaseInOutSine), RepeatMode.Reverse
        ), "neb"
    )
    val twinkle by inf.animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(5000, easing = LinearEasing)), "tw"
    )

    if (showJoinDialog) {
        WarpCodeDialog(
            onDismiss = { showJoinDialog = false },
            onConfirm = { code ->
                showJoinDialog = false
                viewModel.joinTeamByCode(code, eventId)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020818), Color(0xFF050215), Color(0xFF020A10))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush  = Brush.radialGradient(
                    listOf(Color(0xFF2D1B69).copy(alpha = 0.35f), Color.Transparent),
                    radius = 400f,
                    center = Offset(size.width * 0.8f + nebDrift, size.height * 0.2f)
                ),
                radius = 400f,
                center = Offset(size.width * 0.8f + nebDrift, size.height * 0.2f)
            )
            drawCircle(
                brush  = Brush.radialGradient(
                    listOf(Color(0xFF0D4040).copy(alpha = 0.25f), Color.Transparent),
                    radius = 350f,
                    center = Offset(size.width * 0.1f - nebDrift, size.height * 0.7f)
                ),
                radius = 350f,
                center = Offset(size.width * 0.1f - nebDrift, size.height * 0.7f)
            )
            val stars = listOf(
                0.08f to 0.04f, 0.92f to 0.08f, 0.45f to 0.02f,
                0.15f to 0.15f, 0.78f to 0.12f, 0.03f to 0.30f,
                0.96f to 0.25f, 0.35f to 0.40f, 0.85f to 0.42f,
                0.12f to 0.55f, 0.67f to 0.58f, 0.52f to 0.80f,
                0.23f to 0.75f, 0.88f to 0.70f, 0.48f to 0.92f,
                0.72f to 0.88f, 0.32f to 0.62f, 0.60f to 0.22f
            )
            stars.forEachIndexed { i, (x, y) ->
                val tw = (sin(twinkle + i * 0.7f) * 0.4f + 0.6f).toFloat()
                drawCircle(
                    color  = Color(0xFFF0EEFF).copy(alpha = tw * 0.5f),
                    radius = if (i % 3 == 0) 2f else 1.2f,
                    center = Offset(size.width * x, size.height * y)
                )
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Squad Hub", color = Color.White,
                            fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color(0xFF8C83E4))
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.loadTeams(eventId) }) {
                            Icon(Icons.Default.Refresh, null, tint = Color(0xFF8C83E4))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { pad ->
            LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Warp portal join code card
                state.myTeam?.let { myTeam ->
                    item {
                        WarpPortalCard(
                            team   = myTeam,
                            onLeave = { viewModel.leaveTeam(myTeam.id, eventId) }
                        )
                    }
                }

                // Create / join section
                if (state.myTeam == null) {
                    item {
                        CreateJoinSection(
                            teamName       = teamName,
                            onNameChange   = { teamName = it },
                            onCreateTeam   = {
                                if (teamName.isNotBlank()) {
                                    viewModel.createTeam(eventId, teamName.trim())
                                    teamName = ""
                                }
                            },
                            onJoinWithCode = { showJoinDialog = true },
                            isLoading      = state.isLoading
                        )
                    }
                }

                // Loading
                if (state.isLoading) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CSILoader(size = LoaderSize.MEDIUM)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Scanning for squads...",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Section header
                if (state.teams.isNotEmpty() && !state.isLoading) {
                    item {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "SQUADS  ·  ${state.teams.size}",
                                color = Color(0xFF8C83E4).copy(alpha = 0.6f),
                                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Box(
                                Modifier.weight(1f).height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF4B3CC8).copy(alpha = 0.4f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }

                // Team cards
                itemsIndexed(state.teams, key = { _, t -> t.id }) { index, team ->
                    GalaxyTeamCard(
                        team     = team,
                        index    = index,
                        isMyTeam = state.myTeam?.id == team.id
                    )
                }

                // Empty
                if (!state.isLoading && state.teams.isEmpty() && state.myTeam == null) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌌", fontSize = 44.sp)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "No squads yet", color = Color.White,
                                    fontSize = 18.sp, fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Be the first to create one",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.6f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Error
                state.error?.let { err ->
                    item {
                        Box(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1A0808))
                                .border(
                                    1.dp, Color(0xFFEF4444).copy(alpha = 0.4f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("⚠️", fontSize = 16.sp)
                                Text(
                                    err, color = Color(0xFFFCA5A5),
                                    fontSize = 13.sp, modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.clearMessage() }) {
                                    Text("✕", color = Color(0xFFEF4444).copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }

                // Success
                state.successMessage?.let { msg ->
                    item {
                        Box(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0A1A0F))
                                .border(
                                    1.dp, Color(0xFF10B981).copy(alpha = 0.4f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("✅", fontSize = 16.sp)
                                Text(
                                    msg, color = Color(0xFF6EE7B7),
                                    fontSize = 13.sp, modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.clearMessage() }) {
                                    Text("✕", color = Color(0xFF10B981).copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// =============================================================================
// WARP PORTAL CARD  — the hero join code element
// =============================================================================

@Composable
private fun WarpPortalCard(
    team:    TeamResponseDto,
    onLeave: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // ── Copy state ────────────────────────────────────────────────────
    var copied     by remember { mutableStateOf(false) }
    var warpActive by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    // ── Continuous portal animations ──────────────────────────────────
    val inf = rememberInfiniteTransition(label = "portal")

    // 3 portal rings spin at different speeds
    val ring1 by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(3000, easing = LinearEasing)), "r1"
    )
    val ring2 by inf.animateFloat(
        360f, 0f,
        infiniteRepeatable(tween(4500, easing = LinearEasing)), "r2"
    )
    val ring3 by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(7000, easing = LinearEasing)), "r3"
    )

    // Portal core breathe
    val portalBreath by inf.animateFloat(
        0.85f, 1f,
        infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), "pb"
    )

    // Energy particles orbiting
    val energyAngle by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(2000, easing = LinearEasing)), "ea"
    )

    // Code character float
    val codeFloat by inf.animateFloat(
        -2f, 2f,
        infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse), "cf"
    )

    // ── Warp jump animations ──────────────────────────────────────────
    val warpScale by animateFloatAsState(
        targetValue   = if (warpActive) 0f else 1f,
        animationSpec = tween(400, easing = EaseInExpo),
        label         = "warp_scale"
    )
    val warpShockwave by animateFloatAsState(
        targetValue   = if (warpActive) 6f else 0f,
        animationSpec = tween(600, easing = EaseOutExpo),
        label         = "warp_shock"
    )
    val warpShockAlpha by animateFloatAsState(
        targetValue   = if (warpActive) 0f else 1f,
        animationSpec = tween(600),
        label         = "warp_sa"
    )

    // COPIED text entrance
    val copiedScale by animateFloatAsState(
        targetValue   = if (copied) 1f else 0f,
        animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium),
        label         = "copied_s"
    )

    fun copyCode() {
        val code = team.joinCode ?: return
        val cb   = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cb.setPrimaryClip(ClipData.newPlainText("Squad Code", code))
        scope.launch {
            warpActive = true
            delay(400)
            copied     = true
            delay(200)
            warpActive = false
            delay(1800)
            copied = false
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title   = { Text("Leave Squad?", color = Color.White) },
            text    = {
                Text(
                    "Leave ${team.teamName}?",
                    color = Color(0xFF9B94C4)
                )
            },
            confirmButton = {
                TextButton(onClick = { showLeaveDialog = false; onLeave() }) {
                    Text("Leave", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel", color = Color(0xFF8C83E4))
                }
            },
            containerColor = Color(0xFF0F0B2E), tonalElevation = 0.dp
        )
    }

    // Card container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0825), Color(0xFF060418), Color(0xFF080525))
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFF4B3CC8).copy(alpha = 0.5f),
                        Color(0xFF7C3AED).copy(alpha = 0.3f),
                        Color(0xFF4B3CC8).copy(alpha = 0.5f)
                    )
                ),
                RoundedCornerShape(28.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Top bar ───────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Pulsing status dot
                        val dotPulse by inf.animateFloat(
                            0.4f, 1f,
                            infiniteRepeatable(tween(900), RepeatMode.Reverse), "dp"
                        )
                        Box(
                            Modifier.size(7.dp).clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = dotPulse))
                        )
                        Text(
                            "ACTIVE SQUAD",
                            color         = Color(0xFF10B981),
                            fontSize      = 10.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        team.teamName,
                        color         = Color.White,
                        fontSize      = 20.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.08f))
                        .border(
                            1.dp, Color(0xFFEF4444).copy(alpha = 0.25f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { showLeaveDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Leave",
                        color      = Color(0xFFEF4444).copy(alpha = 0.7f),
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── WARP PORTAL — the hero element ───────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Portal visual
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier.size(110.dp)
                ) {
                    Canvas(modifier = Modifier.size(110.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        // Outer glow
                        drawCircle(
                            brush  = Brush.radialGradient(
                                listOf(
                                    Color(0xFF4B3CC8).copy(alpha = 0.3f * portalBreath),
                                    Color(0xFF7C3AED).copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = size.minDimension / 1.6f
                            ),
                            radius = size.minDimension / 1.6f,
                            center = Offset(cx, cy)
                        )

                        // Ring 1 — outer, indigo dashed
                        rotate(ring1, Offset(cx, cy)) {
                            val r1 = size.minDimension / 2.2f
                            drawCircle(
                                color  = Color(0xFF4B3CC8).copy(alpha = 0.6f),
                                radius = r1, center = Offset(cx, cy),
                                style  = Stroke(
                                    2f,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(12f, 6f)
                                    )
                                )
                            )
                            // Glowing dot on ring 1
                            drawCircle(
                                brush  = Brush.radialGradient(
                                    listOf(Color.White, Color(0xFF4B3CC8).copy(alpha = 0f)),
                                    center = Offset(cx + r1, cy), radius = 8f
                                ),
                                radius = 8f, center = Offset(cx + r1, cy)
                            )
                            drawCircle(
                                color  = Color.White,
                                radius = 3f, center = Offset(cx + r1, cy)
                            )
                        }

                        // Ring 2 — middle, violet solid
                        rotate(ring2, Offset(cx, cy)) {
                            val r2 = size.minDimension / 2.8f
                            drawCircle(
                                color  = Color(0xFF7C3AED).copy(alpha = 0.7f),
                                radius = r2, center = Offset(cx, cy),
                                style  = Stroke(1.5f)
                            )
                            drawCircle(
                                brush  = Brush.radialGradient(
                                    listOf(Color(0xFFD4AF37), Color.Transparent),
                                    center = Offset(cx, cy - r2), radius = 6f
                                ),
                                radius = 6f, center = Offset(cx, cy - r2)
                            )
                            drawCircle(
                                color  = Color(0xFFD4AF37),
                                radius = 2.5f, center = Offset(cx, cy - r2)
                            )
                        }

                        // Ring 3 — inner, gold dashed
                        rotate(ring3, Offset(cx, cy)) {
                            val r3 = size.minDimension / 4f
                            drawCircle(
                                color  = Color(0xFFD4AF37).copy(alpha = 0.5f),
                                radius = r3, center = Offset(cx, cy),
                                style  = Stroke(
                                    1f,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(6f, 4f)
                                    )
                                )
                            )
                        }

                        // Portal core
                        drawCircle(
                            brush  = Brush.radialGradient(
                                listOf(
                                    Color(0xFF9B6DFF).copy(alpha = 0.9f * portalBreath),
                                    Color(0xFF4B3CC8).copy(alpha = 0.7f),
                                    Color(0xFF1A0A3A)
                                ),
                                center = Offset(cx, cy),
                                radius = size.minDimension / 5f
                            ),
                            radius = size.minDimension / 5f,
                            center = Offset(cx, cy)
                        )

                        // Inner swirl lines
                        for (i in 0..5) {
                            val a = ring1 * PI.toFloat() / 180f + i * PI.toFloat() / 3f
                            val r = size.minDimension / 12f
                            drawLine(
                                color       = Color(0xFFAEA8EE).copy(alpha = 0.4f),
                                start       = Offset(cx, cy),
                                end         = Offset(cx + r * cos(a), cy + r * sin(a)),
                                strokeWidth = 1f
                            )
                        }

                        // Energy particles orbiting
                        val eRad = size.minDimension / 3.5f
                        for (i in 0..3) {
                            val a = (energyAngle + i * 90f) * PI.toFloat() / 180f
                            val ex = cx + eRad * cos(a)
                            val ey = cy + eRad * sin(a)
                            drawCircle(
                                brush  = Brush.radialGradient(
                                    listOf(Color(0xFF00D4FF), Color.Transparent),
                                    center = Offset(ex, ey), radius = 5f
                                ),
                                radius = 5f, center = Offset(ex, ey)
                            )
                            drawCircle(
                                color  = Color.White,
                                radius = 2f, center = Offset(ex, ey)
                            )
                        }

                        // Warp shockwave
                        if (warpShockwave > 0f) {
                            drawCircle(
                                brush  = Brush.radialGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0xFF9B6DFF).copy(
                                            alpha = warpShockAlpha.coerceIn(0f, 0.8f)
                                        ),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension / 5f * warpShockwave,
                                center = Offset(cx, cy),
                                style  = Stroke(3f)
                            )
                        }
                    }
                }

                // Code + info column
                Column(modifier = Modifier.weight(1f)) {

                    // Label
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "WARP CODE",
                            color         = Color(0xFF8C83E4).copy(alpha = 0.6f),
                            fontSize      = 10.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        if (!copied) {
                            Text(
                                "· tap to copy",
                                color    = Color(0xFF4B3CC8).copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Code characters — tappable
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        if (copied) Color(0xFF0A2218)
                                        else Color(0xFF0A0628),
                                        if (copied) Color(0xFF061510)
                                        else Color(0xFF150A35)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(
                                        if (copied) Color(0xFF10B981).copy(alpha = 0.6f)
                                        else Color(0xFF4B3CC8).copy(alpha = 0.5f),
                                        if (copied) Color(0xFF059669).copy(alpha = 0.3f)
                                        else Color(0xFFD4AF37).copy(alpha = 0.4f)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .scale(warpScale.coerceAtLeast(0.01f))
                            .clickable { copyCode() }
                            .padding(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier            = Modifier.fillMaxWidth()
                        ) {
                            // Characters
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                team.joinCode?.chunked(1)?.forEachIndexed { i, char ->
                                    val charOffset = codeFloat * (if (i % 2 == 0) 1f else -1f)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .offset(y = charOffset.dp)
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    if (copied)
                                                        listOf(
                                                            Color(0xFF10B981).copy(alpha = 0.2f),
                                                            Color(0xFF059669).copy(alpha = 0.1f)
                                                        )
                                                    else
                                                        listOf(
                                                            Color(0xFF2A2060),
                                                            Color(0xFF150A35)
                                                        )
                                                )
                                            )
                                            .border(
                                                0.5.dp,
                                                if (copied) Color(0xFF10B981).copy(alpha = 0.4f)
                                                else Color(0xFF4B3CC8).copy(alpha = 0.3f),
                                                RoundedCornerShape(7.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            char,
                                            color      = if (copied) Color(0xFF10B981)
                                            else Color.White,
                                            fontSize   = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            // Copied feedback
                            AnimatedVisibility(
                                visible = copied,
                                enter   = fadeIn() + expandVertically(),
                                exit    = fadeOut() + shrinkVertically()
                            ) {
                                Row(
                                    modifier              = Modifier.padding(top = 8.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("✦", color = Color(0xFF10B981), fontSize = 10.sp)
                                    Text(
                                        "Warped to clipboard",
                                        color      = Color(0xFF10B981),
                                        fontSize   = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text("✦", color = Color(0xFF10B981), fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Members
                    Text(
                        "CREW  ·  ${team.members.size}",
                        color         = Color(0xFF8C83E4).copy(alpha = 0.5f),
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        team.members.take(4).forEachIndexed { i, name ->
                            val memberColors = listOf(
                                Color(0xFF4B3CC8), Color(0xFF7C3AED),
                                Color(0xFF06B6D4), Color(0xFFD4AF37)
                            )
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(memberColors[i % memberColors.size].copy(alpha = 0.15f))
                                    .border(
                                        1.dp,
                                        memberColors[i % memberColors.size].copy(alpha = 0.35f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    name.take(7),
                                    color    = memberColors[i % memberColors.size].copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (team.members.size > 4) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF2A2060))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    "+${team.members.size - 4}",
                                    color    = Color(0xFF8C83E4),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Bottom share hint bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A0628))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF4B3CC8).copy(alpha = 0.3f),
                                Color(0xFF7C3AED).copy(alpha = 0.2f),
                                Color(0xFF4B3CC8).copy(alpha = 0.3f)
                            )
                        ),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.Center,
                    Alignment.CenterVertically
                ) {
                    Text("🔗", fontSize = 12.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Share the Warp Code with your squad to let them join",
                        color      = Color(0xFF8C83E4).copy(alpha = 0.6f),
                        fontSize   = 11.sp,
                        textAlign  = TextAlign.Center
                    )
                }
            }
        }
    }
}

// =============================================================================
// CREATE / JOIN SECTION
// =============================================================================

@Composable
private fun CreateJoinSection(
    teamName:       String,
    onNameChange:   (String) -> Unit,
    onCreateTeam:   () -> Unit,
    onJoinWithCode: () -> Unit,
    isLoading:      Boolean
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(200); visible = true }
    val sA by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(600), label = "sec_a"
    )

    Column(
        modifier            = Modifier.alpha(sA),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "FORM YOUR SQUAD",
                color         = Color(0xFF8C83E4).copy(alpha = 0.6f),
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Box(
                Modifier.weight(1f).height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF4B3CC8).copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )
        }

        OutlinedTextField(
            value         = teamName,
            onValueChange = onNameChange,
            label         = { Text("Squad Name", color = Color(0xFF8C83E4)) },
            singleLine    = true,
            shape         = RoundedCornerShape(14.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedTextColor        = Color.White,
                unfocusedTextColor      = Color.White,
                focusedBorderColor      = Color(0xFF4B3CC8),
                unfocusedBorderColor    = Color(0xFF1E1A4A),
                focusedLabelColor       = Color(0xFF8C83E4),
                cursorColor             = Color(0xFF8C83E4),
                focusedContainerColor   = Color(0xFF0A0820),
                unfocusedContainerColor = Color(0xFF060412)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (teamName.isNotBlank())
                        Brush.linearGradient(listOf(Color(0xFF4B3CC8), Color(0xFF7C3AED)))
                    else
                        Brush.linearGradient(listOf(Color(0xFF1A1640), Color(0xFF1A1640)))
                )
                .clickable(enabled = teamName.isNotBlank() && !isLoading) { onCreateTeam() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CSILoader(size = LoaderSize.SMALL)
            } else {
                Text(
                    "Create Squad",
                    color      = if (teamName.isNotBlank()) Color.White
                    else Color.White.copy(alpha = 0.3f),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier              = Modifier.padding(vertical = 4.dp)
        ) {
            Box(
                Modifier.weight(1f).height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFF2A2460))
                        )
                    )
            )
            Text("or", color = Color(0xFF4A4070), fontSize = 13.sp)
            Box(
                Modifier.weight(1f).height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF2A2460), Color.Transparent)
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF060412))
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF4B3CC8).copy(alpha = 0.6f),
                            Color(0xFFD4AF37).copy(alpha = 0.5f)
                        )
                    ),
                    RoundedCornerShape(14.dp)
                )
                .clickable { onJoinWithCode() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("⚡", fontSize = 16.sp)
                Text(
                    "Enter Warp Code",
                    color      = Color(0xFF8C83E4),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// =============================================================================
// GALAXY TEAM CARD
// =============================================================================

@Composable
private fun GalaxyTeamCard(
    team:     TeamResponseDto,
    index:    Int,
    isMyTeam: Boolean
) {
    val palette = TEAM_PALETTES[index % TEAM_PALETTES.size]

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 80L + 100L); visible = true }
    val cA by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(350), label = "ca$index"
    )
    val cX by animateFloatAsState(
        targetValue   = if (visible) 0f else 60f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "cx$index"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = cX.dp)
            .alpha(cA)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(palette.bg1, palette.bg2)))
                .border(
                    1.dp,
                    if (isMyTeam)
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF10B981).copy(alpha = 0.5f),
                                Color(0xFF059669).copy(alpha = 0.2f)
                            )
                        )
                    else
                        Brush.linearGradient(
                            listOf(
                                palette.accent.copy(alpha = 0.3f),
                                palette.accent.copy(alpha = 0.1f)
                            )
                        ),
                    RoundedCornerShape(18.dp)
                )
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(palette.accent, palette.accent.copy(alpha = 0.1f))
                        )
                    )
            )

            Row(
                modifier  = Modifier.padding(
                    start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp
                ),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Mini planet
                Canvas(modifier = Modifier.size(44.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r  = size.minDimension / 2.5f
                    drawCircle(
                        brush  = Brush.radialGradient(
                            listOf(palette.glow.copy(alpha = 0.4f), Color.Transparent),
                            center = Offset(cx, cy), radius = r * 2f
                        ),
                        radius = r * 2f, center = Offset(cx, cy)
                    )
                    drawCircle(
                        brush  = Brush.radialGradient(
                            listOf(palette.accent, palette.bg1),
                            center = Offset(cx - r * 0.2f, cy - r * 0.2f),
                            radius = r * 2f
                        ),
                        radius = r, center = Offset(cx, cy)
                    )
                    drawCircle(
                        color  = Color.White.copy(alpha = 0.2f),
                        radius = r * 0.3f,
                        center = Offset(cx - r * 0.3f, cy - r * 0.3f)
                    )
                    drawOval(
                        color   = palette.accent.copy(alpha = 0.4f),
                        topLeft = Offset(cx - r * 1.4f, cy - r * 0.2f),
                        size    = Size(r * 2.8f, r * 0.4f),
                        style   = Stroke(1f)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            team.teamName,
                            color = Color.White, fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isMyTeam) {
                            Box(
                                Modifier.clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "YOU",
                                    color = Color(0xFF10B981), fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        team.leaderName?.let { leader ->
                            Text(
                                "👑 $leader",
                                color = Color(0xFFD4AF37).copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        if (team.members.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                repeat(minOf(team.members.size, 8)) {
                                    Box(
                                        Modifier.size(5.dp).clip(CircleShape)
                                            .background(palette.accent.copy(alpha = 0.6f))
                                    )
                                }
                                if (team.members.size > 8) {
                                    Text(
                                        "+${team.members.size - 8}",
                                        color = palette.accent.copy(alpha = 0.5f),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    Modifier.clip(RoundedCornerShape(10.dp))
                        .background(palette.accent.copy(alpha = 0.12f))
                        .border(
                            1.dp, palette.accent.copy(alpha = 0.25f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            team.members.size.toString(),
                            color = palette.accent, fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "CREW", color = palette.accent.copy(alpha = 0.5f),
                            fontSize = 8.sp, letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// WARP CODE DIALOG
// =============================================================================

@Composable
private fun WarpCodeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "⚡ Enter Warp Code",
                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Get the 8-character code from your squad leader",
                    color = Color(0xFF8C83E4), fontSize = 12.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = code,
                    onValueChange = { if (it.length <= 8) code = it.uppercase() },
                    placeholder   = {
                        Text(
                            "A3F9B2C1",
                            color      = Color(0xFF4A4070),
                            fontFamily = FontFamily.Monospace,
                            fontSize   = 22.sp,
                            textAlign  = TextAlign.Center,
                            modifier   = Modifier.fillMaxWidth()
                        )
                    },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                        color      = Color.White
                    ),
                    shape   = RoundedCornerShape(14.dp),
                    colors  = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = Color(0xFF4B3CC8),
                        unfocusedBorderColor    = Color(0xFF1E1A4A),
                        focusedContainerColor   = Color(0xFF0A0820),
                        unfocusedContainerColor = Color(0xFF060412),
                        cursorColor             = Color(0xFF8C83E4)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "${code.length} / 8",
                    color     = if (code.length == 8) Color(0xFF10B981)
                    else Color(0xFF4A4070),
                    fontSize  = 11.sp,
                    textAlign = TextAlign.End,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Box(
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (code.length == 8)
                            Brush.linearGradient(listOf(Color(0xFF4B3CC8), Color(0xFF7C3AED)))
                        else
                            Brush.linearGradient(listOf(Color(0xFF1A1640), Color(0xFF1A1640)))
                    )
                    .clickable(enabled = code.length == 8) { onConfirm(code) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    "Warp In",
                    color      = if (code.length == 8) Color.White
                    else Color.White.copy(alpha = 0.3f),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF8C83E4))
            }
        },
        containerColor = Color(0xFF0F0B2E),
        tonalElevation = 0.dp
    )
}