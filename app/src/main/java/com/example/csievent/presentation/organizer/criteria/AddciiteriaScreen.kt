package com.example.csievent.presentation.organizer.criteria

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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ── gem colours — each criterion gets a unique crystal colour ─────────────────
private val GEM_COLORS = listOf(
    Color(0xFF9B6DFF), Color(0xFF06B6D4), Color(0xFF10B981),
    Color(0xFFD4AF37), Color(0xFFEC4899), Color(0xFFFF8C00),
    Color(0xFF4B3CC8), Color(0xFF00D4FF)
)

private val BG_STARS = listOf(
    0.05f to 0.03f, 0.91f to 0.07f, 0.42f to 0.02f, 0.16f to 0.14f, 0.75f to 0.10f,
    0.03f to 0.28f, 0.96f to 0.22f, 0.30f to 0.37f, 0.82f to 0.43f, 0.09f to 0.52f,
    0.67f to 0.57f, 0.53f to 0.80f, 0.22f to 0.74f, 0.87f to 0.69f, 0.47f to 0.90f,
    0.70f to 0.87f, 0.34f to 0.63f, 0.61f to 0.21f, 0.49f to 0.46f, 0.24f to 0.34f
)

// =============================================================================
// SCREEN
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCriteriaScreen(
    eventId: Long,
    navController: NavHostController,
    viewModel: AddCriteriaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(eventId) { viewModel.loadCriteria(eventId) }

    // form
    var title by remember { mutableStateOf("") }
    var maxScore by remember { mutableStateOf("") }

    val titleFilled = title.isNotBlank()
    val maxScoreFilled =
        maxScore.isNotBlank() && maxScore.toIntOrNull() != null && (maxScore.toIntOrNull() ?: 0) > 0
    val canAdd = titleFilled && maxScoreFilled && !state.isSubmitting

    // flash toast
    var flashSuccess by remember { mutableStateOf(false) }
    var flashError by remember { mutableStateOf(false) }
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            flashSuccess = true; delay(2500); flashSuccess = false
        }
    }
    LaunchedEffect(state.error) {
        if (state.error != null) {
            flashError = true; delay(2500); flashError = false
        }
    }

    // ── infinite animations ───────────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "ac")
    val twinkle by inf.animateFloat(
        0f, (2f * PI).toFloat(),
        infiniteRepeatable(tween(9000, easing = LinearEasing)), "tw"
    )
    val nebDrift by inf.animateFloat(
        0f, 20f,
        infiniteRepeatable(tween(13000, easing = EaseInOutSine), RepeatMode.Reverse), "nd"
    )
    val outerRing by inf.animateFloat(
        360f, 0f,
        infiniteRepeatable(tween(22000, easing = LinearEasing)), "or"
    )
    val innerRing by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(13000, easing = LinearEasing)), "ir"
    )
    val forgePulse by inf.animateFloat(
        0.9f, 1.1f,
        infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), "fp"
    )
    val forgeRot by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(7000, easing = LinearEasing)), "fr"
    )
    val scanLine by inf.animateFloat(
        -0.05f, 1.05f,
        infiniteRepeatable(tween(5000, easing = LinearEasing)), "sl"
    )
    val particleT by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(4000, easing = LinearEasing)), "pt"
    )

    // header
    var headerIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); headerIn = true }
    val headerA by animateFloatAsState(if (headerIn) 1f else 0f, tween(700), label = "ha")
    val headerY by animateFloatAsState(
        if (headerIn) 0f else -40f,
        spring(0.65f, Spring.StiffnessMediumLow), label = "hy"
    )

    // form slide
    var formIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(300); formIn = true }
    val formA by animateFloatAsState(if (formIn) 1f else 0f, tween(600), label = "fa")
    val formY by animateFloatAsState(
        if (formIn) 0f else 40f,
        spring(0.65f, Spring.StiffnessMediumLow), label = "fy"
    )

    // forge colour — shifts based on what's typed
    val forgeColor = when {
        canAdd -> Color(0xFF10B981)
        titleFilled -> Color(0xFF06B6D4)
        else -> Color(0xFF4B3CC8)
    }

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
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF1A0A50).copy(alpha = 0.5f), Color.Transparent),
                    radius = 420f,
                    center = Offset(size.width * 0.82f + nebDrift, size.height * 0.18f)
                ),
                radius = 420f, center = Offset(size.width * 0.82f + nebDrift, size.height * 0.18f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF041830).copy(alpha = 0.4f), Color.Transparent),
                    radius = 340f, center = Offset(size.width * 0.1f, size.height * 0.72f)
                ),
                radius = 340f, center = Offset(size.width * 0.1f, size.height * 0.72f)
            )
            BG_STARS.forEachIndexed { i, (x, y) ->
                val tw = (sin(twinkle + i * 0.58f) * 0.3f + 0.7f).toFloat()
                val r = when (i % 5) {
                    0 -> 2.2f; 1 -> 1.6f; else -> 1.0f
                }
                drawCircle(
                    Color.White.copy(alpha = tw * 0.6f),
                    r,
                    Offset(size.width * x, size.height * y)
                )
            }
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
                    startY = sp - 24f, endY = sp + 24f
                ), size = size
            )
        }

        // ── TOAST ─────────────────────────────────────────────────────
        AnimatedVisibility(
            flashSuccess,
            enter = fadeIn() + slideInVertically { -40 },
            exit = fadeOut() + slideOutVertically { -40 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp)
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A2218))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)))
                    Text(
                        state.successMessage ?: "", color = Color(0xFF10B981), fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
        AnimatedVisibility(
            flashError,
            enter = fadeIn() + slideInVertically { -40 },
            exit = fadeOut() + slideOutVertically { -40 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp)
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A0808))
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444)))
                    Text(
                        state.error ?: "", color = Color(0xFFEF4444), fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "CRYSTAL FORGE", color = Color.White, fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "Add judging criteria",
                                color = Color(0xFF8C83E4).copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color(0xFF8C83E4))
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
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {

                // ── FORGE HERO ────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .offset(y = headerY.dp)
                            .alpha(headerA),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(Modifier
                            .fillMaxWidth()
                            .height(220.dp)) {
                            val cx = size.width / 2f;
                            val cy = size.height / 2f
                            val fR = 44.dp.toPx()

                            // Outer glow
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(
                                        forgeColor.copy(alpha = 0.18f * forgePulse),
                                        Color.Transparent
                                    ),
                                    center = Offset(cx, cy), radius = fR * 3.5f
                                ),
                                radius = fR * 3.5f, center = Offset(cx, cy)
                            )

                            // Outer ring — dashed, slow CCW
                            rotate(outerRing, Offset(cx, cy)) {
                                drawCircle(
                                    forgeColor.copy(alpha = 0.35f), fR * 2.2f, Offset(cx, cy),
                                    style = Stroke(
                                        1f,
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(
                                                18f,
                                                9f
                                            )
                                        )
                                    )
                                )
                                // 4 corner dots
                                for (i in 0..3) {
                                    val a = i * PI.toFloat() / 2f
                                    drawCircle(
                                        forgeColor.copy(alpha = 0.65f), 3.5f,
                                        Offset(cx + fR * 2.2f * cos(a), cy + fR * 2.2f * sin(a))
                                    )
                                }
                            }
                            // Inner ring — solid CW
                            rotate(innerRing, Offset(cx, cy)) {
                                drawCircle(
                                    Color(0xFF7C3AED).copy(alpha = 0.5f),
                                    fR * 1.55f,
                                    Offset(cx, cy),
                                    style = Stroke(1.3f)
                                )
                                drawCircle(
                                    Color(0xFF9B6DFF).copy(alpha = 0.75f),
                                    3f,
                                    Offset(cx + fR * 1.55f, cy)
                                )
                            }
                            // Orbiting criteria gems preview
                            val gemCount = state.criteria.size.coerceAtMost(8)
                            for (i in 0 until gemCount) {
                                val t = (particleT + i.toFloat() / gemCount.coerceAtLeast(1)) % 1f
                                val a = t * 2f * PI.toFloat()
                                val gR = fR * 1.85f
                                val gx = cx + gR * cos(a);
                                val gy = cy + gR * 0.45f * sin(a)
                                val gemCol = GEM_COLORS[i % GEM_COLORS.size]
                                // Gem diamond shape
                                val gs = 5f
                                val gemPath = Path().apply {
                                    moveTo(gx, gy - gs); lineTo(gx + gs * 0.6f, gy); lineTo(
                                    gx,
                                    gy + gs * 0.7f
                                ); lineTo(gx - gs * 0.6f, gy); close()
                                }
                                drawPath(gemPath, gemCol)
                                drawPath(
                                    gemPath,
                                    Color.White.copy(alpha = 0.3f),
                                    style = Stroke(0.5f)
                                )
                                drawCircle(gemCol.copy(alpha = 0.3f), 9f, Offset(gx, gy))
                            }

                            // Rotating forge facets inside core
                            rotate(forgeRot, Offset(cx, cy)) {
                                for (facet in 0..5) {
                                    val a = facet * PI.toFloat() / 3f
                                    drawLine(
                                        forgeColor.copy(alpha = 0.3f),
                                        Offset(cx, cy),
                                        Offset(cx + fR * 0.75f * cos(a), cy + fR * 0.75f * sin(a)),
                                        1f
                                    )
                                }
                            }

                            // Forge core body — hexagonal feel with drawPath
                            val hexPath = Path()
                            for (h in 0..5) {
                                val a = h * PI.toFloat() / 3f - PI.toFloat() / 6f
                                val px = cx + fR * 0.92f * cos(a);
                                val py = cy + fR * 0.92f * sin(a)
                                if (h == 0) hexPath.moveTo(px, py) else hexPath.lineTo(px, py)
                            }
                            hexPath.close()
                            drawPath(
                                hexPath, brush = Brush.radialGradient(
                                    listOf(
                                        forgeColor.copy(alpha = 0.8f),
                                        Color(0xFF2A1A60),
                                        Color(0xFF060212)
                                    ),
                                    center = Offset(cx - fR * 0.15f, cy - fR * 0.18f),
                                    radius = fR * 1.4f
                                )
                            )
                            drawPath(
                                hexPath,
                                forgeColor.copy(alpha = 0.65f * forgePulse),
                                style = Stroke(1.8f)
                            )

                            // Specular
                            drawCircle(
                                Color.White.copy(alpha = 0.3f), fR * 0.22f,
                                Offset(cx - fR * 0.28f, cy - fR * 0.32f)
                            )

                            // Criteria count arc
                            val total = state.criteria.size
                            if (total > 0) {
                                val pct = (total.toFloat() / 10f).coerceAtMost(1f)
                                drawCircle(
                                    Color(0xFF1A1040),
                                    fR * 1.12f,
                                    Offset(cx, cy),
                                    style = Stroke(3f)
                                )
                                drawArc(
                                    forgeColor.copy(alpha = 0.85f),
                                    -90f,
                                    pct * 360f,
                                    false,
                                    Offset(cx - fR * 1.12f, cy - fR * 1.12f),
                                    Size(fR * 2.24f, fR * 2.24f),
                                    style = Stroke(3f, cap = StrokeCap.Round)
                                )
                            }
                        }

                        // Centre text
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${state.criteria.size}",
                                color = Color.White.copy(alpha = 0.9f), fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                            )
                            Text(
                                if (state.criteria.size == 1) "CRYSTAL" else "CRYSTALS",
                                color = forgeColor.copy(alpha = 0.7f), fontSize = 8.sp,
                                fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }

                // ── ADD FORM ──────────────────────────────────────────
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .offset(y = formY.dp)
                            .alpha(formA)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF09091A), Color(0xFF060510))
                                    )
                                )
                                .border(
                                    1.dp, Brush.linearGradient(
                                        listOf(
                                            forgeColor.copy(alpha = 0.5f),
                                            forgeColor.copy(alpha = 0.15f),
                                            forgeColor.copy(alpha = 0.5f)
                                        )
                                    ),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                // Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(forgeColor.copy(alpha = 0.7f))
                                    )
                                    Text(
                                        "FORGE NEW CRYSTAL",
                                        color = Color(0xFF8C83E4).copy(alpha = 0.55f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .height(1.dp)
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        forgeColor.copy(alpha = 0.3f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                    )
                                }

                                // Criterion title field
                                AcField(
                                    title,
                                    { title = it },
                                    "CRITERION TITLE",
                                    "e.g. Presentation, Innovation...",
                                    titleFilled,
                                    Color(0xFF4B3CC8),
                                    KeyboardType.Text,
                                    KeyboardCapitalization.Words
                                )

                                // Max score field
                                AcField(
                                    maxScore,
                                    { maxScore = it.filter { c -> c.isDigit() } },
                                    "MAX SCORE",
                                    "e.g. 10, 25, 100",
                                    maxScoreFilled,
                                    Color(0xFF7C3AED),
                                    KeyboardType.Number,
                                    KeyboardCapitalization.None
                                )

                                // Validation hint
                                AnimatedVisibility(maxScore.isNotBlank() && !maxScoreFilled) {
                                    Text(
                                        "Enter a positive number",
                                        color = Color(0xFFEF4444).copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                // Add button
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (canAdd)
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color(0xFF1A0A50),
                                                        Color(0xFF2A1270)
                                                    )
                                                )
                                            else Brush.linearGradient(
                                                listOf(
                                                    Color(0xFF0A0A16),
                                                    Color(0xFF0A0A16)
                                                )
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            if (canAdd) Brush.linearGradient(
                                                listOf(
                                                    forgeColor.copy(
                                                        alpha = 0.7f
                                                    ),
                                                    Color(0xFF7C3AED).copy(alpha = 0.35f),
                                                    forgeColor.copy(alpha = 0.7f)
                                                )
                                            )
                                            else Brush.linearGradient(
                                                listOf(
                                                    Color(0xFF181828),
                                                    Color(0xFF181828)
                                                )
                                            ),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable(enabled = canAdd) {
                                            viewModel.addCriteria(eventId, title, maxScore.toInt())
                                            title = ""; maxScore = ""
                                        }
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedContent(
                                        state.isSubmitting,
                                        transitionSpec = {
                                            fadeIn(tween(200)) togetherWith fadeOut(
                                                tween(150)
                                            )
                                        },
                                        label = "add_btn"
                                    ) { submitting ->
                                        if (submitting) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                CSILoader(size = LoaderSize.SMALL)
                                                Text(
                                                    "FORGING CRYSTAL",
                                                    color = Color(0xFF8B6DFF),
                                                    fontSize = 13.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                // Diamond shape indicator
                                                Canvas(Modifier.size(16.dp)) {
                                                    val c = size.minDimension / 2f
                                                    val col =
                                                        if (canAdd) forgeColor else Color(0xFF252535)
                                                    val dPath = Path().apply {
                                                        moveTo(
                                                            c,
                                                            0f
                                                        ); lineTo(size.width, c); lineTo(
                                                        c,
                                                        size.height
                                                    ); lineTo(0f, c); close()
                                                    }
                                                    drawPath(dPath, col)
                                                }
                                                Text(
                                                    if (canAdd) "ADD CRITERION" else "FILL BOTH FIELDS",
                                                    color = if (canAdd) Color.White else Color(
                                                        0xFF252535
                                                    ),
                                                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // HUD brackets
                        Canvas(Modifier.matchParentSize()) {
                            val s = 14f;
                            val w = 1.3f;
                            val c = forgeColor.copy(alpha = 0.4f)
                            drawLine(c, Offset(0f, s), Offset(0f, 0f), w); drawLine(
                            c,
                            Offset(0f, 0f),
                            Offset(s, 0f),
                            w
                        )
                            drawLine(
                                c,
                                Offset(size.width - s, 0f),
                                Offset(size.width, 0f),
                                w
                            ); drawLine(c, Offset(size.width, 0f), Offset(size.width, s), w)
                            drawLine(
                                c,
                                Offset(0f, size.height - s),
                                Offset(0f, size.height),
                                w
                            ); drawLine(c, Offset(0f, size.height), Offset(s, size.height), w)
                            drawLine(
                                c,
                                Offset(size.width - s, size.height),
                                Offset(size.width, size.height),
                                w
                            ); drawLine(
                            c,
                            Offset(size.width, size.height - s),
                            Offset(size.width, size.height),
                            w
                        )
                        }
                    }
                }

                item { Spacer(Modifier.height(20.dp)) }

                // ── CRITERIA LIST ─────────────────────────────────────
                if (!state.isLoading && state.criteria.isNotEmpty()) {
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                Modifier
                                    .width(3.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF4B3CC8))
                            )
                            Text(
                                "FORGED CRYSTALS", color = Color(0xFF8C83E4).copy(alpha = 0.55f),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                            )
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF4B3CC8).copy(alpha = 0.12f))
                                    .border(
                                        0.5.dp,
                                        Color(0xFF4B3CC8).copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "${state.criteria.size}",
                                    color = Color(0xFF4B3CC8),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF4B3CC8).copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    }
                    itemsIndexed(state.criteria, key = { _, c -> c.id }) { idx, criterion ->
                        Box(Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 10.dp)) {
                            CrystalCard(
                                criterion, idx,
                                onDelete = { viewModel.deleteCriteria(criterion.id) })
                        }
                    }
                }

                // Loading
                if (state.isLoading) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CSILoader(size = LoaderSize.MEDIUM)
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Loading crystals...",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Empty
                if (!state.isLoading && state.criteria.isEmpty()) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Canvas(Modifier.size(48.dp)) {
                                    val c = size.minDimension / 2f
                                    val dp = Path().apply {
                                        moveTo(c, 0f); lineTo(size.width, c); lineTo(
                                        c,
                                        size.height
                                    ); lineTo(0f, c); close()
                                    }
                                    drawPath(dp, Color(0xFF4B3CC8).copy(alpha = 0.15f))
                                    drawPath(
                                        dp,
                                        Color(0xFF4B3CC8).copy(alpha = 0.4f),
                                        style = Stroke(1.5f)
                                    )
                                }
                                Text(
                                    "No crystals forged yet", color = Color.White, fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Add judging criteria above",
                                    color = Color(0xFF8C83E4).copy(alpha = 0.5f), fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
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
// CRYSTAL CARD
// =============================================================================
@Composable
private fun CrystalCard(
    criterion: CriteriaResponseDto,
    index: Int,
    onDelete: () -> Unit
) {
    val gemCol = GEM_COLORS[index % GEM_COLORS.size]

    var vis by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 80L + 200L); vis = true }
    val entX by animateFloatAsState(
        if (vis) 0f else -100f,
        spring(0.65f, Spring.StiffnessMediumLow), label = "ex"
    )
    val entA by animateFloatAsState(if (vis) 1f else 0f, tween(350), label = "ea")

    // delete confirm
    var confirmDelete by remember { mutableStateOf(false) }

    val inf = rememberInfiniteTransition(label = "cc$index")
    val scanC by inf.animateFloat(
        -0.1f, 1.1f,
        infiniteRepeatable(tween(3800 + index * 180, easing = LinearEasing)), "sc"
    )
    val gemPulse by inf.animateFloat(
        0.85f,
        1f,
        infiniteRepeatable(tween(1800 + index * 200, easing = EaseInOutSine), RepeatMode.Reverse),
        "gp"
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
                            gemCol.copy(alpha = 0.07f),
                            Color(0xFF04040E)
                        )
                    )
                )
                .border(
                    1.dp, Brush.linearGradient(
                        listOf(
                            gemCol.copy(alpha = 0.5f),
                            gemCol.copy(alpha = 0.12f),
                            gemCol.copy(alpha = 0.5f)
                        )
                    ),
                    RoundedCornerShape(16.dp)
                )
        ) {
            // scan line
            Canvas(Modifier
                .fillMaxWidth()
                .height(70.dp)) {
                val sy = size.height * scanC
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            gemCol.copy(alpha = 0.04f),
                            gemCol.copy(alpha = 0.07f),
                            gemCol.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        startY = sy - 12f, endY = sy + 12f
                    ), size = size
                )
            }

            Row(
                Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Crystal gem shape
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(44.dp)) {
                        val c = size.minDimension / 2f
                        // Outer glow
                        drawCircle(gemCol.copy(alpha = 0.2f * gemPulse), c)
                        // Diamond
                        val gPath = Path().apply {
                            moveTo(c, c * 0.2f); lineTo(c * 1.6f, c); lineTo(
                            c,
                            c * 1.75f
                        ); lineTo(c * 0.4f, c); close()
                        }
                        drawPath(gPath, gemCol)
                        // Specular line
                        drawLine(
                            Color.White.copy(alpha = 0.4f),
                            Offset(c * 0.7f, c * 0.45f),
                            Offset(c * 0.9f, c * 0.35f),
                            1f
                        )
                        drawPath(gPath, Color.White.copy(alpha = 0.15f), style = Stroke(0.8f))
                        // Index
                    }
                    Text(
                        "%02d".format(index + 1),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Content
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        criterion.title, color = Color.White, fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(gemCol.copy(alpha = 0.12f))
                                .border(
                                    0.5.dp,
                                    gemCol.copy(alpha = 0.35f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "MAX ${criterion.maxScore} PTS", color = gemCol, fontSize = 9.sp,
                                fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Delete
                AnimatedContent(
                    confirmDelete,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(130)) },
                    label = "del"
                ) { confirm ->
                    if (confirm) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.18f))
                                    .border(
                                        1.dp,
                                        Color(0xFFEF4444).copy(alpha = 0.6f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onDelete(); confirmDelete = false }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    "DELETE",
                                    color = Color(0xFFEF4444),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                "CONFIRM",
                                color = Color(0xFFEF4444).copy(alpha = 0.4f),
                                fontSize = 7.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { confirmDelete = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = Color(0xFF8C83E4).copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        // HUD brackets
        Canvas(Modifier.matchParentSize()) {
            val s = 10f;
            val w = 1f;
            val c = gemCol.copy(alpha = 0.38f)
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

// =============================================================================
// FIELD
// =============================================================================
@Composable
private fun AcField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    active: Boolean,
    accent: Color,
    keyboardType: KeyboardType,
    capitalization: KeyboardCapitalization
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (active) accent else Color(0xFF252535))
            )
            Text(
                label,
                color = if (active) accent.copy(alpha = 0.75f) else Color(0xFF252535),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Box(
                Modifier
                    .width(10.dp)
                    .height(1.dp)
                    .background(if (active) accent.copy(alpha = 0.3f) else Color(0xFF1A1A28))
            )
            Text(
                if (active) "● SET" else "○ EMPTY",
                color = if (active) accent.copy(alpha = 0.5f) else Color(0xFF252535),
                fontSize = 8.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp
            )
        }
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF252535), fontSize = 14.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = capitalization
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = accent,
                unfocusedBorderColor = accent.copy(alpha = 0.25f),
                focusedContainerColor = Color(0xFF06060E),
                unfocusedContainerColor = Color(0xFF04040C),
                focusedLabelColor = accent,
                unfocusedLabelColor = accent.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}