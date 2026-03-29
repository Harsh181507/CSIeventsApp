package com.example.csievent.presentation.judge.teams

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.presentation.student.CSILoader
import com.example.csievent.presentation.student.LoaderSize
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * SOUL VESSELS — Judge Teams Screen
 *
 * Visual concept: Teams are souls awaiting judgment, rendered as
 * ethereal glowing SOUL VESSELS — large orbs with particle halos,
 * ripple rings, and inner constellation patterns.
 * Each vessel breathes, pulses, and has a unique soul signature.
 *
 * Layout: full-width vessels stacked vertically, each taking 140dp height.
 * The orb sits left, soul name + member count right.
 * Background: vertical beam of light descending from top — "the judgment ray."
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JudgeTeamsScreen(
    eventId: Long,
    navController: NavHostController,
    viewModel: JudgeTeamsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(eventId) { viewModel.loadTeams(eventId) }

    val inf = rememberInfiniteTransition(label = "soul_bg")
    val cosmicPhase by inf.animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(10000, easing = LinearEasing)), "cp"
    )
    val beamPulse by inf.animateFloat(
        0.4f, 0.9f,
        infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse), "bp"
    )

    var headerIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); headerIn = true }
    val headerA by animateFloatAsState(if (headerIn) 1f else 0f, tween(900), label = "ha")
    val headerY by animateFloatAsState(
        if (headerIn) 0f else 30f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "hy"
    )

    val gold = Color(0xFFD4AF37)
    val cyan = Color(0xFF00E5FF)
    val violet = Color(0xFFBB86FC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020010),
                        Color(0xFF010108),
                        Color(0xFF00000A)
                    )
                )
            )
    ) {

        // ── JUDGMENT BEAM — vertical light ray from top ───────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val beamW = size.width * 0.4f
            val beamX = (size.width - beamW) / 2f
            // Central judgment beam descending
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        violet.copy(alpha = 0.12f * beamPulse),
                        gold.copy(alpha = 0.06f * beamPulse),
                        Color.Transparent
                    ),
                    startY = 0f, endY = size.height * 0.7f
                ),
                topLeft = Offset(beamX, 0f),
                size = androidx.compose.ui.geometry.Size(beamW, size.height * 0.7f)
            )
            // Beam edge lines
            drawLine(
                brush = Brush.verticalGradient(
                    listOf(
                        violet.copy(alpha = 0.3f * beamPulse),
                        Color.Transparent
                    )
                ),
                start = Offset(beamX, 0f),
                end = Offset(beamX, size.height * 0.6f),
                strokeWidth = 0.5f
            )
            drawLine(
                brush = Brush.verticalGradient(
                    listOf(
                        violet.copy(alpha = 0.3f * beamPulse),
                        Color.Transparent
                    )
                ),
                start = Offset(beamX + beamW, 0f),
                end = Offset(beamX + beamW, size.height * 0.6f),
                strokeWidth = 0.5f
            )
            // Floating soul particles ascending
            val particles = listOf(0.3f, 0.45f, 0.5f, 0.55f, 0.62f, 0.38f, 0.52f)
            particles.forEachIndexed { i, xf ->
                val phase = (cosmicPhase + i * 0.9f) % (2 * PI.toFloat())
                val py = size.height * (0.9f - (phase / (2 * PI.toFloat())) * 0.85f)
                val px = size.width * xf + sin(phase * 2) * 10f
                val alpha = sin(phase * 0.5f + 0.3f).coerceAtLeast(0f)
                drawCircle(gold.copy(alpha = alpha * 0.3f), 2f, Offset(px, py))
                drawCircle(Color.White.copy(alpha = alpha * 0.5f), 0.8f, Offset(px, py))
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, null, tint = gold.copy(alpha = 0.7f))
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.loadTeams(eventId) }) {
                            Icon(Icons.Default.Refresh, null, tint = gold.copy(alpha = 0.7f))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { pad ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Header
                item {
                    Column(
                        modifier = Modifier
                            .offset(y = headerY.dp)
                            .alpha(headerA),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(12.dp))
                        // Gold top rule
                        Canvas(Modifier
                            .fillMaxWidth()
                            .height(1.dp)) {
                            drawLine(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        gold.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                ),
                                Offset(0f, 0f), Offset(size.width, 0f), 0.8f
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "SOULS",
                            color = gold.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "AWAITING\nJUDGMENT",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1.5).sp,
                            lineHeight = 44.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        if (state.teams.isNotEmpty()) {
                            Text(
                                "${state.teams.size} SOUL${if (state.teams.size != 1) "S" else ""} PRESENTED",
                                color = violet.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Canvas(Modifier
                            .fillMaxWidth()
                            .height(1.dp)) {
                            drawLine(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        gold.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                ),
                                Offset(0f, 0f), Offset(size.width, 0f), 0.8f
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }

                // Loading
                if (state.isLoading) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CSILoader(LoaderSize.LARGE)
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Summoning souls...",
                                    color = gold.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // Error
                if (!state.isLoading && state.error != null) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF150005))
                                .border(
                                    1.dp,
                                    Color(0xFFEF4444).copy(alpha = 0.4f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("⚠", fontSize = 28.sp, color = Color(0xFFEF4444))
                                Spacer(Modifier.height(8.dp))
                                Text(state.error ?: "", color = Color(0xFFFCA5A5), fontSize = 13.sp)
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                                        .clickable { viewModel.loadTeams(eventId) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "RETRY",
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Empty
                if (!state.isLoading && state.error == null && state.teams.isEmpty()) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Canvas(Modifier.size(60.dp)) {
                                    val c = Offset(size.width / 2f, size.height / 2f)
                                    drawCircle(
                                        violet.copy(alpha = 0.15f),
                                        size.minDimension / 2f,
                                        c
                                    )
                                    drawCircle(
                                        violet.copy(alpha = 0.3f),
                                        size.minDimension / 2f,
                                        c,
                                        style = Stroke(0.8f)
                                    )
                                }
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    "THE COURT IS EMPTY",
                                    color = gold.copy(alpha = 0.4f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "No souls assigned for this seal",
                                    color = Color.White.copy(alpha = 0.25f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Soul vessel cards
                itemsIndexed(state.teams, key = { _, t -> t.id }) { index, team ->
                    SoulVesselCard(
                        team = team,
                        index = index,
                        eventId = eventId,
                        navController = navController
                    )
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun SoulVesselCard(
    team: TeamResponseDto,
    index: Int,
    eventId: Long,
    navController: NavHostController
) {
    val inf = rememberInfiniteTransition(label = "soul_$index")

    // Soul colors — ethereal, each unique
    val soulPalettes = listOf(
        Triple(Color(0xFF00E5FF), Color(0xFF80F8FF), Color(0xFF003040)),
        Triple(Color(0xFFBB86FC), Color(0xFFDDB8FF), Color(0xFF2A0050)),
        Triple(Color(0xFF69FF89), Color(0xFFB8FFD0), Color(0xFF003020)),
        Triple(Color(0xFFFFD700), Color(0xFFFFF0A0), Color(0xFF302000)),
        Triple(Color(0xFFFF8A80), Color(0xFFFFBBB0), Color(0xFF300010)),
        Triple(Color(0xFF82B1FF), Color(0xFFBDD0FF), Color(0xFF001050)),
    )
    val (soulCore, soulAura, soulDeep) = soulPalettes[index % soulPalettes.size]

    val breathe by inf.animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(3000 + index * 300, easing = LinearEasing)), "br"
    )
    val rotate by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(10000 + index * 800, easing = LinearEasing)), "rt"
    )
    val ripple by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2000 + index * 200), RepeatMode.Restart), "rp"
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 120L + 300L); visible = true }
    val entranceY by animateFloatAsState(
        if (visible) 0f else 50f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "ey$index"
    )
    val entranceA by animateFloatAsState(if (visible) 1f else 0f, tween(500), label = "ea$index")

    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        if (pressed) 0.96f else 1f,
        spring(stiffness = Spring.StiffnessHigh), label = "ps$index"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = entranceY.dp)
            .alpha(entranceA)
            .scale(pressScale)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF07040F),
                            soulDeep.copy(alpha = 0.4f),
                            Color(0xFF04030A)
                        )
                    )
                )
                .border(
                    1.dp, Brush.linearGradient(
                        listOf(
                            soulCore.copy(alpha = 0.5f + sin(breathe).toFloat() * 0.2f),
                            soulCore.copy(alpha = 0.08f),
                            soulCore.copy(alpha = 0.3f + sin(breathe + 1f).toFloat() * 0.15f)
                        )
                    ), RoundedCornerShape(24.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                        onTap = { navController.navigate("${Routes.JUDGE_CRITERIA}/$eventId/${team.id}") }
                    )
                }
        ) {
            Row(
                modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── SOUL VESSEL ORB ───────────────────────────────────
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                    Canvas(modifier = Modifier.size(90.dp)) {
                        val cx = size.width / 2f;
                        val cy = size.height / 2f
                        val r = size.minDimension / 2.2f

                        // Ripple ring expanding outward
                        val rippleR = r * (1f + ripple * 0.6f)
                        drawCircle(
                            soulCore.copy(alpha = (0.5f - ripple * 0.5f).coerceAtLeast(0f)),
                            rippleR, Offset(cx, cy), style = Stroke(1f)
                        )

                        // Outer aura glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    soulAura.copy(alpha = (0.1f + sin(breathe).toFloat() * 0.06f)),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy), radius = r * 1.5f
                            ),
                            radius = r * 1.5f, center = Offset(cx, cy)
                        )

                        // Main vessel body
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    soulAura.copy(alpha = 0.3f + sin(breathe).toFloat() * 0.1f),
                                    soulCore.copy(alpha = 0.5f), soulDeep
                                ),
                                center = Offset(cx - r * 0.2f, cy - r * 0.2f), radius = r * 1.5f
                            ),
                            radius = r, center = Offset(cx, cy)
                        )

                        // Rotating constellation inside vessel
                        rotate(rotate, Offset(cx, cy)) {
                            val points = 5 + (index % 3)
                            repeat(points) { i ->
                                val a1 = i * (360f / points) * PI.toFloat() / 180f
                                val a2 = (i + 2) % points * (360f / points) * PI.toFloat() / 180f
                                val pr = r * 0.55f
                                drawLine(
                                    soulAura.copy(alpha = 0.4f),
                                    Offset(cx + pr * cos(a1), cy + pr * sin(a1)),
                                    Offset(cx + pr * cos(a2), cy + pr * sin(a2)), 0.7f
                                )
                                drawCircle(
                                    soulAura.copy(alpha = 0.7f),
                                    2.5f,
                                    Offset(cx + pr * cos(a1), cy + pr * sin(a1))
                                )
                            }
                        }

                        // Bright soul core
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.8f + sin(breathe).toFloat() * 0.15f),
                                    soulAura.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy), radius = r * 0.25f
                            ),
                            radius = r * 0.25f, center = Offset(cx, cy)
                        )
                        drawCircle(Color.White.copy(alpha = 0.9f), 3f, Offset(cx, cy))
                    }
                }

                // ── SOUL INFO ─────────────────────────────────────────
                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        "SOUL ${String.format("%02d", index + 1)}",
                        color = soulCore.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        team.teamName, color = Color.White,
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )

                    team.leaderName?.let { leader ->
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Herald: $leader",
                            color = soulCore.copy(alpha = 0.65f),
                            fontSize = 11.sp
                        )
                    }

                    if (team.members.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${team.members.size} soul${if (team.members.size != 1) "s" else ""} bound",
                            color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // "Pass judgment" button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        soulCore.copy(alpha = 0.15f),
                                        soulDeep.copy(alpha = 0.5f)
                                    )
                                )
                            )
                            .border(1.dp, soulCore.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "PASS JUDGMENT  ▶", color = soulCore, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}