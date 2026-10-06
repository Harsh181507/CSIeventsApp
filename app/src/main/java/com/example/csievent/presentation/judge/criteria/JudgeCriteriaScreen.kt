package com.example.csievent.presentation.judge.criteria

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.criteria.CriteriaResponseDto
import com.example.csievent.presentation.student.CSILoader
import com.example.csievent.presentation.student.LoaderSize
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * JUDGMENT ALTAR — Judge Criteria Screen (v2)
 *
 * UX overhaul: one single "CAST VERDICT" button at the bottom
 * submits ALL criteria scores at once. Each rune stone is purely
 * an input card — no per-card submit button.
 *
 * Flow:
 *  1. User fills score into each rune stone (arc dial updates live)
 *  2. Validation runs per-card inline (red ring + message)
 *  3. Single "CAST VERDICT" button at the bottom validates all,
 *     then submits them in one request via submitAllScores()
 *  4. Button shows loading spinner during submission
 *  5. Toast appears, button glows gold on success
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JudgeCriteriaScreen(
    eventId: Long,
    teamId: Long,
    navController: NavHostController,
    viewModel: JudgeCriteriaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(eventId) { viewModel.loadCriteriaAndScores(eventId) }

    val inf = rememberInfiniteTransition(label = "altar")
    val altarPulse by inf.animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(4000, easing = LinearEasing)), "ap"
    )
    val columnGlow by inf.animateFloat(
        0.3f, 0.7f,
        infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse), "cg"
    )
    val goldShimmer by inf.animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(3000, easing = LinearEasing)), "gsh"
    )

    var headerIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); headerIn = true }
    val headerA by animateFloatAsState(if (headerIn) 1f else 0f, tween(900), label = "ha")
    val headerY by animateFloatAsState(
        if (headerIn) 0f else 30f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "hy"
    )

    // Per-criterion score inputs — keyed by criteriaId
    // Pre-fill with existing scores when loaded
    val scoreInputs = remember { mutableStateMapOf<Long, String>() }
    val inputErrors = remember { mutableStateMapOf<Long, String?>() }

    // Pre-fill existing scores when state loads
    LaunchedEffect(state.existingScores) {
        state.existingScores.forEach { score ->
            if (score.teamId == teamId) {
                scoreInputs[score.criteriaId] = score.scoreValue.toString()
            }
        }
    }

    // Toast
    var toastData by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            toastData = it to true
            delay(1800)           // show toast briefly
            toastData = null
            viewModel.clearMessage()
            navController.popBackStack() // auto return to team screen
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            toastData = it to false
            delay(2800); toastData = null; viewModel.clearMessage()
        }
    }

    // Verdict burst — fires once on success
    var verdictBurst by remember { mutableStateOf(false) }
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            verdictBurst = true; delay(800); verdictBurst = false
        }
    }
    val burstScale by animateFloatAsState(
        if (verdictBurst) 4f else 0f,
        tween(800, easing = EaseOutExpo), label = "vbs"
    )
    val burstAlpha by animateFloatAsState(
        if (verdictBurst) 0f else 0.6f,
        tween(800), label = "vba"
    )

    val gold = Color(0xFFD4AF37)
    val goldL = Color(0xFFFFF0A0)
    val violet = Color(0xFFBB86FC)

    // How many criteria have a valid filled-in score
    val filledCount = state.criteria.count { c ->
        scoreInputs[c.id]?.toIntOrNull() != null
    }
    val allFilled = state.criteria.isNotEmpty() && filledCount == state.criteria.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF08020F), Color(0xFF010108), Color(0xFF000005)),
                    radius = 1600f
                )
            )
    ) {

        // ── ALTAR CHAMBER BACKGROUND ──────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        violet.copy(alpha = columnGlow * 0.12f),
                        Color.Transparent
                    ),
                    startX = 0f, endX = size.width * 0.15f
                ),
                size = androidx.compose.ui.geometry.Size(size.width * 0.15f, size.height)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        gold.copy(alpha = columnGlow * 0.12f),
                        Color.Transparent
                    ),
                    startX = size.width * 0.85f, endX = size.width
                ),
                topLeft = Offset(size.width * 0.85f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.15f, size.height)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        gold.copy(alpha = 0.05f + sin(altarPulse).toFloat() * 0.02f)
                    ),
                    startY = size.height * 0.75f, endY = size.height
                )
            )
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(violet.copy(alpha = 0.06f), Color.Transparent),
                    startY = 0f, endY = size.height * 0.18f
                )
            )
            // Floor rune lines
            val floorY = size.height * 0.9f
            repeat(5) { i ->
                val xf = 0.1f + i * 0.2f
                drawLine(
                    gold.copy(alpha = 0.04f + i * 0.01f),
                    Offset(size.width * xf, floorY),
                    Offset(size.width * (1f - xf), floorY + 18f), 0.5f
                )
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { pad ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .imePadding(),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 20.dp, top = 8.dp,
                    // Extra bottom padding so the last card clears the verdict button
                    bottom = 110.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── HEADER ────────────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .offset(y = headerY.dp)
                            .alpha(headerA),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(10.dp))
                        Canvas(Modifier
                            .fillMaxWidth()
                            .height(12.dp)) {
                            val cx = size.width / 2f
                            repeat(4) { i ->
                                val x1 = cx - 60f - i * 22f;
                                val x2 = cx + 60f + i * 22f
                                val a = 0.3f - i * 0.06f
                                drawLine(
                                    gold.copy(alpha = a),
                                    Offset(x1, size.height / 2f - 4f),
                                    Offset(x1, size.height / 2f + 4f),
                                    1f
                                )
                                drawLine(
                                    gold.copy(alpha = a),
                                    Offset(x2, size.height / 2f - 4f),
                                    Offset(x2, size.height / 2f + 4f),
                                    1f
                                )
                            }
                            drawLine(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        gold.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                Offset(0f, size.height / 2f),
                                Offset(size.width, size.height / 2f),
                                0.5f
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "JUDGMENT ALTAR",
                            color = gold.copy(alpha = 0.5f + sin(goldShimmer).toFloat() * 0.15f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "CAST YOUR\nVERDICT",
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1.5).sp,
                            lineHeight = 46.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Fill each rune stone, then cast all at once",
                            color = violet.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        // Overall fill progress arc
                        if (state.criteria.isNotEmpty()) {
                            Spacer(Modifier.height(20.dp))
                            val pct = filledCount.toFloat() / state.criteria.size
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(84.dp)
                            ) {
                                Canvas(Modifier.size(84.dp)) {
                                    val cx2 = size.width / 2f;
                                    val cy2 = size.height / 2f
                                    val r2 = size.minDimension / 2f - 5f
                                    drawArc(
                                        Color(0xFF0F0820),
                                        -220f,
                                        260f,
                                        false,
                                        Offset(cx2 - r2, cy2 - r2),
                                        androidx.compose.ui.geometry.Size(r2 * 2f, r2 * 2f),
                                        style = Stroke(7f, cap = StrokeCap.Round)
                                    )
                                    if (pct > 0f) drawArc(
                                        Brush.sweepGradient(
                                            listOf(violet, gold, gold),
                                            center = Offset(cx2, cy2)
                                        ),
                                        -220f,
                                        260f * pct,
                                        false,
                                        Offset(cx2 - r2, cy2 - r2),
                                        androidx.compose.ui.geometry.Size(r2 * 2f, r2 * 2f),
                                        style = Stroke(7f, cap = StrokeCap.Round)
                                    )
                                    // Burst ring on success
                                    if (burstScale > 0f) drawCircle(
                                        gold.copy(alpha = burstAlpha),
                                        r2 * burstScale, Offset(cx2, cy2), style = Stroke(2f)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "$filledCount/${state.criteria.size}", color = gold,
                                        fontSize = 16.sp, fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "FILLED",
                                        color = gold.copy(alpha = 0.4f),
                                        fontSize = 8.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }

                // ── LOADING ───────────────────────────────────────────
                if (state.isLoading) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CSILoader(LoaderSize.LARGE)
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Consulting the runes...", color = gold.copy(alpha = 0.6f),
                                    fontSize = 13.sp, letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // ── EMPTY ─────────────────────────────────────────────
                if (!state.isLoading && state.criteria.isEmpty()) {
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
                                    drawCircle(gold.copy(alpha = 0.1f), size.minDimension / 2f, c)
                                    drawCircle(
                                        gold.copy(alpha = 0.25f),
                                        size.minDimension / 2f,
                                        c,
                                        style = Stroke(1f)
                                    )
                                    repeat(3) { i ->
                                        val a = i * 120f * PI.toFloat() / 180f
                                        drawLine(
                                            gold.copy(alpha = 0.2f), c,
                                            Offset(c.x + 20f * cos(a), c.y + 20f * sin(a)), 1f
                                        )
                                    }
                                }
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    "THE ALTAR IS BARE",
                                    color = gold.copy(alpha = 0.4f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "No rune stones have been placed",
                                    color = Color.White.copy(alpha = 0.25f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // ── RUNE STONE CARDS (input only, no per-card button) ─
                itemsIndexed(state.criteria, key = { _, c -> c.id }) { index, criteria ->
                    val existingScore = state.existingScores.find {
                        it.criteriaId == criteria.id && it.teamId == teamId
                    }
                    RuneStoneInputCard(
                        criteria = criteria,
                        index = index,
                        scoreInput = scoreInputs[criteria.id] ?: "",
                        onScoreChange = { newVal ->
                            scoreInputs[criteria.id] = newVal
                            inputErrors[criteria.id] = null
                        },
                        error = inputErrors[criteria.id],
                        isAlreadyCast = existingScore != null,
                        existingScore = existingScore?.scoreValue,
                        altarPulse = altarPulse
                    )
                }
            }
        }

        // ── SINGLE "CAST VERDICT" BUTTON — pinned at bottom ──────────
        if (state.criteria.isNotEmpty() && !state.isLoading) {
            CastVerdictButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                filledCount = filledCount,
                totalCount = state.criteria.size,
                isSubmitting = state.isSubmitting,
                allFilled = allFilled,
                verdictBurst = verdictBurst,
                gold = gold,
                goldL = goldL,
                violet = violet,
                goldShimmer = goldShimmer,
                onCast = {
                    // Validate all inputs first
                    var hasError = false
                    state.criteria.forEach { c ->
                        val raw = scoreInputs[c.id]?.trim()
                        val parsed = raw?.toIntOrNull()
                        when {
                            parsed == null -> {
                                inputErrors[c.id] = "Required"; hasError = true
                            }

                            parsed < 0 -> {
                                inputErrors[c.id] = "Cannot be negative"; hasError = true
                            }

                            parsed > c.maxScore -> {
                                inputErrors[c.id] = "Max is ${c.maxScore}"; hasError = true
                            }
                        }
                    }
                    if (!hasError) {
                        val scores = state.criteria.associate { c ->
                            c.id to (scoreInputs[c.id]!!.trim().toInt())
                        }
                        viewModel.submitAllScores(teamId, scores, eventId)
                    }
                }
            )
        }

        // ── FLOATING TOAST ────────────────────────────────────────────
        AnimatedVisibility(
            visible = toastData != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp, start = 20.dp, end = 20.dp)
        ) {
            toastData?.let { (msg, isSuccess) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSuccess) Color(0xFF060F08) else Color(0xFF130005))
                        .border(
                            1.dp,
                            if (isSuccess) gold.copy(alpha = 0.6f) else Color(0xFFEF4444).copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Canvas(Modifier.size(28.dp)) {
                            val c = Offset(size.width / 2f, size.height / 2f)
                            val col = if (isSuccess) gold else Color(0xFFEF4444)
                            drawCircle(col.copy(alpha = 0.15f), size.minDimension / 2f, c)
                            drawCircle(
                                col.copy(alpha = 0.5f),
                                size.minDimension / 2f,
                                c,
                                style = Stroke(1f)
                            )
                            if (isSuccess) {
                                drawLine(
                                    col,
                                    Offset(c.x - 6f, c.y),
                                    Offset(c.x - 2f, c.y + 5f),
                                    1.5f,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    col,
                                    Offset(c.x - 2f, c.y + 5f),
                                    Offset(c.x + 7f, c.y - 5f),
                                    1.5f,
                                    cap = StrokeCap.Round
                                )
                            } else {
                                drawLine(
                                    col,
                                    Offset(c.x - 5f, c.y - 5f),
                                    Offset(c.x + 5f, c.y + 5f),
                                    1.5f,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    col,
                                    Offset(c.x + 5f, c.y - 5f),
                                    Offset(c.x - 5f, c.y + 5f),
                                    1.5f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        Text(
                            msg,
                            color = if (isSuccess) gold else Color(0xFFFCA5A5),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// CAST VERDICT BUTTON — the single bottom action
// =============================================================================

@Composable
private fun CastVerdictButton(
    modifier: Modifier,
    filledCount: Int,
    totalCount: Int,
    isSubmitting: Boolean,
    allFilled: Boolean,
    verdictBurst: Boolean,
    gold: Color,
    goldL: Color,
    violet: Color,
    goldShimmer: Float,
    onCast: () -> Unit
) {
    val inf = rememberInfiniteTransition(label = "vbtn")
    val btnPulse by inf.animateFloat(
        0.97f, 1f,
        infiniteRepeatable(tween(1600, easing = EaseInOutSine), RepeatMode.Reverse), "bp"
    )
    val runeRot by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(12000, easing = LinearEasing)), "rr"
    )

    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        if (pressed) 0.96f else if (allFilled) btnPulse else 1f,
        spring(stiffness = Spring.StiffnessHigh), label = "ps"
    )

    // Burst ring
    val burstScale by animateFloatAsState(
        if (verdictBurst) 3.5f else 0f,
        tween(700, easing = EaseOutExpo), label = "vbs"
    )
    val burstAlpha by animateFloatAsState(if (verdictBurst) 0f else 0.7f, tween(700), label = "vba")

    val activeColor = if (allFilled) gold else Color.White.copy(alpha = 0.2f)
    val activeBorder = if (allFilled) gold.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.08f)
    val activeBg1 = if (allFilled) Color(0xFF1A1000) else Color(0xFF0A080F)
    val activeBg2 = if (allFilled) Color(0xFF0D0A00) else Color(0xFF07060C)

    Box(modifier = modifier
        .fillMaxWidth()
        .scale(pressScale)) {
        // Outer glow when all filled
        if (allFilled) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)) {
                drawRect(
                    brush = Brush.radialGradient(
                        listOf(
                            gold.copy(alpha = 0.12f + sin(goldShimmer).toFloat() * 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.width * 0.6f
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(activeBg1, activeBg2, activeBg1)))
                .border(
                    1.dp,
                    if (allFilled)
                        Brush.horizontalGradient(
                            listOf(
                                gold.copy(alpha = 0.4f),
                                goldL.copy(alpha = 0.8f),
                                gold.copy(alpha = 0.4f)
                            )
                        )
                    else
                        Brush.horizontalGradient(listOf(activeBorder, activeBorder)),
                    RoundedCornerShape(18.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                        onTap = { if (!isSubmitting) onCast() }
                    )
                }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            // Decorative rune ring behind text (only when all filled)
            if (allFilled) {
                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)) {
                    val cx = size.width / 2f;
                    val cy = size.height / 2f
                    rotate(runeRot, Offset(cx, cy)) {
                        repeat(8) { i ->
                            val a = i * 45f * PI.toFloat() / 180f
                            val r = 22f
                            drawLine(
                                gold.copy(alpha = 0.15f),
                                Offset(cx + r * cos(a), cy + r * sin(a)),
                                Offset(cx + (r + 6f) * cos(a), cy + (r + 6f) * sin(a)), 1f
                            )
                        }
                        drawCircle(
                            gold.copy(alpha = 0.08f),
                            22f,
                            Offset(cx, cy),
                            style = Stroke(0.5f)
                        )
                    }
                }
            }

            // Burst ring
            if (burstScale > 0f) {
                Canvas(modifier = Modifier.size(58.dp)) {
                    drawCircle(
                        gold.copy(alpha = burstAlpha),
                        size.minDimension / 2f * burstScale,
                        Offset(size.width / 2f, size.height / 2f), style = Stroke(2f)
                    )
                }
            }

            AnimatedContent(
                targetState = isSubmitting,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "vbtn_content"
            ) { submitting ->
                if (submitting) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CSILoader(LoaderSize.SMALL)
                        Text(
                            "CASTING VERDICTS...", color = gold, fontSize = 13.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            if (allFilled) "⬡  CAST VERDICT  ⬡" else "FILL ALL RUNE STONES",
                            color = activeColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        if (!allFilled) {
                            Text(
                                "$filledCount of $totalCount rune stones filled",
                                color = Color.White.copy(alpha = 0.25f),
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// RUNE STONE INPUT CARD — no submit button, pure input
// =============================================================================

@Composable
private fun RuneStoneInputCard(
    criteria: CriteriaResponseDto,
    index: Int,
    scoreInput: String,
    onScoreChange: (String) -> Unit,
    error: String?,
    isAlreadyCast: Boolean,
    existingScore: Int?,
    altarPulse: Float
) {
    val inf = rememberInfiniteTransition(label = "rs_$index")
    val runeColors = listOf(
        Triple(Color(0xFFD4AF37), Color(0xFFFFF0A0), "AURUM"),
        Triple(Color(0xFF00E5FF), Color(0xFF80EEFF), "AQUA"),
        Triple(Color(0xFFBB86FC), Color(0xFFDDB8FF), "ARCANUM"),
        Triple(Color(0xFF69FF89), Color(0xFFB0FFC0), "VIRIDIS"),
        Triple(Color(0xFFFF8A80), Color(0xFFFFBBB0), "IGNIS"),
        Triple(Color(0xFF82B1FF), Color(0xFFBDD0FF), "CAELUM"),
    )
    val (runeCore, runeLight, runeName) = runeColors[index % runeColors.size]

    val stoneGlow by inf.animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(3500 + index * 400, easing = LinearEasing)), "sg"
    )
    val runeFlicker by inf.animateFloat(
        0.88f, 1f,
        infiniteRepeatable(tween(120 + index * 50, easing = LinearEasing), RepeatMode.Reverse), "rf"
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 100L + 200L); visible = true }
    val entranceY by animateFloatAsState(
        if (visible) 0f else 40f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "ey$index"
    )
    val entranceA by animateFloatAsState(if (visible) 1f else 0f, tween(450), label = "ea$index")

    // Arc dial fill
    val scoreFill = remember(scoreInput, criteria.maxScore) {
        (scoreInput.toIntOrNull() ?: 0).toFloat() / criteria.maxScore.toFloat()
    }.coerceIn(0f, 1f)
    val animatedFill by animateFloatAsState(scoreFill, tween(350), label = "sf$index")

    val hasError = error != null
    val borderColor = when {
        hasError -> Color(0xFFEF4444)
        isAlreadyCast -> runeCore
        else -> runeCore.copy(alpha = 0.45f * runeFlicker)
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .offset(y = entranceY.dp)
        .alpha(entranceA)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF06040F), runeCore.copy(alpha = 0.03f), Color(0xFF03020A)
                        )
                    )
                )
                .border(
                    1.dp, Brush.linearGradient(
                        listOf(
                            borderColor.copy(alpha = if (hasError) 0.8f else borderColor.alpha),
                            borderColor.copy(alpha = 0.06f),
                            borderColor.copy(alpha = if (hasError) 0.5f else borderColor.alpha * 0.6f)
                        )
                    ), RoundedCornerShape(22.dp)
                )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {

                // ── HEADER ROW: arc dial + info ───────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Arc dial
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
                        Canvas(Modifier.size(76.dp)) {
                            val cx = size.width / 2f;
                            val cy = size.height / 2f
                            val r = size.minDimension / 2f - 4f
                            // Glow aura
                            drawCircle(
                                runeCore.copy(alpha = (0.07f + sin(stoneGlow).toFloat() * 0.04f)),
                                r * 1.3f,
                                Offset(cx, cy)
                            )
                            // Track
                            drawArc(
                                Color(0xFF0F0820),
                                -220f,
                                260f,
                                false,
                                Offset(cx - r, cy - r),
                                androidx.compose.ui.geometry.Size(r * 2f, r * 2f),
                                style = Stroke(7f, cap = StrokeCap.Round)
                            )
                            // Fill
                            if (animatedFill > 0f) drawArc(
                                Brush.sweepGradient(
                                    listOf(
                                        runeCore.copy(alpha = 0.6f),
                                        runeLight,
                                        runeCore
                                    ), center = Offset(cx, cy)
                                ),
                                -220f,
                                260f * animatedFill,
                                false,
                                Offset(cx - r, cy - r),
                                androidx.compose.ui.geometry.Size(r * 2f, r * 2f),
                                style = Stroke(7f, cap = StrokeCap.Round)
                            )
                            // Rune tick marks
                            repeat(6) { i ->
                                val a = (-220f + 260f * i / 5f) * PI.toFloat() / 180f
                                val isFilled = (i.toFloat() / 5f) <= animatedFill
                                drawLine(
                                    runeCore.copy(alpha = if (isFilled) 0.6f else 0.15f),
                                    Offset(cx + (r - 10f) * cos(a), cy + (r - 10f) * sin(a)),
                                    Offset(cx + (r - 14f) * cos(a), cy + (r - 14f) * sin(a)), 1.5f
                                )
                            }
                            // Center circle
                            drawCircle(
                                runeCore.copy(alpha = 0.08f + animatedFill * 0.15f),
                                r * 0.5f,
                                Offset(cx, cy)
                            )
                            drawCircle(
                                runeCore.copy(alpha = 0.3f),
                                r * 0.5f,
                                Offset(cx, cy),
                                style = Stroke(0.5f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                scoreInput.ifEmpty { "—" },
                                color = if (scoreInput.isEmpty()) runeCore.copy(alpha = 0.3f) else runeCore,
                                fontSize = 16.sp, fontWeight = FontWeight.Bold
                            )
                            Text(
                                "/ ${criteria.maxScore}", color = runeCore.copy(alpha = 0.35f),
                                fontSize = 8.sp, fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Criterion info
                    Column(modifier = Modifier.weight(1f)) {
                        // Badge row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(runeCore.copy(alpha = 0.1f))
                                    .border(
                                        0.5.dp,
                                        runeCore.copy(alpha = 0.35f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    runeName,
                                    color = runeCore,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                            if (isAlreadyCast) {
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                        .border(
                                            0.5.dp,
                                            Color(0xFF10B981).copy(alpha = 0.4f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "CAST",
                                        color = Color(0xFF10B981),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            criteria.title,
                            color = Color.White.copy(alpha = runeFlicker),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp
                        )
                        Spacer(Modifier.height(6.dp))
                        // Segment fill bar
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            val segs = minOf(criteria.maxScore, 10)
                            val filled = ((scoreInput.toIntOrNull()
                                ?: 0).toFloat() / criteria.maxScore * segs).toInt()
                            repeat(segs) { i ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (i < filled) runeCore.copy(alpha = 0.85f) else runeCore.copy(
                                                alpha = 0.1f
                                            )
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── SCORE TEXT INPUT ──────────────────────────────────
                OutlinedTextField(
                    value = scoreInput,
                    onValueChange = { if (it.length <= 4) onScoreChange(it) },
                    label = {
                        Text(
                            if (isAlreadyCast) "Update verdict  (current: $existingScore)"
                            else "Inscribe score  (0 – ${criteria.maxScore})",
                            color = if (hasError) Color(0xFFEF4444).copy(alpha = 0.8f) else runeCore.copy(
                                alpha = 0.7f
                            ),
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    isError = hasError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = if (hasError) Color(0xFFEF4444) else runeCore,
                        unfocusedBorderColor = if (hasError) Color(0xFFEF4444).copy(alpha = 0.5f) else runeCore.copy(
                            alpha = 0.22f
                        ),
                        errorBorderColor = Color(0xFFEF4444),
                        focusedLabelColor = if (hasError) Color(0xFFEF4444) else runeCore,
                        cursorColor = runeCore,
                        focusedContainerColor = runeCore.copy(alpha = 0.04f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Inline validation error
                AnimatedVisibility(visible = hasError) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)))
                        Text(error ?: "", color = Color(0xFFFCA5A5), fontSize = 11.sp)
                    }
                }
            }
        }

        // Corner sigil marks
        Canvas(modifier = Modifier.matchParentSize()) {
            val s = 14f;
            val w = 1.2f
            val c = if (hasError) Color(0xFFEF4444).copy(alpha = 0.5f)
            else runeCore.copy(alpha = if (isAlreadyCast) 0.65f else 0.38f)
            drawLine(c, Offset(0f, s), Offset(0f, 0f), w); drawLine(
            c,
            Offset(0f, 0f),
            Offset(s, 0f),
            w
        )
            drawLine(c, Offset(size.width - s, 0f), Offset(size.width, 0f), w); drawLine(
            c,
            Offset(size.width, 0f),
            Offset(size.width, s),
            w
        )
            drawLine(c, Offset(0f, size.height - s), Offset(0f, size.height), w); drawLine(
            c,
            Offset(0f, size.height),
            Offset(s, size.height),
            w
        )
            drawLine(
                c,
                Offset(size.width - s, size.height),
                Offset(size.width, size.height),
                w
            ); drawLine(c, Offset(size.width, size.height - s), Offset(size.width, size.height), w)
        }
    }
}