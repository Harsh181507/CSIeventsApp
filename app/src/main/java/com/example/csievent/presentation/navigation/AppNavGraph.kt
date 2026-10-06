package com.example.csievent.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.csievent.presentation.auth.LoginScreen
import com.example.csievent.presentation.auth.RegisterScreen
import com.example.csievent.presentation.judge.JudgeHomeScreen
import com.example.csievent.presentation.judge.scoring.JudgeScoringScreen
import com.example.csievent.presentation.judge.teams.JudgeTeamsScreen
import com.example.csievent.presentation.organizer.CreateEventScreen
import com.example.csievent.presentation.organizer.OrganizerHomeScreen
import com.example.csievent.presentation.organizer.assign.AssignJudgeScreen
import com.example.csievent.presentation.organizer.criteria.CriteriaScreen
import com.example.csievent.presentation.organizer.events.OrganizerEventScreen
import com.example.csievent.presentation.organizer.roles.RoleManagementScreen
import com.example.csievent.presentation.profile.ProfileScreen
import com.example.csievent.presentation.results.ResultsScreen
import com.example.csievent.presentation.splash.SplashScreen
import com.example.csievent.presentation.student.StudentHomeScreen
import com.example.csievent.presentation.student.teams.StudentEventScreen


@Composable
fun AppNavGraph(navController: NavHostController) {

    // Short cross-fades: cheap to render, no sliding layout work
    NavHost(
        navController      = navController,
        startDestination   = Routes.SPLASH,
        enterTransition    = { fadeIn(tween(180)) },
        exitTransition     = { fadeOut(tween(120)) },
        popEnterTransition = { fadeIn(tween(180)) },
        popExitTransition  = { fadeOut(tween(120)) }
    ) {

        // ── Auth ────────────────────────────────────────────────────────
        composable(
            Routes.SPLASH,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            SplashScreen(navController = navController)
        }
        composable(Routes.LOGIN) { LoginScreen(navController = navController) }
        composable(Routes.REGISTER) { RegisterScreen(navController = navController) }

        // ── Any role ────────────────────────────────────────────────────
        composable(Routes.PROFILE) { ProfileScreen(navController = navController) }
        eventRoute(Routes.RESULTS) { eventId, _ ->
            ResultsScreen(eventId = eventId, onBack = { navController.popBackStack() })
        }

        // ── Student ─────────────────────────────────────────────────────
        composable(Routes.STUDENT_HOME) { StudentHomeScreen(navController = navController) }
        eventRoute(Routes.STUDENT_EVENT) { eventId, _ ->
            StudentEventScreen(eventId = eventId, navController = navController)
        }

        // ── Judge ───────────────────────────────────────────────────────
        composable(Routes.JUDGE_HOME) { JudgeHomeScreen(navController = navController) }
        eventRoute(Routes.JUDGE_TEAMS) { eventId, _ ->
            JudgeTeamsScreen(eventId = eventId, navController = navController)
        }
        composable("${Routes.JUDGE_SCORING}/{eventId}/{teamId}") { back ->
            val eventId = back.longArg("eventId") ?: return@composable
            val teamId = back.longArg("teamId") ?: return@composable
            JudgeScoringScreen(eventId = eventId, teamId = teamId, navController = navController)
        }

        // ── Organizer ───────────────────────────────────────────────────
        composable(Routes.ORGANIZER_HOME) { OrganizerHomeScreen(navController = navController) }
        composable(Routes.CREATE_EVENT) { CreateEventScreen(navController = navController) }
        composable(Routes.ROLE_MANAGEMENT) { RoleManagementScreen(navController = navController) }
        eventRoute(Routes.ORGANIZER_EVENT) { eventId, _ ->
            OrganizerEventScreen(eventId = eventId, navController = navController)
        }
        eventRoute(Routes.CRITERIA) { eventId, _ ->
            CriteriaScreen(eventId = eventId, navController = navController)
        }
        eventRoute(Routes.ASSIGN_JUDGE) { eventId, _ ->
            AssignJudgeScreen(eventId = eventId, navController = navController)
        }
    }
}

/** A "route/{eventId}" destination; skips rendering if the id is malformed. */
private fun NavGraphBuilder.eventRoute(
    route: String,
    content: @Composable (eventId: Long, entry: NavBackStackEntry) -> Unit
) {
    composable("$route/{eventId}") { back ->
        val eventId = back.longArg("eventId") ?: return@composable
        content(eventId, back)
    }
}

private fun NavBackStackEntry.longArg(name: String): Long? =
    arguments?.getString(name)?.toLongOrNull()
