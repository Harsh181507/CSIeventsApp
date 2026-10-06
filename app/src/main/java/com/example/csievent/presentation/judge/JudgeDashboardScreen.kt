package com.example.csievent.presentation.judge

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
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
import com.example.csievent.data.remote.dto.event.JudgeEventResponseDto
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.presentation.student.CSILoader
import com.example.csievent.presentation.student.LoaderSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * VOID COURT — Judge Dashboard
 *
 * Visual concept: A cosmic courtroom drifting in the void.
 * Events are rendered as glowing VERDICT SEALS — ancient circular sigil designs
 * drawn entirely with Canvas: concentric rings, geometric rune patterns, rotating
 * inner mandalas, and pulsing energy cores. Each seal has a unique geometric
 * signature based on its index.
 *
 * Background: deep void with slow particle drift (not nebula blobs — individual
 * floating particles that drift upward, giving a "souls ascending" feel).
 *
 * Typography: massive 56sp "VOID COURT" title with a sacred geometry underline
 * drawn via Canvas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JudgeDashboardScreen(
    navController: NavHostController,
    viewModel: JudgeDashboardViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { viewModel.loadJudgeEvents() }

    val inf = rememberInfiniteTransition(label = "void")

    // Slow void rotation — entire field of particles rotates subtly
    val voidRot by inf.animateFloat(
        0f, 360f, infiniteRepeatable(tween(120000, easing = LinearEasing)), "vr"
    )
    // Particle drift phase
    val driftPhase by inf.animateFloat(
        0f, (2 * PI).toFloat(), infiniteRepeatable(tween(8000, easing = LinearEasing)), "dp"
    )
    // Sacred pulse — slow breathe
    val sacredPulse by inf.animateFloat(
        0f, (2 * PI).toFloat(), infiniteRepeatable(tween(5000, easing = LinearEasing)), "sp"
    )
    // Gold shimmer
    val goldShimmer by inf.animateFloat(
        0f, (2 * PI).toFloat(), infiniteRepeatable(tween(3000, easing = LinearEasing)), "gs"
    )

    // Header entrance
    var headerIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(200); headerIn = true }
    val headerA by animateFloatAsState(if (headerIn) 1f else 0f, tween(1200), label = "ha")
    val headerY by animateFloatAsState(
        if (headerIn) 0f else 30f,
        spring(Spring.DampingRatioLowBouncy, Spring.StiffnessVeryLow), label = "hy"
    )

    // Void colors
    val voidDeep = Color(0xFF010108)
    val voidIndigo = Color(0xFF05021A)
    val gold = Color(0xFFD4AF37)
    val goldLight = Color(0xFFFFF0A0)
    val cyanSeal = Color(0xFF00E5FF)
    val violetSeal = Color(0xFFBB86FC)

    // Particle positions — fixed seeds
    val particles = remember {
        (0 until 60).map { i ->
            Triple(
                (sin(i * 2.399f) * 0.5f + 0.5f),  // x 0..1
                (cos(i * 1.618f) * 0.5f + 0.5f),  // y 0..1
                (i * 0.618f % 1f)                   // phase offset
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(voidIndigo, voidDeep, Color(0xFF000005)),
                    radius = 1800f
                )
            )
    ) {

        // ── VOID PARTICLE FIELD ───────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Sacred geometry background — large faint rings centered on screen
            val cx = size.width / 2f
            val cy = size.height * 0.38f
            // Outer sacred ring
            drawCircle(
                color = gold.copy(alpha = 0.03f + sin(sacredPulse).toFloat() * 0.015f),
                radius = minOf(size.width, size.height) * 0.42f,
                center = Offset(cx, cy), style = Stroke(0.5f)
            )
            drawCircle(
                color = gold.copy(alpha = 0.025f),
                radius = minOf(size.width, size.height) * 0.32f,
                center = Offset(cx, cy), style = Stroke(0.5f)
            )
            // 6-point star lines (faint)
            repeat(6) { i ->
                val angle = i * 60f * PI.toFloat() / 180f + voidRot * PI.toFloat() / 180f * 0.05f
                val r = minOf(size.width, size.height) * 0.38f
                drawLine(
                    color = gold.copy(alpha = 0.02f),
                    start = Offset(cx, cy),
                    end = Offset(cx + r * cos(angle), cy + r * sin(angle)),
                    strokeWidth = 0.5f
                )
            }

            // Floating particles — drift upward slowly
            particles.forEach { (xFrac, yFrac, phase) ->
                val drift = (driftPhase + phase * 2 * PI.toFloat()) % (2 * PI.toFloat())
                val px = size.width * xFrac + sin(drift * 0.5f) * 15f
                // drift upward then wrap
                val rawY =
                    size.height * yFrac - (driftPhase / (2 * PI.toFloat())) * size.height * 0.3f
                val py = ((rawY % size.height) + size.height) % size.height
                val alpha = (sin(drift) * 0.3f + 0.4f).coerceIn(0f, 0.7f)
                val radius = if ((xFrac * 100).toInt() % 5 == 0) 1.8f else 0.9f
                val col = when ((xFrac * 10).toInt() % 3) {
                    0 -> goldLight.copy(alpha = alpha * 0.6f)
                    1 -> cyanSeal.copy(alpha = alpha * 0.3f)
                    else -> Color.White.copy(alpha = alpha * 0.4f)
                }
                drawCircle(color = col, radius = radius, center = Offset(px, py))
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = { viewModel.loadJudgeEvents() }) {
                            Icon(Icons.Default.Refresh, null, tint = gold.copy(alpha = 0.8f))
                        }
                        IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                            Icon(Icons.Default.AccountCircle, "Profile", tint = gold.copy(alpha = 0.8f))
                        }
                        IconButton(onClick = {
                            scope.launch {
                                viewModel.logout()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(0) {
                                        inclusive = true
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, null, tint = gold.copy(alpha = 0.8f))
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
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // ── VOID COURT HEADER ─────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .offset(y = headerY.dp)
                            .alpha(headerA),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(20.dp))

                        // Sacred geometry title underline drawn as canvas
                        Canvas(modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)) {
                            val gw = size.width * 0.6f
                            val gx = (size.width - gw) / 2f
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        gold.copy(alpha = 0.5f),
                                        gold,
                                        gold.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(gx, 1f), end = Offset(gx + gw, 1f), strokeWidth = 1f
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Huge title
                        Text(
                            "VOID\nCOURT",
                            color = Color.White,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-3).sp,
                            lineHeight = 58.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(Modifier.height(6.dp))

                        // Glowing subtitle
                        Text(
                            "THE COURT OF COSMIC JUDGMENT",
                            color = gold.copy(alpha = 0.6f + sin(goldShimmer).toFloat() * 0.2f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        // Stats — three sacred orbs
                        if (state.events.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                VoidStatOrb(
                                    state.events.size.toString(),
                                    "SEALED",
                                    gold,
                                    Modifier.weight(1f)
                                )
                                VoidStatOrb(
                                    state.events.count { !it.scoringLocked }.toString(),
                                    "OPEN",
                                    cyanSeal,
                                    Modifier.weight(1f)
                                )
                                VoidStatOrb(
                                    state.events.count { it.scoringLocked }.toString(),
                                    "JUDGED",
                                    violetSeal,
                                    Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Sacred divider
                        Canvas(modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)) {
                            val cx2 = size.width / 2f
                            val cy2 = size.height / 2f
                            // Center diamond
                            val d = 5f
                            drawLine(
                                gold.copy(alpha = 0.4f),
                                Offset(cx2 - d, cy2),
                                Offset(cx2, cy2 - d),
                                0.8f
                            )
                            drawLine(
                                gold.copy(alpha = 0.4f),
                                Offset(cx2, cy2 - d),
                                Offset(cx2 + d, cy2),
                                0.8f
                            )
                            drawLine(
                                gold.copy(alpha = 0.4f),
                                Offset(cx2 + d, cy2),
                                Offset(cx2, cy2 + d),
                                0.8f
                            )
                            drawLine(
                                gold.copy(alpha = 0.4f),
                                Offset(cx2, cy2 + d),
                                Offset(cx2 - d, cy2),
                                0.8f
                            )
                            // Lines out from diamond
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        gold.copy(alpha = 0.3f)
                                    )
                                ),
                                start = Offset(0f, cy2),
                                end = Offset(cx2 - d - 4f, cy2),
                                strokeWidth = 0.5f
                            )
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        gold.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(cx2 + d + 4f, cy2),
                                end = Offset(size.width, cy2),
                                strokeWidth = 0.5f
                            )
                        }
                    }
                }

                // ── LOADING ───────────────────────────────────────────
                if (state.isLoading) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CSILoader(LoaderSize.LARGE)
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Consulting the void...",
                                    color = gold.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // ── ERROR ─────────────────────────────────────────────
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
                                Text("⚠", fontSize = 32.sp, color = Color(0xFFEF4444))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    state.error ?: "",
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(Modifier.height(14.dp))
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                                        .border(
                                            1.dp,
                                            Color(0xFFEF4444).copy(alpha = 0.3f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.loadJudgeEvents() }
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        "RETRY",
                                        color = Color(0xFFEF4444),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ── EMPTY ─────────────────────────────────────────────
                if (!state.isLoading && state.error == null && state.events.isEmpty()) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Empty void sigil
                                Canvas(modifier = Modifier.size(80.dp)) {
                                    val c = Offset(size.width / 2f, size.height / 2f)
                                    val r = size.minDimension / 2f
                                    drawCircle(gold.copy(alpha = 0.15f), r, c, style = Stroke(1f))
                                    drawCircle(
                                        gold.copy(alpha = 0.08f),
                                        r * 0.65f,
                                        c,
                                        style = Stroke(0.5f)
                                    )
                                    repeat(8) { i ->
                                        val a = i * 45f * PI.toFloat() / 180f
                                        drawLine(
                                            gold.copy(alpha = 0.1f),
                                            c,
                                            Offset(c.x + r * cos(a), c.y + r * sin(a)),
                                            0.5f
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "THE VOID IS EMPTY",
                                    color = gold.copy(alpha = 0.4f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "No seals have been assigned",
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // ── VERDICT SEAL CARDS ────────────────────────────────
                itemsIndexed(state.events, key = { _, e -> e.id }) { index, event ->
                    VerdictSealCard(
                        event = event,
                        index = index,
                        sacredPulse = sacredPulse,
                        goldShimmer = goldShimmer,
                        navController = navController
                    )
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

// =============================================================================
// VERDICT SEAL CARD
// The centrepiece — each event is a glowing sigil/seal drawn entirely in Canvas.
// =============================================================================

@Composable
private fun VerdictSealCard(
    event: JudgeEventResponseDto,
    index: Int,
    sacredPulse: Float,
    goldShimmer: Float,
    navController: NavHostController
) {
    val isLocked = event.scoringLocked
    val inf = rememberInfiniteTransition(label = "seal_$index")

    // Seal rotation — inner mandala spins
    val innerRot by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(if (isLocked) 20000 else 8000, easing = LinearEasing)), "ir"
    )
    val outerRot by inf.animateFloat(
        360f, 0f,
        infiniteRepeatable(tween(if (isLocked) 30000 else 12000, easing = LinearEasing)), "or"
    )
    val energyPulse by inf.animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(2500, easing = LinearEasing)), "ep"
    )
    val orbAlpha by inf.animateFloat(
        0.4f, 1f,
        infiniteRepeatable(tween(1500), RepeatMode.Reverse), "oa"
    )

    // Entrance — rise from below
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 150L + 400L); visible = true }
    val entranceY by animateFloatAsState(
        if (visible) 0f else 60f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "ey$index"
    )
    val entranceA by animateFloatAsState(if (visible) 1f else 0f, tween(500), label = "ea$index")

    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        if (pressed) 0.96f else 1f, spring(stiffness = Spring.StiffnessHigh), label = "ps$index"
    )

    // Unique accent per seal based on index
    val sealColors = listOf(
        Triple(Color(0xFFD4AF37), Color(0xFFFFF0A0), Color(0xFF8B6914)), // Gold
        Triple(Color(0xFF00E5FF), Color(0xFF80F0FF), Color(0xFF006070)), // Cyan
        Triple(Color(0xFFBB86FC), Color(0xFFDDB8FF), Color(0xFF5A2A8A)), // Violet
        Triple(Color(0xFF69FF89), Color(0xFFB0FFC0), Color(0xFF1A6030)), // Emerald
        Triple(Color(0xFFFF6B6B), Color(0xFFFFB0B0), Color(0xFF8B1A1A)), // Crimson
        Triple(Color(0xFFFFB347), Color(0xFFFFD89B), Color(0xFF8B4500)), // Amber
    )
    val (sealMain, sealLight, sealDeep) = sealColors[index % sealColors.size]
    val dim = if (isLocked) 0.3f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = entranceY.dp)
            .alpha(entranceA)
            .scale(pressScale)
    ) {
        // Outer glow bloom behind card
        if (!isLocked) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            sealMain.copy(alpha = 0.08f + sin(energyPulse).toFloat() * 0.04f),
                            Color.Transparent
                        ),
                        center = Offset(80.dp.toPx(), size.height / 2f),
                        radius = 120.dp.toPx()
                    ),
                    radius = 120.dp.toPx(),
                    center = Offset(80.dp.toPx(), size.height / 2f)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF06040F),
                            Color(0xFF04030A),
                            Color(0xFF030208)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            sealMain.copy(alpha = 0.5f * dim),
                            sealMain.copy(alpha = 0.1f * dim),
                            sealMain.copy(alpha = 0.3f * dim)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                        onTap = {
                            if (!isLocked) navController.navigate("${Routes.JUDGE_EVENT_TEAMS}/${event.id}")
                        }
                    )
                }
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // ── VERDICT SEAL (the star of the show) ──────────────
                Canvas(modifier = Modifier.size(120.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val R = size.minDimension / 2f - 4f

                    // Outer atmosphere bloom
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                sealMain.copy(alpha = (0.15f + sin(energyPulse).toFloat() * 0.08f) * dim),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy), radius = R * 1.4f
                        ),
                        radius = R * 1.4f, center = Offset(cx, cy)
                    )

                    // Outer ring — slowly counter-rotating, with notches
                    rotate(outerRot, Offset(cx, cy)) {
                        drawCircle(
                            sealMain.copy(alpha = 0.5f * dim),
                            R,
                            Offset(cx, cy),
                            style = Stroke(1.2f)
                        )
                        // 12 notch marks on outer ring
                        repeat(12) { i ->
                            val a = i * 30f * PI.toFloat() / 180f
                            val isLong = i % 3 == 0
                            val r1 = R - (if (isLong) 8f else 4f)
                            drawLine(
                                sealMain.copy(alpha = (if (isLong) 0.8f else 0.4f) * dim),
                                Offset(cx + R * cos(a), cy + R * sin(a)),
                                Offset(cx + r1 * cos(a), cy + r1 * sin(a)),
                                if (isLong) 1.5f else 0.8f
                            )
                        }
                    }

                    // Mid ring
                    val midR = R * 0.72f
                    rotate(innerRot * 0.4f, Offset(cx, cy)) {
                        drawCircle(
                            sealMain.copy(alpha = 0.35f * dim),
                            midR,
                            Offset(cx, cy),
                            style = Stroke(0.8f)
                        )
                        // 6-point sacred geometry
                        repeat(6) { i ->
                            val a = i * 60f * PI.toFloat() / 180f
                            drawLine(
                                sealLight.copy(alpha = 0.2f * dim),
                                Offset(cx - midR * cos(a), cy - midR * sin(a)),
                                Offset(cx + midR * cos(a), cy + midR * sin(a)),
                                0.5f
                            )
                        }
                    }

                    // Inner spinning mandala — unique pattern per index
                    val innerR = R * 0.44f
                    rotate(innerRot, Offset(cx, cy)) {
                        val petals = 6 + (index % 3) * 2 // 6, 8, or 10 petals
                        repeat(petals) { i ->
                            val a = i * (360f / petals) * PI.toFloat() / 180f
                            val a2 = a + (360f / petals / 2f) * PI.toFloat() / 180f
                            // Petal shape — line from center to rim, angled
                            drawLine(
                                brush = Brush.linearGradient(
                                    listOf(
                                        sealMain.copy(alpha = 0.6f * dim),
                                        sealLight.copy(alpha = 0.1f * dim)
                                    ),
                                    start = Offset(cx, cy),
                                    end = Offset(cx + innerR * cos(a), cy + innerR * sin(a))
                                ),
                                start = Offset(cx, cy),
                                end = Offset(cx + innerR * cos(a), cy + innerR * sin(a)),
                                strokeWidth = 1f
                            )
                            // Arc connecting petal tips
                            drawLine(
                                sealMain.copy(alpha = 0.3f * dim),
                                Offset(cx + innerR * cos(a), cy + innerR * sin(a)),
                                Offset(cx + innerR * cos(a2), cy + innerR * sin(a2)),
                                0.5f
                            )
                        }
                        // Inner glow core
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    sealLight.copy(alpha = (0.6f + sin(energyPulse).toFloat() * 0.3f) * dim),
                                    sealMain.copy(alpha = 0.1f * dim),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy), radius = innerR * 0.4f
                            ),
                            radius = innerR * 0.4f, center = Offset(cx, cy)
                        )
                        drawCircle(sealLight.copy(alpha = 0.9f * dim), 3f, Offset(cx, cy))
                    }

                    // Locked overlay
                    if (isLocked) {
                        drawCircle(Color(0xFF010108).copy(alpha = 0.7f), R, Offset(cx, cy))
                        drawCircle(Color(0xFFEF4444).copy(alpha = 0.15f), R * 0.3f, Offset(cx, cy))
                        drawLine(
                            Color(0xFFEF4444).copy(alpha = 0.4f),
                            Offset(cx - 8f, cy - 8f),
                            Offset(cx + 8f, cy + 8f),
                            1.5f
                        )
                        drawLine(
                            Color(0xFFEF4444).copy(alpha = 0.4f),
                            Offset(cx + 8f, cy - 8f),
                            Offset(cx - 8f, cy + 8f),
                            1.5f
                        )
                    }

                    // Orbiting energy dot
                    if (!isLocked) {
                        val orbAngle = energyPulse + index * 1.047f
                        val dotX = cx + R * 0.88f * cos(orbAngle)
                        val dotY = cy + R * 0.88f * sin(orbAngle)
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(sealLight, Color.Transparent),
                                center = Offset(dotX, dotY),
                                radius = 6f
                            ),
                            radius = 6f, center = Offset(dotX, dotY)
                        )
                        drawCircle(Color.White.copy(alpha = orbAlpha), 2.5f, Offset(dotX, dotY))
                    }
                }

                // ── INFO PANEL ────────────────────────────────────────
                Column(modifier = Modifier.weight(1f)) {

                    // Status tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isLocked) Color(0xFFEF4444).copy(alpha = 0.5f) else sealMain.copy(
                                        alpha = orbAlpha
                                    )
                                )
                        )
                        Text(
                            if (isLocked) "SEAL CLOSED" else "AWAITING VERDICT",
                            color = if (isLocked) Color(0xFFEF4444).copy(alpha = 0.6f) else sealMain,
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        event.title,
                        color = Color.White.copy(alpha = if (isLocked) 0.35f else 1f),
                        fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )

                    event.description?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(5.dp))
                        Text(
                            it,
                            color = Color.White.copy(alpha = if (isLocked) 0.18f else 0.5f),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    if (!isLocked) {
                        // Enter seal button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(sealMain.copy(alpha = 0.1f))
                                .border(
                                    1.dp,
                                    sealMain.copy(alpha = 0.5f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                "ENTER SEAL  ⬡",
                                color = sealMain,
                                fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1A0010))
                                .border(
                                    1.dp,
                                    Color(0xFFEF4444).copy(alpha = 0.2f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                "VERDICT SEALED", color = Color(0xFFEF4444).copy(alpha = 0.45f),
                                fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// VOID STAT ORB
// =============================================================================

@Composable
private fun VoidStatOrb(number: String, label: String, color: Color, modifier: Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(500); visible = true }
    val s by animateFloatAsState(
        if (visible) 1f else 0.7f,
        spring(Spring.DampingRatioMediumBouncy),
        label = "vs"
    )
    val a by animateFloatAsState(if (visible) 1f else 0f, tween(500), label = "va")
    val inf = rememberInfiniteTransition(label = "orb_$label")
    val pulse by inf.animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(2800, easing = LinearEasing)), "op"
    )

    Box(modifier = modifier
        .scale(s)
        .alpha(a), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)) {
            val cx = size.width / 2f;
            val cy = size.height / 2f
            val r = size.minDimension / 2.2f
            drawCircle(
                color.copy(alpha = 0.08f + sin(pulse).toFloat() * 0.04f),
                r * 1.3f,
                Offset(cx, cy)
            )
            drawCircle(color.copy(alpha = 0.12f), r, Offset(cx, cy))
            drawCircle(color.copy(alpha = 0.35f), r, Offset(cx, cy), style = Stroke(0.8f))
            // Inner ring
            drawCircle(color.copy(alpha = 0.15f), r * 0.65f, Offset(cx, cy), style = Stroke(0.5f))
            // 4 corner marks
            repeat(4) { i ->
                val a2 = i * 90f * PI.toFloat() / 180f + pulse * 0.05f
                drawLine(
                    color.copy(alpha = 0.4f), Offset(cx + r * cos(a2), cy + r * sin(a2)),
                    Offset(cx + (r - 6f) * cos(a2), cy + (r - 6f) * sin(a2)), 1f
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                number,
                color = color,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )
            Text(
                label,
                color = color.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }
    }
}