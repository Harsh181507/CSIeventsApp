package com.example.csievent.presentation.organizer.roles

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.dto.user.UserResponseDto
import com.example.csievent.presentation.student.CSILoader
import com.example.csievent.presentation.student.LoaderSize
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ── constants ─────────────────────────────────────────────────────────────────
private val JUDGE_COL   = Color(0xFFD4AF37)
private val STUDENT_COL = Color(0xFF4B3CC8)
private val PROMOTE_COL = Color(0xFF10B981)
private val DEMOTE_COL  = Color(0xFFEF4444)

// Fixed background stars
private val RM3_STARS = listOf(
    0.05f to 0.03f, 0.92f to 0.07f, 0.41f to 0.02f, 0.15f to 0.14f, 0.74f to 0.09f,
    0.03f to 0.28f, 0.97f to 0.21f, 0.29f to 0.37f, 0.83f to 0.42f, 0.08f to 0.52f,
    0.66f to 0.57f, 0.52f to 0.81f, 0.21f to 0.74f, 0.87f to 0.69f, 0.46f to 0.90f,
    0.70f to 0.88f, 0.33f to 0.63f, 0.60f to 0.22f, 0.48f to 0.47f, 0.24f to 0.33f,
    0.78f to 0.73f, 0.37f to 0.56f, 0.91f to 0.84f, 0.12f to 0.68f, 0.57f to 0.96f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleManagementScreen(
    navController: NavHostController,
    viewModel:     RoleManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadUsers() }

    var query by remember { mutableStateOf("") }
    val trimmed = query.trim().lowercase()

    val allJudges   = state.users.filter { it.role == "JUDGE" }
    val allStudents = state.users.filter { it.role == "STUDENT" }

    val judges = if (trimmed.isEmpty()) allJudges
    else allJudges.filter {
        it.name.lowercase().contains(trimmed) || it.email.lowercase().contains(trimmed)
    }
    val students = if (trimmed.isEmpty()) allStudents
    else allStudents.filter {
        it.name.lowercase().contains(trimmed) || it.email.lowercase().contains(trimmed)
    }

    // toast flash
    var flashSuccess by remember { mutableStateOf(false) }
    var flashError   by remember { mutableStateOf(false) }
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) { flashSuccess = true; delay(2500); flashSuccess = false }
    }
    LaunchedEffect(state.error) {
        if (state.error != null) { flashError = true; delay(2500); flashError = false }
    }

    // ── continuous animations ──────────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "rm3")
    val twinkle    by inf.animateFloat(0f, (2f*PI).toFloat(),
        infiniteRepeatable(tween(9000, easing=LinearEasing)), "tw")
    val nebDrift   by inf.animateFloat(0f, 22f,
        infiniteRepeatable(tween(14000, easing=EaseInOutSine), RepeatMode.Reverse), "nd")
    val ring1      by inf.animateFloat(360f, 0f,
        infiniteRepeatable(tween(20000, easing=LinearEasing)), "r1")
    val ring2      by inf.animateFloat(0f, 360f,
        infiniteRepeatable(tween(13000, easing=LinearEasing)), "r2")
    val heroBreath by inf.animateFloat(0.94f, 1.06f,
        infiniteRepeatable(tween(2800, easing=EaseInOutSine), RepeatMode.Reverse), "hb")
    val scanLine   by inf.animateFloat(-0.05f, 1.05f,
        infiniteRepeatable(tween(5000, easing=LinearEasing)), "sl")
    val particleT  by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(3500, easing=LinearEasing)), "pt")
    val beaconPulse by inf.animateFloat(0.25f, 1f,
        infiniteRepeatable(tween(900, easing=EaseInOutSine), RepeatMode.Reverse), "bp")

    // header slide
    var headerIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); headerIn = true }
    val headerA by animateFloatAsState(if(headerIn)1f else 0f, tween(700), label="ha")
    val headerY by animateFloatAsState(if(headerIn)0f else -50f,
        spring(0.65f, Spring.StiffnessMediumLow), label="hy")

    Box(Modifier.fillMaxSize().background(Color(0xFF020912))) {

        // ── STARFIELD BACKGROUND ──────────────────────────────────────
        Canvas(Modifier.fillMaxSize()) {
            drawRect(brush=Brush.verticalGradient(
                listOf(Color(0xFF030B1A), Color(0xFF020810), Color(0xFF040614))))
            // Nebulae
            drawCircle(brush=Brush.radialGradient(
                listOf(Color(0xFF1A0A50).copy(alpha=0.48f), Color.Transparent),
                radius=440f, center=Offset(size.width*0.82f+nebDrift, size.height*0.18f)),
                radius=440f, center=Offset(size.width*0.82f+nebDrift, size.height*0.18f))
            drawCircle(brush=Brush.radialGradient(
                listOf(Color(0xFF041830).copy(alpha=0.38f), Color.Transparent),
                radius=340f, center=Offset(size.width*0.1f, size.height*0.72f)),
                radius=340f, center=Offset(size.width*0.1f, size.height*0.72f))
            // Stars
            RM3_STARS.forEachIndexed { i,(x,y)->
                val tw = (sin(twinkle+i*0.58f)*0.3f+0.7f).toFloat()
                val r  = when(i%5){0->2.2f;1->1.6f;else->1.0f}
                val pos = Offset(size.width*x, size.height*y)
                drawCircle(Color.White.copy(alpha=tw*0.6f), r, pos)
                if(i%5==0){
                    drawLine(Color.White.copy(alpha=tw*0.16f),
                        Offset(pos.x-7f,pos.y),Offset(pos.x+7f,pos.y),0.5f)
                    drawLine(Color.White.copy(alpha=tw*0.16f),
                        Offset(pos.x,pos.y-7f),Offset(pos.x,pos.y+7f),0.5f)
                }
            }
            // Scan line
            val sp = size.height * scanLine
            drawRect(brush=Brush.verticalGradient(
                listOf(Color.Transparent,Color(0xFF4B3CC8).copy(alpha=0.04f),
                    Color(0xFF4B3CC8).copy(alpha=0.07f),Color(0xFF4B3CC8).copy(alpha=0.04f),
                    Color.Transparent),
                startY=sp-26f, endY=sp+26f), size=size)
        }

        // ── TOAST ─────────────────────────────────────────────────────
        AnimatedVisibility(flashSuccess,
            enter=fadeIn()+slideInVertically{-40},
            exit=fadeOut()+slideOutVertically{-40},
            modifier=Modifier.align(Alignment.TopCenter).padding(top=72.dp)) {
            Box(Modifier.clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0A2218))
                .border(1.dp,PROMOTE_COL.copy(alpha=0.5f),RoundedCornerShape(12.dp))
                .padding(horizontal=20.dp,vertical=10.dp)){
                Row(verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Box(Modifier.size(7.dp).clip(CircleShape).background(PROMOTE_COL))
                    Text(state.successMessage?:"",color=PROMOTE_COL,fontSize=13.sp,
                        fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace)
                }
            }
        }
        AnimatedVisibility(flashError,
            enter=fadeIn()+slideInVertically{-40},
            exit=fadeOut()+slideOutVertically{-40},
            modifier=Modifier.align(Alignment.TopCenter).padding(top=72.dp)) {
            Box(Modifier.clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1A0808))
                .border(1.dp,DEMOTE_COL.copy(alpha=0.5f),RoundedCornerShape(12.dp))
                .padding(horizontal=20.dp,vertical=10.dp)){
                Row(verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Box(Modifier.size(7.dp).clip(CircleShape).background(DEMOTE_COL))
                    Text(state.error?:"",color=DEMOTE_COL,fontSize=13.sp,
                        fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace)
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("CREW MANIFEST", color=Color.White, fontSize=14.sp,
                                fontWeight=FontWeight.Bold, fontFamily=FontFamily.Monospace,
                                letterSpacing=2.sp)
                            Text("Role Assignment Console",
                                color=Color(0xFF8C83E4).copy(alpha=0.5f), fontSize=10.sp)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick={navController.popBackStack()}){
                            Icon(Icons.Default.ArrowBack,null,tint=Color(0xFF8C83E4))
                        }
                    },
                    actions = {
                        IconButton(onClick={viewModel.loadUsers()}){
                            Icon(Icons.Default.Refresh,null,tint=Color(0xFF8C83E4))
                        }
                    },
                    colors=TopAppBarDefaults.topAppBarColors(containerColor=Color.Transparent)
                )
            },
            containerColor=Color.Transparent
        ) { pad ->
            LazyColumn(
                modifier=Modifier.fillMaxSize().padding(pad),
                contentPadding=PaddingValues(bottom=32.dp)
            ) {

                // ── STAR MAP HERO ─────────────────────────────────────
                item {
                    Box(
                        modifier=Modifier.fillMaxWidth().height(230.dp)
                            .offset(y=headerY.dp).alpha(headerA),
                        contentAlignment=Alignment.Center
                    ) {
                        Canvas(Modifier.fillMaxWidth().height(230.dp)) {
                            val cx = size.width/2f
                            val cy = size.height/2f
                            val hr = 52.dp.toPx()

                            // Far glow
                            drawCircle(brush=Brush.radialGradient(
                                listOf(JUDGE_COL.copy(alpha=0.10f*heroBreath),Color.Transparent),
                                center=Offset(cx,cy),radius=hr*4f),
                                radius=hr*4f, center=Offset(cx,cy))

                            // Outer dashed ring — gold, CCW
                            rotate(ring1, Offset(cx,cy)){
                                drawCircle(JUDGE_COL.copy(alpha=0.32f),hr*2.3f,Offset(cx,cy),
                                    style=Stroke(1f,pathEffect=PathEffect.dashPathEffect(floatArrayOf(20f,10f))))
                                for(i in 0..3){
                                    val a=i*PI.toFloat()/2f
                                    val nx=cx+hr*2.3f*cos(a); val ny=cy+hr*2.3f*sin(a)
                                    drawCircle(JUDGE_COL.copy(alpha=0.6f),3.5f,Offset(nx,ny))
                                    drawCircle(JUDGE_COL.copy(alpha=0.2f),7f,Offset(nx,ny),style=Stroke(0.8f))
                                }
                            }
                            // Middle ring — indigo CW
                            rotate(ring2, Offset(cx,cy)){
                                drawCircle(STUDENT_COL.copy(alpha=0.4f),hr*1.6f,Offset(cx,cy),
                                    style=Stroke(1.2f))
                                drawCircle(STUDENT_COL.copy(alpha=0.7f),3f,Offset(cx+hr*1.6f,cy))
                                drawCircle(STUDENT_COL.copy(alpha=0.7f),3f,Offset(cx-hr*1.6f,cy))
                            }

                            // Orbiting crew particles — gold for judges, indigo for students
                            val judgeCount   = allJudges.size.coerceAtMost(6)
                            val studentCount = allStudents.size.coerceAtMost(6)
                            for(i in 0 until judgeCount){
                                val t  = (particleT + i.toFloat()/judgeCount.coerceAtLeast(1))%1f
                                val a  = t*2f*PI.toFloat()
                                val pr = hr*1.92f
                                val px = cx+pr*cos(a); val py = cy+pr*0.4f*sin(a)
                                drawCircle(JUDGE_COL.copy(alpha=0.75f),4f,Offset(px,py))
                                drawCircle(JUDGE_COL.copy(alpha=0.2f),9f,Offset(px,py),style=Stroke(0.8f))
                            }
                            for(i in 0 until studentCount){
                                val t  = (particleT + 0.5f + i.toFloat()/studentCount.coerceAtLeast(1))%1f
                                val a  = t*2f*PI.toFloat()
                                val pr = hr*1.35f
                                val px = cx+pr*cos(a); val py = cy+pr*0.5f*sin(a)
                                drawCircle(STUDENT_COL.copy(alpha=0.65f),3f,Offset(px,py))
                            }

                            // Two-tier hierarchy lines from core
                            // Judge tier above
                            drawLine(JUDGE_COL.copy(alpha=0.25f),Offset(cx,cy-hr*0.4f),Offset(cx-hr*0.55f,cy-hr*0.75f),0.8f)
                            drawLine(JUDGE_COL.copy(alpha=0.25f),Offset(cx,cy-hr*0.4f),Offset(cx+hr*0.55f,cy-hr*0.75f),0.8f)
                            drawCircle(JUDGE_COL.copy(alpha=0.6f),5f,Offset(cx,cy-hr*0.4f))
                            drawCircle(JUDGE_COL.copy(alpha=0.3f),10f,Offset(cx,cy-hr*0.4f),style=Stroke(0.8f))
                            drawCircle(JUDGE_COL.copy(alpha=0.4f),4f,Offset(cx-hr*0.55f,cy-hr*0.75f))
                            drawCircle(JUDGE_COL.copy(alpha=0.4f),4f,Offset(cx+hr*0.55f,cy-hr*0.75f))

                            // Student tier below
                            drawLine(STUDENT_COL.copy(alpha=0.2f),Offset(cx,cy+hr*0.35f),Offset(cx-hr*0.6f,cy+hr*0.7f),0.7f)
                            drawLine(STUDENT_COL.copy(alpha=0.2f),Offset(cx,cy+hr*0.35f),Offset(cx,cy+hr*0.78f),0.7f)
                            drawLine(STUDENT_COL.copy(alpha=0.2f),Offset(cx,cy+hr*0.35f),Offset(cx+hr*0.6f,cy+hr*0.7f),0.7f)
                            drawCircle(STUDENT_COL.copy(alpha=0.5f),4f,Offset(cx,cy+hr*0.35f))
                            for(sx in listOf(-hr*0.6f,0f,hr*0.6f)){
                                drawCircle(STUDENT_COL.copy(alpha=0.35f),3.5f,Offset(cx+sx,cy+hr*0.72f))
                            }

                            // Central core connecting line
                            drawLine(Color(0xFF8C83E4).copy(alpha=0.3f),Offset(cx,cy-hr*0.38f),Offset(cx,cy+hr*0.33f),0.8f)

                            // Core body — circle with gradient
                            drawCircle(brush=Brush.radialGradient(
                                listOf(Color(0xFF2A1A60),Color(0xFF100828),Color(0xFF060212)),
                                center=Offset(cx-hr*0.15f,cy-hr*0.18f),radius=hr*1.4f),
                                radius=hr*0.9f,center=Offset(cx,cy))
                            drawCircle(JUDGE_COL.copy(alpha=0.5f*heroBreath),hr*0.9f,Offset(cx,cy),
                                style=Stroke(1.5f))
                            drawCircle(Color.White.copy(alpha=0.22f),hr*0.24f,
                                Offset(cx-hr*0.28f,cy-hr*0.3f))
                        }

                        // Core label
                        Text("CREW", color=Color.White.copy(alpha=0.85f), fontSize=10.sp,
                            fontWeight=FontWeight.Bold, fontFamily=FontFamily.Monospace,
                            letterSpacing=3.sp)
                    }
                }

                // ── STATS ROW ─────────────────────────────────────────
                if (!state.isLoading && state.users.isNotEmpty()) {
                    item {
                        Row(Modifier.fillMaxWidth()
                            .padding(horizontal=20.dp).padding(bottom=16.dp),
                            horizontalArrangement=Arrangement.spacedBy(10.dp)){
                            Rm3Stat("${state.users.size}","CREW",Color(0xFF8C83E4),Modifier.weight(1f))
                            Rm3Stat("${allJudges.size}","JUDGES",JUDGE_COL,Modifier.weight(1f))
                            Rm3Stat("${allStudents.size}","STUDENTS",STUDENT_COL,Modifier.weight(1f))
                        }
                    }
                }

                // ── SEARCH ────────────────────────────────────────────
                if (!state.isLoading && state.users.isNotEmpty()) {
                    item {
                        Column(Modifier.fillMaxWidth()
                            .padding(horizontal=20.dp).padding(bottom=16.dp),
                            verticalArrangement=Arrangement.spacedBy(6.dp)){
                            OutlinedTextField(
                                value=query, onValueChange={query=it},
                                placeholder={Text("Search by name or email...",
                                    color=Color(0xFF3A3060),fontSize=13.sp)},
                                leadingIcon={Icon(Icons.Default.Search,null,
                                    tint=if(query.isNotEmpty())Color(0xFF8B6DFF) else Color(0xFF3A3060),
                                    modifier=Modifier.size(18.dp))},
                                trailingIcon={
                                    AnimatedVisibility(query.isNotEmpty()){
                                        IconButton(onClick={query=""}){
                                            Icon(Icons.Default.Close,null,
                                                tint=Color(0xFF8C83E4),modifier=Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine=true,
                                keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(
                                    imeAction=ImeAction.Search),
                                shape=RoundedCornerShape(14.dp),
                                colors=OutlinedTextFieldDefaults.colors(
                                    focusedTextColor=Color.White,unfocusedTextColor=Color.White,
                                    focusedBorderColor=Color(0xFF8B6DFF),
                                    unfocusedBorderColor=Color(0xFF2A2060),
                                    focusedContainerColor=Color(0xFF08081A),
                                    unfocusedContainerColor=Color(0xFF06060E),
                                    focusedLabelColor=Color(0xFF8B6DFF),
                                    unfocusedLabelColor=Color(0xFF3A3060)
                                ),
                                modifier=Modifier.fillMaxWidth()
                            )
                            // result count
                            if (query.isNotEmpty()) {
                                val total = judges.size + students.size
                                Text(
                                    "$total result${if(total!=1)"s" else ""} found",
                                    color=Color(0xFF8B6DFF).copy(alpha=0.5f),
                                    fontSize=9.sp, fontFamily=FontFamily.Monospace,
                                    modifier=Modifier.padding(start=4.dp)
                                )
                            }
                        }
                    }
                }

                // ── LOADING ───────────────────────────────────────────
                if (state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment=Alignment.Center){
                            Column(horizontalAlignment=Alignment.CenterHorizontally,
                                verticalArrangement=Arrangement.spacedBy(14.dp)){
                                CSILoader(size=LoaderSize.LARGE)
                                Text("Loading crew manifest...",
                                    color=Color(0xFF8C83E4).copy(alpha=0.6f),fontSize=12.sp)
                            }
                        }
                    }
                }

                // ── EMPTY ─────────────────────────────────────────────
                if (!state.isLoading && state.users.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().height(160.dp),
                            contentAlignment=Alignment.Center){
                            Column(horizontalAlignment=Alignment.CenterHorizontally,
                                verticalArrangement=Arrangement.spacedBy(8.dp)){
                                Canvas(Modifier.size(50.dp)){
                                    val c=size.minDimension/2f
                                    drawCircle(STUDENT_COL.copy(alpha=0.15f),c)
                                    drawCircle(STUDENT_COL.copy(alpha=0.3f),c,style=Stroke(1.2f))
                                    for(i in 0..2){
                                        val a=(i*120f-90f)*PI.toFloat()/180f
                                        drawCircle(STUDENT_COL.copy(alpha=0.5f),4f,
                                            Offset(c+c*0.52f*cos(a),c+c*0.52f*sin(a)))
                                    }
                                }
                                Text("No crew members found",color=Color.White,
                                    fontSize=16.sp,fontWeight=FontWeight.Bold)
                                Text("Tap refresh to reload",
                                    color=Color(0xFF8C83E4).copy(alpha=0.5f),fontSize=12.sp)
                            }
                        }
                    }
                }

                // ── NO SEARCH RESULTS ─────────────────────────────────
                if (!state.isLoading && query.isNotEmpty() &&
                    judges.isEmpty() && students.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top=20.dp),
                            contentAlignment=Alignment.Center){
                            Text("No crew members match \"$query\"",
                                color=Color(0xFF8C83E4).copy(alpha=0.6f),
                                fontSize=13.sp, textAlign=TextAlign.Center)
                        }
                    }
                }

                // ── JUDGES SECTION ────────────────────────────────────
                if (!state.isLoading && judges.isNotEmpty()) {
                    item { Rm3SectionHeader("JUDGES", judges.size, JUDGE_COL) }
                    itemsIndexed(judges, key={_,u->"j_${u.id}"}) { idx, user ->
                        Box(Modifier.padding(horizontal=20.dp).padding(bottom=10.dp)){
                            Rm3Card(user, idx, true, DEMOTE_COL, "DEMOTE TO STUDENT",
                                { viewModel.updateRole(user.id,"STUDENT"); viewModel.clearMessage() })
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                // ── STUDENTS SECTION ──────────────────────────────────
                if (!state.isLoading && students.isNotEmpty()) {
                    item { Rm3SectionHeader("STUDENTS", students.size, STUDENT_COL) }
                    itemsIndexed(students, key={_,u->"s_${u.id}"}) { idx, user ->
                        Box(Modifier.padding(horizontal=20.dp).padding(bottom=10.dp)){
                            Rm3Card(user, idx, false, PROMOTE_COL, "PROMOTE TO JUDGE",
                                { viewModel.updateRole(user.id,"JUDGE"); viewModel.clearMessage() })
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// =============================================================================
// SECTION HEADER
// =============================================================================
@Composable
private fun Rm3SectionHeader(label: String, count: Int, color: Color) {
    Row(Modifier.fillMaxWidth().padding(horizontal=20.dp, vertical=6.dp),
        verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        // Animated indicator bar
        val inf = rememberInfiniteTransition(label="sh")
        val glow by inf.animateFloat(0.4f,1f,
            infiniteRepeatable(tween(1200,easing=EaseInOutSine),RepeatMode.Reverse),"sg")
        Box(Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(2.dp))
            .background(color.copy(alpha=glow)))
        Text(label, color=color.copy(alpha=0.85f), fontSize=11.sp,
            fontWeight=FontWeight.Bold, letterSpacing=2.sp)
        Box(Modifier.clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha=0.12f))
            .border(0.5.dp,color.copy(alpha=0.3f),RoundedCornerShape(8.dp))
            .padding(horizontal=8.dp,vertical=2.dp)){
            Text("$count", color=color, fontSize=10.sp,
                fontFamily=FontFamily.Monospace, fontWeight=FontWeight.Bold)
        }
        Box(Modifier.weight(1f).height(1.dp).background(
            Brush.horizontalGradient(listOf(color.copy(alpha=0.3f),Color.Transparent))))
    }
}

// =============================================================================
// CREW CARD  —  the star in the star map
// =============================================================================
@Composable
private fun Rm3Card(
    user:        UserResponseDto,
    index:       Int,
    isJudge:     Boolean,
    actionColor: Color,
    actionLabel: String,
    onAction:    () -> Unit
) {
    val accent = if (isJudge) JUDGE_COL else STUDENT_COL

    // Slide from left staggered
    var vis by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index*85L+300L); vis=true }
    val entX by animateFloatAsState(if(vis)0f else -120f,
        spring(0.65f,Spring.StiffnessMediumLow), label="ex$index")
    val entA by animateFloatAsState(if(vis)1f else 0f, tween(350), label="ea$index")

    // Press scale
    var pressed by remember { mutableStateOf(false) }
    val pressS by animateFloatAsState(if(pressed)0.97f else 1f,
        spring(stiffness=Spring.StiffnessHigh), label="ps$index")

    // Two-tap confirm
    var confirm by remember { mutableStateOf(false) }

    // Card scan line
    val inf = rememberInfiniteTransition(label="rc$index")
    val scanC by inf.animateFloat(-0.1f,1.1f,
        infiniteRepeatable(tween(4000+index*220,easing=LinearEasing)),"sc")
    val starPulse by inf.animateFloat(0.8f,1f,
        infiniteRepeatable(tween(1600+index*180,easing=EaseInOutSine),RepeatMode.Reverse),"sp")

    Box(Modifier.fillMaxWidth().offset(x=entX.dp).alpha(entA).scale(pressS)) {
        // Glow edge — stronger for judges
        if (isJudge) {
            Box(Modifier.fillMaxWidth().height(2.dp).align(Alignment.BottomCenter)
                .background(Brush.horizontalGradient(
                    listOf(Color.Transparent,JUDGE_COL.copy(alpha=0.4f),Color.Transparent))))
        }

        Box(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(
                    listOf(accent.copy(alpha=0.07f), Color(0xFF040410))))
                .border(1.dp, Brush.linearGradient(
                    listOf(accent.copy(alpha=0.52f),accent.copy(alpha=0.1f),accent.copy(alpha=0.52f))),
                    RoundedCornerShape(18.dp))
                .pointerInput(Unit){
                    detectTapGestures(
                        onPress={pressed=true;tryAwaitRelease();pressed=false})
                }
        ) {
            // Sweep scan line
            Canvas(Modifier.fillMaxWidth().height(86.dp)){
                val sy=size.height*scanC
                drawRect(brush=Brush.verticalGradient(
                    listOf(Color.Transparent,accent.copy(alpha=0.04f),
                        accent.copy(alpha=0.07f),accent.copy(alpha=0.04f),Color.Transparent),
                    startY=sy-15f,endY=sy+15f),size=size)
            }

            Row(Modifier.padding(14.dp),
                verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(14.dp)){

                // Star avatar — the "star in the star map" visual
                Box(Modifier.size(50.dp), contentAlignment=Alignment.Center){
                    Canvas(Modifier.size(50.dp)){
                        val c=size.minDimension/2f
                        // Outer glow ring
                        drawCircle(accent.copy(alpha=0.18f*starPulse),c)
                        // Dashed orbit ring
                        drawCircle(accent.copy(alpha=0.3f),c-1f,
                            style=Stroke(0.8f,pathEffect=PathEffect.dashPathEffect(floatArrayOf(5f,4f))))
                        // Star body — slightly larger than indigo circle
                        drawCircle(brush=Brush.radialGradient(
                            listOf(accent.copy(alpha=0.4f),accent.copy(alpha=0.12f)),
                            center=Offset(c,c),radius=c*0.72f),
                            radius=c*0.72f,center=Offset(c,c))
                        drawCircle(accent.copy(alpha=0.6f),c*0.72f,Offset(c,c),style=Stroke(1f))
                        // Specular
                        drawCircle(Color.White.copy(alpha=0.3f),c*0.22f,
                            Offset(c-c*0.28f,c-c*0.3f))
                    }
                    // Initials
                    Text(user.name.take(2).uppercase(),
                        color=accent,fontSize=13.sp,
                        fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace)
                }

                // Info
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
                    Text(user.name,color=Color.White,fontSize=15.sp,
                        fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis)
                    Text(user.email,color=Color(0xFF8C83E4).copy(alpha=0.5f),
                        fontSize=11.sp,maxLines=1,overflow=TextOverflow.Ellipsis,
                        fontFamily=FontFamily.Monospace)
                    // Role pill
                    Box(Modifier.clip(RoundedCornerShape(20.dp))
                        .background(accent.copy(alpha=0.1f))
                        .border(0.5.dp,accent.copy(alpha=0.35f),RoundedCornerShape(20.dp))
                        .padding(horizontal=9.dp,vertical=2.dp)){
                        Text(user.role,color=accent.copy(alpha=0.8f),fontSize=8.sp,
                            fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace,
                            letterSpacing=1.sp)
                    }
                }

                // Action — two-tap confirm
                AnimatedContent(confirm,
                    transitionSpec={fadeIn(tween(180)) togetherWith fadeOut(tween(130))},
                    label="act$index"){c->
                    if(c){
                        Column(horizontalAlignment=Alignment.CenterHorizontally,
                            verticalArrangement=Arrangement.spacedBy(4.dp)){
                            Box(Modifier.clip(RoundedCornerShape(10.dp))
                                .background(actionColor.copy(alpha=0.18f))
                                .border(1.dp,actionColor.copy(alpha=0.65f),RoundedCornerShape(10.dp))
                                .clickable{onAction();confirm=false}
                                .padding(horizontal=12.dp,vertical=8.dp)){
                                Text("CONFIRM",color=actionColor,fontSize=9.sp,
                                    fontWeight=FontWeight.Bold,fontFamily=FontFamily.Monospace,
                                    letterSpacing=1.sp)
                            }
                            Text("TAP TO CONFIRM",
                                color=actionColor.copy(alpha=0.4f),fontSize=7.sp,
                                fontFamily=FontFamily.Monospace,letterSpacing=0.5.sp)
                        }
                    } else {
                        Box(Modifier.clip(RoundedCornerShape(10.dp))
                            .background(actionColor.copy(alpha=0.08f))
                            .border(1.dp,actionColor.copy(alpha=0.4f),RoundedCornerShape(10.dp))
                            .clickable{confirm=true}
                            .padding(horizontal=10.dp,vertical=8.dp)){
                            Text(actionLabel,color=actionColor.copy(alpha=0.85f),
                                fontSize=8.sp,fontWeight=FontWeight.Bold,
                                fontFamily=FontFamily.Monospace,letterSpacing=0.5.sp,
                                textAlign=TextAlign.Center)
                        }
                    }
                }
            }
        }

        // HUD corner brackets
        Canvas(Modifier.matchParentSize()){
            val s=12f;val w=1.1f;val col=accent.copy(alpha=0.38f)
            drawLine(col,Offset(0f,s),Offset(0f,0f),w);drawLine(col,Offset(0f,0f),Offset(s,0f),w)
            drawLine(col,Offset(size.width-s,0f),Offset(size.width,0f),w)
            drawLine(col,Offset(size.width,0f),Offset(size.width,s),w)
            drawLine(col,Offset(0f,size.height-s),Offset(0f,size.height),w)
            drawLine(col,Offset(0f,size.height),Offset(s,size.height),w)
            drawLine(col,Offset(size.width-s,size.height),Offset(size.width,size.height),w)
            drawLine(col,Offset(size.width,size.height-s),Offset(size.width,size.height),w)
        }
    }
}

// =============================================================================
// STAT CARD
// =============================================================================
@Composable
private fun Rm3Stat(value:String, label:String, color:Color, modifier:Modifier){
    var vis by remember{mutableStateOf(false)}
    LaunchedEffect(Unit){delay(500);vis=true}
    val s by animateFloatAsState(if(vis)1f else 0.8f,spring(0.5f,Spring.StiffnessMediumLow),label="ss")
    val a by animateFloatAsState(if(vis)1f else 0f,tween(400),label="sa")
    Box(modifier.scale(s).alpha(a)
        .clip(RoundedCornerShape(14.dp))
        .background(Brush.verticalGradient(listOf(color.copy(alpha=0.13f),color.copy(alpha=0.04f))))
        .border(1.dp,color.copy(alpha=0.22f),RoundedCornerShape(14.dp))
        .padding(vertical=14.dp),
        contentAlignment=Alignment.Center){
        Column(horizontalAlignment=Alignment.CenterHorizontally){
            Text(value,color=color,fontSize=24.sp,
                fontWeight=FontWeight.Bold,letterSpacing=(-0.5).sp)
            Text(label,color=color.copy(alpha=0.5f),fontSize=9.sp,
                fontWeight=FontWeight.SemiBold,letterSpacing=1.sp)
        }
    }
}