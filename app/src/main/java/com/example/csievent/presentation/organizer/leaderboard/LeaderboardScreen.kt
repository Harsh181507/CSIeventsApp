package com.example.csievent.presentation.organizer.leaderboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.csievent.data.remote.dto.score.LeaderboardResponseDto
import com.example.csievent.presentation.student.CSILoader
import com.example.csievent.presentation.student.LoaderSize
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── fixed stars ───────────────────────────────────────────────────────────────
private val LB_STARS = listOf(
    0.05f to 0.03f, 0.91f to 0.06f, 0.42f to 0.02f, 0.16f to 0.14f, 0.74f to 0.09f,
    0.03f to 0.28f, 0.96f to 0.21f, 0.30f to 0.37f, 0.82f to 0.43f, 0.09f to 0.51f,
    0.67f to 0.56f, 0.53f to 0.80f, 0.22f to 0.74f, 0.86f to 0.68f, 0.47f to 0.90f,
    0.70f to 0.87f, 0.34f to 0.62f, 0.61f to 0.20f, 0.49f to 0.46f, 0.25f to 0.33f,
    0.78f to 0.72f, 0.38f to 0.55f, 0.92f to 0.83f, 0.13f to 0.67f, 0.57f to 0.96f
)

// rank colours
private val RANK_1_COL = Color(0xFFFFD700)   // gold
private val RANK_2_COL = Color(0xFFC0C8D8)   // silver
private val RANK_3_COL = Color(0xFFCD7F32)   // bronze
private val REST_COL = Color(0xFF4B3CC8)   // indigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    eventId: Long,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(eventId) { viewModel.loadLeaderboard(eventId) }

    val inf = rememberInfiniteTransition(label = "lb")

    val twinkle by inf.animateFloat(
        0f, (2f * PI).toFloat(),
        infiniteRepeatable(tween(8000, easing = LinearEasing)), "tw"
    )
    val nebDrift by inf.animateFloat(
        0f, 20f,
        infiniteRepeatable(tween(13000, easing = EaseInOutSine), RepeatMode.Reverse), "nd"
    )
    val novaRot by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(6000, easing = LinearEasing)), "nr"
    )
    val novaRot2 by inf.animateFloat(
        360f, 0f,
        infiniteRepeatable(tween(10000, easing = LinearEasing)), "nr2"
    )
    val novaPulse by inf.animateFloat(
        0.90f, 1.10f,
        infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse), "np"
    )
    val planet2Rot by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(9000, easing = LinearEasing)), "p2"
    )
    val planet3Rot by inf.animateFloat(
        360f, 0f,
        infiniteRepeatable(tween(12000, easing = LinearEasing)), "p3"
    )
    val particleT by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(4000, easing = LinearEasing)), "pt"
    )
    val scanLine by inf.animateFloat(
        -0.05f, 1.05f,
        infiniteRepeatable(tween(5000, easing = LinearEasing)), "sl"
    )
    val crownGlow by inf.animateFloat(
        0.4f, 1f,
        infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse), "cg"
    )

    // header entrance
    var headerIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); headerIn = true }
    val headerA by animateFloatAsState(if (headerIn) 1f else 0f, tween(700), label = "ha")
    val headerY by animateFloatAsState(
        if (headerIn) 0f else -50f,
        spring(0.6f, Spring.StiffnessMediumLow), label = "hy"
    )

    val top3 = state.leaderboard.take(3)
    val rest = state.leaderboard.drop(3)
    val maxScore = state.leaderboard.maxOfOrNull { it.totalScore } ?: 1

    Box(Modifier
        .fillMaxSize()
        .background(Color(0xFF020912))) {

        // ── BACKGROUND ────────────────────────────────────────────────
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF030B1A), Color(0xFF020810), Color(0xFF040614))
                )
            )
            // Nebulae
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF1A0A50).copy(alpha = 0.5f), Color.Transparent),
                    radius = 450f,
                    center = Offset(size.width * 0.82f + nebDrift, size.height * 0.18f)
                ),
                radius = 450f, center = Offset(size.width * 0.82f + nebDrift, size.height * 0.18f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF041830).copy(alpha = 0.4f), Color.Transparent),
                    radius = 350f, center = Offset(size.width * 0.1f, size.height * 0.72f)
                ),
                radius = 350f, center = Offset(size.width * 0.1f, size.height * 0.72f)
            )
            // Stars
            LB_STARS.forEachIndexed { i, (x, y) ->
                val tw = (sin(twinkle + i * 0.58f) * 0.3f + 0.7f).toFloat()
                val r = when (i % 5) {
                    0 -> 2.3f; 1 -> 1.7f; else -> 1.1f
                }
                val pos = Offset(size.width * x, size.height * y)
                drawCircle(Color.White.copy(alpha = tw * 0.6f), r, pos)
                if (i % 5 == 0) {
                    drawLine(
                        Color.White.copy(alpha = tw * 0.16f),
                        Offset(pos.x - 7f, pos.y),
                        Offset(pos.x + 7f, pos.y),
                        0.5f
                    )
                    drawLine(
                        Color.White.copy(alpha = tw * 0.16f),
                        Offset(pos.x, pos.y - 7f),
                        Offset(pos.x, pos.y + 7f),
                        0.5f
                    )
                }
            }
            // Scan line
            val sp = size.height * scanLine
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0xFF4B3CC8).copy(alpha = 0.05f),
                        Color(0xFF4B3CC8).copy(alpha = 0.08f),
                        Color(0xFF4B3CC8).copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    startY = sp - 25f, endY = sp + 25f
                ), size = size
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "GALACTIC RANKINGS", color = Color.White,
                                fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace, letterSpacing = 2.sp
                            )
                            Text(
                                "Mission Score Board",
                                color = Color(0xFF8C83E4).copy(alpha = 0.5f), fontSize = 10.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.loadLeaderboard(eventId) }) {
                            Icon(Icons.Default.Refresh, null, tint = Color(0xFF8C83E4))
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
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {

                // ── PODIUM HERO ───────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .offset(y = headerY.dp)
                            .alpha(headerA),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(Modifier
                            .fillMaxWidth()
                            .height(300.dp)) {
                            val w = size.width
                            val h = size.height

                            // ── Rank 1 — Gold Supernova (centre top) ──
                            if (top3.isNotEmpty()) {
                                val cx1 = w * 0.5f;
                                val cy1 = h * 0.36f
                                val r1 = 36.dp.toPx() * novaPulse

                                // Corona rays
                                rotate(novaRot, Offset(cx1, cy1)) {
                                    for (ray in 0..11) {
                                        val a = ray * PI.toFloat() / 6f
                                        val rL =
                                            r1 * 0.5f + if (ray % 2 == 0) r1 * 0.5f else r1 * 0.25f
                                        drawLine(
                                            RANK_1_COL.copy(alpha = 0.35f),
                                            Offset(cx1 + r1 * cos(a), cy1 + r1 * sin(a)),
                                            Offset(
                                                cx1 + (r1 + rL) * cos(a),
                                                cy1 + (r1 + rL) * sin(a)
                                            ),
                                            if (ray % 2 == 0) 1.5f else 0.8f
                                        )
                                    }
                                }
                                // Outer glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(RANK_1_COL.copy(alpha = 0.25f), Color.Transparent),
                                        center = Offset(cx1, cy1), radius = r1 * 3f
                                    ),
                                    radius = r1 * 3f, center = Offset(cx1, cy1)
                                )
                                // Orbiting ring
                                rotate(novaRot2, Offset(cx1, cy1)) {
                                    drawCircle(
                                        RANK_1_COL.copy(alpha = 0.4f), r1 * 1.8f, Offset(cx1, cy1),
                                        style = Stroke(
                                            1.2f,
                                            pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(
                                                    12f,
                                                    6f
                                                )
                                            )
                                        )
                                    )
                                    drawCircle(
                                        RANK_1_COL.copy(alpha = 0.8f),
                                        4f,
                                        Offset(cx1 + r1 * 1.8f, cy1)
                                    )
                                }
                                // 8 orbiting particles
                                for (p in 0..7) {
                                    val t = (particleT + p / 8f) % 1f
                                    val a = t * 2f * PI.toFloat()
                                    val px = cx1 + r1 * 1.45f * cos(a);
                                    val py = cy1 + r1 * 1.45f * sin(a)
                                    drawCircle(RANK_1_COL.copy(alpha = 0.6f), 2.5f, Offset(px, py))
                                }
                                // Star body
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(Color.White, RANK_1_COL, Color(0xFF8B4A00)),
                                        center = Offset(cx1 - r1 * 0.2f, cy1 - r1 * 0.25f),
                                        radius = r1 * 1.5f
                                    ),
                                    radius = r1, center = Offset(cx1, cy1)
                                )
                                drawCircle(
                                    Color.White.copy(alpha = 0.5f), r1 * 0.3f,
                                    Offset(cx1 - r1 * 0.28f, cy1 - r1 * 0.32f)
                                )
                                // Crown above
                                val crownY = cy1 - r1 - 18f
                                drawLine(
                                    RANK_1_COL.copy(alpha = crownGlow),
                                    Offset(cx1 - 12f, crownY + 10f), Offset(cx1 - 12f, crownY), 2f
                                )
                                drawLine(
                                    RANK_1_COL.copy(alpha = crownGlow),
                                    Offset(cx1, crownY + 10f), Offset(cx1, crownY - 6f), 2f
                                )
                                drawLine(
                                    RANK_1_COL.copy(alpha = crownGlow),
                                    Offset(cx1 + 12f, crownY + 10f), Offset(cx1 + 12f, crownY), 2f
                                )
                                drawLine(
                                    RANK_1_COL.copy(alpha = crownGlow),
                                    Offset(cx1 - 12f, crownY + 10f),
                                    Offset(cx1 + 12f, crownY + 10f),
                                    2f
                                )
                                drawCircle(
                                    RANK_1_COL.copy(alpha = crownGlow),
                                    3f,
                                    Offset(cx1 - 12f, crownY)
                                )
                                drawCircle(
                                    RANK_1_COL.copy(alpha = crownGlow),
                                    4f,
                                    Offset(cx1, crownY - 6f)
                                )
                                drawCircle(
                                    RANK_1_COL.copy(alpha = crownGlow),
                                    3f,
                                    Offset(cx1 + 12f, crownY)
                                )
                            }

                            // ── Rank 2 — Silver Planet (left) ────────
                            if (top3.size >= 2) {
                                val cx2 = w * 0.22f;
                                val cy2 = h * 0.52f
                                val r2 = 24.dp.toPx()
                                // Orbital ring
                                rotate(planet2Rot, Offset(cx2, cy2)) {
                                    drawOval(
                                        RANK_2_COL.copy(alpha = 0.4f),
                                        Offset(cx2 - r2 * 1.7f, cy2 - r2 * 0.35f),
                                        Size(r2 * 3.4f, r2 * 0.7f),
                                        style = Stroke(1.2f)
                                    )
                                    drawCircle(
                                        RANK_2_COL.copy(alpha = 0.7f),
                                        3f,
                                        Offset(cx2 + r2 * 1.7f, cy2)
                                    )
                                }
                                // Glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(RANK_2_COL.copy(alpha = 0.2f), Color.Transparent),
                                        center = Offset(cx2, cy2), radius = r2 * 2.2f
                                    ),
                                    radius = r2 * 2.2f, center = Offset(cx2, cy2)
                                )
                                // Body
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(Color.White, RANK_2_COL, Color(0xFF3A4060)),
                                        center = Offset(cx2 - r2 * 0.2f, cy2 - r2 * 0.25f),
                                        radius = r2 * 1.5f
                                    ),
                                    radius = r2, center = Offset(cx2, cy2)
                                )
                                drawCircle(
                                    Color.White.copy(alpha = 0.35f), r2 * 0.28f,
                                    Offset(cx2 - r2 * 0.28f, cy2 - r2 * 0.3f)
                                )
                            }

                            // ── Rank 3 — Bronze Planet (right) ───────
                            if (top3.size >= 3) {
                                val cx3 = w * 0.78f;
                                val cy3 = h * 0.52f
                                val r3 = 20.dp.toPx()
                                // Orbital ring
                                rotate(planet3Rot, Offset(cx3, cy3)) {
                                    drawOval(
                                        RANK_3_COL.copy(alpha = 0.4f),
                                        Offset(cx3 - r3 * 1.6f, cy3 - r3 * 0.32f),
                                        Size(r3 * 3.2f, r3 * 0.64f),
                                        style = Stroke(1f)
                                    )
                                    drawCircle(
                                        RANK_3_COL.copy(alpha = 0.7f),
                                        2.5f,
                                        Offset(cx3 + r3 * 1.6f, cy3)
                                    )
                                }
                                // Glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(RANK_3_COL.copy(alpha = 0.18f), Color.Transparent),
                                        center = Offset(cx3, cy3), radius = r3 * 2f
                                    ),
                                    radius = r3 * 2f, center = Offset(cx3, cy3)
                                )
                                // Body
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(Color.White, RANK_3_COL, Color(0xFF3A2000)),
                                        center = Offset(cx3 - r3 * 0.2f, cy3 - r3 * 0.22f),
                                        radius = r3 * 1.5f
                                    ),
                                    radius = r3, center = Offset(cx3, cy3)
                                )
                                drawCircle(
                                    Color.White.copy(alpha = 0.3f), r3 * 0.26f,
                                    Offset(cx3 - r3 * 0.26f, cy3 - r3 * 0.28f)
                                )
                            }

                            // ── Constellation lines connecting podium ─
                            if (top3.size >= 2)
                                drawLine(
                                    RANK_1_COL.copy(alpha = 0.15f),
                                    Offset(w * 0.5f, h * 0.36f), Offset(w * 0.22f, h * 0.52f), 0.8f
                                )
                            if (top3.size >= 3)
                                drawLine(
                                    RANK_1_COL.copy(alpha = 0.15f),
                                    Offset(w * 0.5f, h * 0.36f), Offset(w * 0.78f, h * 0.52f), 0.8f
                                )
                            if (top3.size >= 3)
                                drawLine(
                                    RANK_2_COL.copy(alpha = 0.08f),
                                    Offset(w * 0.22f, h * 0.52f), Offset(w * 0.78f, h * 0.52f), 0.5f
                                )

                            // ── Podium bases ──────────────────────────
                            val podW = 80.dp.toPx();
                            val podBot = h * 0.88f
                            // 1st — tallest, centre
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        RANK_1_COL.copy(alpha = 0.35f),
                                        RANK_1_COL.copy(alpha = 0.1f)
                                    )
                                ),
                                topLeft = Offset(w * 0.5f - podW / 2f, podBot - 90f),
                                size = Size(podW, 90f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                            )
                            drawRoundRect(
                                RANK_1_COL.copy(alpha = 0.4f),
                                Offset(w * 0.5f - podW / 2f, podBot - 90f), Size(podW, 90f),
                                androidx.compose.ui.geometry.CornerRadius(6f), style = Stroke(1f)
                            )
                            // 2nd — left
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        RANK_2_COL.copy(alpha = 0.3f),
                                        RANK_2_COL.copy(alpha = 0.08f)
                                    )
                                ),
                                topLeft = Offset(w * 0.22f - podW / 2f, podBot - 60f),
                                size = Size(podW, 60f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                            )
                            drawRoundRect(
                                RANK_2_COL.copy(alpha = 0.35f),
                                Offset(w * 0.22f - podW / 2f, podBot - 60f), Size(podW, 60f),
                                androidx.compose.ui.geometry.CornerRadius(6f), style = Stroke(0.8f)
                            )
                            // 3rd — right
                            if (top3.size >= 3) {
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            RANK_3_COL.copy(alpha = 0.28f),
                                            RANK_3_COL.copy(alpha = 0.07f)
                                        )
                                    ),
                                    topLeft = Offset(w * 0.78f - podW / 2f, podBot - 40f),
                                    size = Size(podW, 40f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                                )
                                drawRoundRect(
                                    RANK_3_COL.copy(alpha = 0.3f),
                                    Offset(w * 0.78f - podW / 2f, podBot - 40f),
                                    Size(podW, 40f),
                                    androidx.compose.ui.geometry.CornerRadius(6f),
                                    style = Stroke(0.8f)
                                )
                            }
                        }

                        // Podium text labels — Compose, not Canvas
                        Box(Modifier
                            .fillMaxWidth()
                            .height(300.dp)) {
                            // Rank 1 name
                            if (top3.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🥇", fontSize = 16.sp)
                                    Text(
                                        top3[0].teamName, color = RANK_1_COL, fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold, maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(88.dp)
                                    )
                                    Text(
                                        "${top3[0].totalScore} pts",
                                        color = RANK_1_COL.copy(alpha = 0.7f), fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            // Rank 2 name
                            if (top3.size >= 2) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(start = 10.dp, bottom = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🥈", fontSize = 14.sp)
                                    Text(
                                        top3[1].teamName, color = RANK_2_COL, fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold, maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(80.dp)
                                    )
                                    Text(
                                        "${top3[1].totalScore} pts",
                                        color = RANK_2_COL.copy(alpha = 0.7f), fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            // Rank 3 name
                            if (top3.size >= 3) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 10.dp, bottom = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🥉", fontSize = 14.sp)
                                    Text(
                                        top3[2].teamName, color = RANK_3_COL, fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold, maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(80.dp)
                                    )
                                    Text(
                                        "${top3[2].totalScore} pts",
                                        color = RANK_3_COL.copy(alpha = 0.7f), fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // ── LOADING ───────────────────────────────────────────
                if (state.isLoading) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CSILoader(size = LoaderSize.LARGE)
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Scanning the galaxy for scores...",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.6f), fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // ── EMPTY ─────────────────────────────────────────────
                if (!state.isLoading && state.error == null && state.leaderboard.isEmpty()) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌌", fontSize = 40.sp)
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "No scores yet", color = Color.White, fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Scores will appear once judging begins",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.5f), fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // ── ERROR ─────────────────────────────────────────────
                state.error?.let { err ->
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1A0808))
                                .border(
                                    1.dp,
                                    Color(0xFFEF4444).copy(alpha = 0.35f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⚠️", fontSize = 16.sp)
                                Text(
                                    err, color = Color(0xFFFCA5A5), fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.clearError() }) {
                                    Text("✕", color = Color(0xFFEF4444).copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }

                // ── SECTION HEADER ────────────────────────────────────
                if (!state.isLoading && rest.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(REST_COL.copy(alpha = 0.6f))
                            )
                            Text(
                                "FULL STANDINGS",
                                color = Color(0xFF8C83E4).copy(alpha = 0.5f), fontSize = 10.sp,
                                fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                            )
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                REST_COL.copy(alpha = 0.3f), Color.Transparent
                                            )
                                        )
                                    )
                            )
                            Text(
                                "${state.leaderboard.size} teams",
                                color = Color(0xFF8C83E4).copy(alpha = 0.4f), fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // ── ALL RANK CARDS ────────────────────────────────────
                if (!state.isLoading) {
                    itemsIndexed(state.leaderboard, key = { _, e -> e.rank }) { _, entry ->
                        Box(Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 10.dp)) {
                            LbRankCard(entry = entry, maxScore = maxScore)
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// =============================================================================
// RANK CARD
// =============================================================================
@Composable
private fun LbRankCard(
    entry: LeaderboardResponseDto,
    maxScore: Long
) {
    val rank = entry.rank
    val accent = when (rank) {
        1 -> RANK_1_COL; 2 -> RANK_2_COL; 3 -> RANK_3_COL; else -> REST_COL
    }
    val medal = when (rank) {
        1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> null
    }
    val scorePct = (entry.totalScore.toFloat() / maxScore.toFloat()).coerceIn(0f, 1f)

    // Stagger entrance
    var vis by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay((rank - 1) * 80L + 400L); vis = true }
    val entX by animateFloatAsState(
        if (vis) 0f else -120f,
        spring(0.65f, Spring.StiffnessMediumLow), label = "ex$rank"
    )
    val entA by animateFloatAsState(if (vis) 1f else 0f, tween(350), label = "ea$rank")

    // Score bar animation
    val barAnim by animateFloatAsState(
        if (vis) scorePct else 0f,
        tween(900, easing = EaseOutCubic), label = "ba$rank"
    )

    val inf = rememberInfiniteTransition(label = "lbc$rank")
    val scanC by inf.animateFloat(
        -0.1f, 1.1f,
        infiniteRepeatable(tween(3500 + (rank * 200), easing = LinearEasing)), "sc"
    )

    Box(Modifier
        .fillMaxWidth()
        .offset(x = entX.dp)
        .alpha(entA)) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = if (rank <= 3) 0.08f else 0.04f),
                            Color(0xFF04040E)
                        )
                    )
                )
                .border(
                    1.dp, Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = if (rank <= 3) 0.5f else 0.2f),
                            accent.copy(alpha = if (rank <= 3) 0.15f else 0.06f),
                            accent.copy(alpha = if (rank <= 3) 0.5f else 0.2f)
                        )
                    ),
                    RoundedCornerShape(16.dp)
                )
        ) {
            // Internal scan line
            Canvas(Modifier
                .fillMaxWidth()
                .height(72.dp)) {
                val sy = size.height * scanC
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            accent.copy(alpha = 0.04f),
                            accent.copy(alpha = 0.07f),
                            accent.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        startY = sy - 16f, endY = sy + 16f
                    ), size = size
                )
            }

            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Rank badge
                        Box(
                            Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(accent.copy(alpha = 0.2f), Color(0xFF04040E))
                                    )
                                )
                                .border(
                                    1.dp,
                                    accent.copy(alpha = if (rank <= 3) 0.6f else 0.3f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (medal != null) Text(medal, fontSize = 16.sp)
                            else Text(
                                "#$rank", color = accent, fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                            )
                        }
                        // Team name
                        Column {
                            Text(
                                entry.teamName,
                                color = Color.White.copy(alpha = if (rank <= 3) 1f else 0.8f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "RANK #$rank", color = accent.copy(alpha = 0.45f), fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace, letterSpacing = 1.sp
                            )
                        }
                    }
                    // Score
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${entry.totalScore}", color = accent, fontSize = 22.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp
                        )
                        Text(
                            "POINTS", color = accent.copy(alpha = 0.4f), fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace, letterSpacing = 1.sp
                        )
                    }
                }

                // Score bar
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF0A0A1A))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(barAnim)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accent.copy(alpha = 0.6f), accent)
                                )
                            )
                    )
                }
                // Percentage text
                Text(
                    "${(scorePct * 100).toInt()}% of max score",
                    color = accent.copy(alpha = 0.35f), fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        // HUD brackets — only for top 3
        if (rank <= 3) {
            Canvas(Modifier.matchParentSize()) {
                val s = 12f;
                val w2 = 1.2f;
                val c = accent.copy(alpha = 0.4f)
                drawLine(c, Offset(0f, s), Offset(0f, 0f), w2); drawLine(
                c,
                Offset(0f, 0f),
                Offset(s, 0f),
                w2
            )
                drawLine(c, Offset(size.width - s, 0f), Offset(size.width, 0f), w2); drawLine(
                c,
                Offset(size.width, 0f),
                Offset(size.width, s),
                w2
            )
                drawLine(c, Offset(0f, size.height - s), Offset(0f, size.height), w2); drawLine(
                c,
                Offset(0f, size.height),
                Offset(s, size.height),
                w2
            )
                drawLine(
                    c,
                    Offset(size.width - s, size.height),
                    Offset(size.width, size.height),
                    w2
                ); drawLine(
                c,
                Offset(size.width, size.height - s),
                Offset(size.width, size.height),
                w2
            )
            }
        }
    }
}