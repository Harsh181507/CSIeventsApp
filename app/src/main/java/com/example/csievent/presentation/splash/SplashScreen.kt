package com.example.csievent.presentation.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.R
import com.example.csievent.presentation.navigation.Routes
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * Galaxy Constellation Splash Screen — v4
 *
 * v3 features (unchanged):
 * - Stars shoot in with streaking tails
 * - Constellation lines draw between stars
 * - Big bang burst explosion
 * - Logo slams in with spring bounce
 * - Shockwave ring expands from logo
 * - Gold orbiting dot
 * - Gold bouncing loader dots
 *
 * NEW in v4:
 * - [C] Typewriter effect — "CSI Events" types letter by letter with blinking cursor
 * - [D] Energy rings — 3 concentric sonar rings pulse outward after logo slams in
 *         Ring 1: Indigo   (fastest)
 *         Ring 2: Violet   (medium)
 *         Ring 3: Gold     (slowest, most dramatic)
 *
 * File: app/src/main/java/com/example/csievent/presentation/splash/SplashScreen.kt
 */
@Composable
fun SplashScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // ── Phase controller ──────────────────────────────────────────────
    var phase by remember { mutableStateOf(0) }

    // ── Phase animations ──────────────────────────────────────────────
    val starProgress by animateFloatAsState(
        targetValue   = if (phase >= 1) 1f else 0f,
        animationSpec = tween(900, easing = EaseOutExpo),
        label         = "stars"
    )
    val lineProgress by animateFloatAsState(
        targetValue   = if (phase >= 2) 1f else 0f,
        animationSpec = tween(700, easing = EaseInOutCubic),
        label         = "lines"
    )
    val burstProgress by animateFloatAsState(
        targetValue   = if (phase >= 3) 1f else 0f,
        animationSpec = tween(500, easing = EaseOutExpo),
        label         = "burst"
    )
    val shockwaveScale by animateFloatAsState(
        targetValue   = if (phase >= 4) 4f else 0f,
        animationSpec = tween(800, easing = EaseOutExpo),
        label         = "shock_scale"
    )
    val shockwaveAlpha by animateFloatAsState(
        targetValue   = if (phase >= 4) 0f else 1f,
        animationSpec = tween(800),
        label         = "shock_alpha"
    )
    val logoScale by animateFloatAsState(
        targetValue   = if (phase >= 3) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "logo"
    )
    val logoAlpha by animateFloatAsState(
        targetValue   = if (phase >= 3) 1f else 0f,
        animationSpec = tween(300),
        label         = "logo_alpha"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue   = if (phase >= 5) 1f else 0f,
        animationSpec = tween(700),
        label         = "sub_alpha"
    )

    // ── [C] Typewriter: reveal characters one by one ──────────────────
    val fullTitle    = "CSI Events"
    var visibleChars by remember { mutableStateOf(0) }
    val displayText  = fullTitle.take(visibleChars)

    // Cursor blink
    val cursorVisible by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor"
    )
    // Only show cursor while typing or just finished
    val showCursor = phase >= 4 && visibleChars <= fullTitle.length

    // ── [D] Energy rings — 3 independent pulse cycles ─────────────────
    // Each ring expands from scale 0 → 3 and fades out
    // Staggered start times give sonar wave feel

    val ring1Scale by animateFloatAsState(
        targetValue   = if (phase >= 4) 3f else 0f,
        animationSpec = tween(1200, easing = EaseOutExpo),
        label         = "ring1_scale"
    )
    val ring1Alpha by animateFloatAsState(
        targetValue   = if (phase >= 4) 0f else 0f,
        animationSpec = tween(1200),
        label         = "ring1_alpha"
    )

    // Ring 2 — delayed by 300ms via phase tracking
    var ring2Active by remember { mutableStateOf(false) }
    val ring2Scale by animateFloatAsState(
        targetValue   = if (ring2Active) 3f else 0f,
        animationSpec = tween(1400, easing = EaseOutExpo),
        label         = "ring2_scale"
    )
    val ring2Alpha by animateFloatAsState(
        targetValue   = if (ring2Active) 0f else 0.8f,
        animationSpec = tween(1400),
        label         = "ring2_alpha"
    )

    // Ring 3 — delayed by 600ms
    var ring3Active by remember { mutableStateOf(false) }
    val ring3Scale by animateFloatAsState(
        targetValue   = if (ring3Active) 3.5f else 0f,
        animationSpec = tween(1600, easing = EaseOutExpo),
        label         = "ring3_scale"
    )
    val ring3Alpha by animateFloatAsState(
        targetValue   = if (ring3Active) 0f else 0.6f,
        animationSpec = tween(1600),
        label         = "ring3_alpha"
    )

    // ── Continuous animations ─────────────────────────────────────────
    val twinkle by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label         = "twinkle"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue  = 0.6f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "glow"
    )
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label         = "orbit"
    )

    // ── Phase sequencer ───────────────────────────────────────────────
    LaunchedEffect(Unit) {
        phase = 1
        delay(900)
        phase = 2
        delay(700)
        phase = 3                     // logo slams in + burst
        delay(200)
        phase = 4                     // shockwave + ring 1 fires
        ring2Active = true            // ring 2 fires immediately with ring 1
        delay(300)
        ring3Active = true            // ring 3 fires 300ms later

        // [C] Typewriter — type each character
        repeat(fullTitle.length) {
            visibleChars++
            delay(85)                 // ~85ms per character
        }
        delay(100)
        phase = 5                     // subtitle fades in
        delay(1200)

        viewModel.checkAuth { role ->
            val dest = when (role) {
                "ORGANIZER" -> Routes.ORGANIZER_DASHBOARD
                "JUDGE"     -> Routes.JUDGE_DASHBOARD
                "STUDENT"   -> Routes.STUDENT_DASHBOARD
                else        -> Routes.LOGIN
            }
            navController.navigate(dest) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    // ── Colors ────────────────────────────────────────────────────────
    val deepBlack  = Color(0xFF030308)
    val deepIndigo = Color(0xFF0D0A2E)
    val indigo     = Color(0xFF4B3CC8)
    val indigoGlow = Color(0xFF6B5FD6)
    val violet     = Color(0xFF7C3AED)
    val gold       = Color(0xFFD4AF37)
    val goldLight  = Color(0xFFFFF3A0)
    val starWhite  = Color(0xFFF0EEFF)
    val cyanGlow   = Color(0xFF00D4FF)

    // ── Star + connection data ────────────────────────────────────────
    val starData = remember {
        listOf(
            Triple(Offset(0.12f, 0.08f), 2.5f, 220f),
            Triple(Offset(0.88f, 0.06f), 2f,   320f),
            Triple(Offset(0.45f, 0.04f), 3f,   270f),
            Triple(Offset(0.22f, 0.18f), 1.5f, 200f),
            Triple(Offset(0.75f, 0.14f), 2f,   340f),
            Triple(Offset(0.55f, 0.22f), 2.5f, 250f),
            Triple(Offset(0.04f, 0.38f), 1.5f, 180f),
            Triple(Offset(0.96f, 0.32f), 2f,   0f),
            Triple(Offset(0.33f, 0.45f), 3f,   210f),
            Triple(Offset(0.85f, 0.48f), 1.5f, 10f),
            Triple(Offset(0.15f, 0.58f), 2f,   190f),
            Triple(Offset(0.68f, 0.60f), 2.5f, 330f),
            Triple(Offset(0.42f, 0.70f), 2f,   240f),
            Triple(Offset(0.80f, 0.74f), 1.5f, 350f),
            Triple(Offset(0.28f, 0.80f), 3f,   150f),
            Triple(Offset(0.60f, 0.88f), 2f,   290f),
            Triple(Offset(0.10f, 0.88f), 1.5f, 160f),
            Triple(Offset(0.92f, 0.82f), 2.5f, 20f),
            Triple(Offset(0.50f, 0.94f), 2f,   270f),
            Triple(Offset(0.38f, 0.15f), 1.5f, 230f),
            Triple(Offset(0.70f, 0.30f), 2f,   310f),
            Triple(Offset(0.48f, 0.52f), 1.5f, 260f),
            Triple(Offset(0.18f, 0.40f), 2f,   170f),
            Triple(Offset(0.82f, 0.22f), 2.5f, 30f),
        )
    }
    val connections = remember {
        listOf(
            0 to 4, 4 to 7, 7 to 11, 11 to 14,
            0 to 2, 2 to 5, 5 to 8,  8 to 12,
            1 to 4, 4 to 9, 9 to 13, 13 to 17,
            2 to 5, 5 to 10, 10 to 15, 3 to 8,
            6 to 10, 10 to 14, 14 to 18, 19 to 20,
            20 to 21, 21 to 22, 22 to 23
        )
    }
    val burstAngles = remember { (0 until 24).map { i -> i * 15f } }

    // ─────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(deepIndigo, deepBlack),
                    radius = 2000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Stars + Constellation + Burst canvas ──────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Burst particles
            if (burstProgress > 0f) {
                burstAngles.forEachIndexed { i, angle ->
                    val rad     = angle * PI.toFloat() / 180f
                    val dist    = burstProgress * (80f + (i % 5) * 30f)
                    val alpha   = (1f - burstProgress).coerceIn(0f, 1f)
                    val pRadius = (3f + (i % 4)) * (1f - burstProgress * 0.5f)
                    val pColor  = when (i % 4) {
                        0    -> goldLight
                        1    -> Color(0xFF8C83E4)
                        2    -> cyanGlow
                        else -> Color.White
                    }
                    drawCircle(
                        color  = pColor.copy(alpha = alpha * 0.9f),
                        radius = pRadius,
                        center = Offset(cx + dist * cos(rad), cy + dist * sin(rad))
                    )
                    if (dist > 10f) {
                        drawLine(
                            color       = pColor.copy(alpha = alpha * 0.35f),
                            start       = Offset(cx, cy),
                            end         = Offset(cx + dist * cos(rad), cy + dist * sin(rad)),
                            strokeWidth = 0.5f
                        )
                    }
                }
            }

            // Constellation lines
            if (lineProgress > 0f) {
                connections.forEachIndexed { idx, (a, b) ->
                    val startDelay = (idx.toFloat() / connections.size) * 0.6f
                    val localProg  = ((lineProgress - startDelay) / 0.4f).coerceIn(0f, 1f)
                    if (localProg <= 0f) return@forEachIndexed
                    val sA = starData[a].first
                    val sB = starData[b].first
                    val ax = size.width * sA.x
                    val ay = size.height * sA.y
                    val bx = size.width * sB.x
                    val by_ = size.height * sB.y
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4B3CC8).copy(alpha = 0.4f),
                                Color(0xFF8C83E4).copy(alpha = 0.15f)
                            ),
                            start = Offset(ax, ay), end = Offset(bx, by_)
                        ),
                        start       = Offset(ax, ay),
                        end         = Offset(ax + (bx - ax) * localProg, ay + (by_ - ay) * localProg),
                        strokeWidth = 0.8f
                    )
                }
            }

            // Stars
            starData.forEachIndexed { i, (pos, radius, shootAngle) ->
                val destX   = size.width  * pos.x
                val destY   = size.height * pos.y
                val rad     = shootAngle * PI.toFloat() / 180f
                val srcX    = destX + 300f * cos(rad)
                val srcY    = destY + 300f * sin(rad)
                val curX    = srcX + (destX - srcX) * starProgress
                val curY    = srcY + (destY - srcY) * starProgress
                val tw      = (sin(twinkle + i * 0.9f) * 0.4f + 0.6f).toFloat()
                val sAlpha  = starProgress * tw

                if (starProgress < 1f) {
                    val streakLen = (1f - starProgress) * 40f
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(starWhite.copy(alpha = sAlpha * 0.8f), Color.Transparent),
                            start  = Offset(curX, curY),
                            end    = Offset(curX + streakLen * cos(rad + PI.toFloat()), curY + streakLen * sin(rad + PI.toFloat()))
                        ),
                        start       = Offset(curX, curY),
                        end         = Offset(curX + streakLen * cos(rad + PI.toFloat()), curY + streakLen * sin(rad + PI.toFloat())),
                        strokeWidth = radius * 0.5f
                    )
                }
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(starWhite.copy(alpha = sAlpha), Color(0xFF8C83E4).copy(alpha = sAlpha * 0.3f), Color.Transparent),
                        radius = radius * 4f, center = Offset(curX, curY)
                    ),
                    radius = radius * 4f, center = Offset(curX, curY)
                )
                drawCircle(color = starWhite.copy(alpha = sAlpha), radius = radius, center = Offset(curX, curY))
            }
        }

        // ── [D] Energy rings — 3 sonar pulses ─────────────────────────
        // Ring 1: Indigo (fires first)
        if (phase >= 4) {
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .scale(ring1Scale)
                    .alpha((1f - ring1Scale / 3f).coerceIn(0f, 0.9f))
            ) {
                drawCircle(
                    color  = indigo.copy(alpha = 0.8f),
                    radius = size.minDimension / 2f,
                    style  = Stroke(width = 2.5f)
                )
            }
        }

        // Ring 2: Violet (300ms after ring 1)
        if (ring2Active) {
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .scale(ring2Scale)
                    .alpha(ring2Alpha.coerceIn(0f, 0.8f))
            ) {
                drawCircle(
                    color  = violet.copy(alpha = 0.7f),
                    radius = size.minDimension / 2f,
                    style  = Stroke(width = 2f)
                )
            }
        }

        // Ring 3: Gold (600ms after ring 1 — most dramatic)
        if (ring3Active) {
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .scale(ring3Scale)
                    .alpha(ring3Alpha.coerceIn(0f, 0.7f))
            ) {
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(Color.Transparent, gold.copy(alpha = 0.6f), Color.Transparent)
                    ),
                    radius = size.minDimension / 2f,
                    style  = Stroke(width = 1.5f)
                )
            }
        }

        // ── Shockwave ring ────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .size(120.dp)
                .scale(shockwaveScale)
                .alpha(shockwaveAlpha * 0.6f)
        ) {
            drawCircle(
                brush  = Brush.radialGradient(
                    colors = listOf(Color.Transparent, indigoGlow.copy(alpha = 0.8f), Color.Transparent)
                ),
                radius = size.minDimension / 2f,
                style  = Stroke(width = 3f)
            )
        }

        // ── Logo glow ─────────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .alpha(logoAlpha * glowPulse * 0.5f)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(indigoGlow.copy(alpha = 0.5f), violet.copy(alpha = 0.2f), Color.Transparent)
                ),
                radius = size.minDimension / 2f
            )
        }

        // ── Orbiting gold dot ─────────────────────────────────────────
        if (phase >= 4) {
            Canvas(modifier = Modifier.size(170.dp)) {
                val rad  = orbitAngle * PI.toFloat() / 180f
                val dotX = center.x + (size.width / 2f) * cos(rad)
                val dotY = center.y + (size.height / 2f * 0.4f) * sin(rad)
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(goldLight, gold.copy(alpha = 0f)),
                        radius = 8f, center = Offset(dotX, dotY)
                    ),
                    radius = 8f, center = Offset(dotX, dotY)
                )
                drawCircle(color = Color.White, radius = 3f, center = Offset(dotX, dotY))
            }
        }

        // ── Logo ──────────────────────────────────────────────────────
        Image(
            painter            = painterResource(id = R.drawable.csi_logo),
            contentDescription = "CSI VIT-AP",
            contentScale       = ContentScale.Fit,
            modifier           = Modifier
                .size(140.dp)
                .scale(logoScale)
                .alpha(logoAlpha)
        )

        // ── Text content ──────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {

            // [C] Typewriter title with blinking cursor
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text          = displayText,
                    color         = Color.White,
                    fontSize      = 36.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                    textAlign     = TextAlign.Center
                )
                // Blinking cursor
                if (showCursor) {
                    Text(
                        text       = "|",
                        color      = Color(0xFF8C83E4).copy(alpha = cursorVisible),
                        fontSize   = 36.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gold divider
            Canvas(
                modifier = Modifier
                    .width(200.dp)
                    .height(1.dp)
                    .alpha(subtitleAlpha)
            ) {
                drawLine(
                    brush       = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, gold, Color.Transparent)
                    ),
                    start       = Offset(0f, 0f),
                    end         = Offset(size.width, 0f),
                    strokeWidth = 1.5f
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text          = "VIT-AP  ·  TECHNICAL CLUB",
                color         = gold.copy(alpha = 0.85f),
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 3.sp,
                textAlign     = TextAlign.Center,
                modifier      = Modifier.alpha(subtitleAlpha)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Gold bouncing dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier              = Modifier.alpha(subtitleAlpha)
            ) {
                repeat(3) { i ->
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue  = 0.5f,
                        targetValue   = 1f,
                        animationSpec = infiniteRepeatable(
                            animation          = tween(500),
                            repeatMode         = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(i * 160)
                        ),
                        label = "dot$i"
                    )
                    Canvas(modifier = Modifier.size(7.dp).scale(dotScale)) {
                        drawCircle(
                            brush = Brush.radialGradient(colors = listOf(goldLight, gold))
                        )
                    }
                }
            }
        }
    }
}