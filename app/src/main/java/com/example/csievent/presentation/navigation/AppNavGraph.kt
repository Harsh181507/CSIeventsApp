package com.example.csievent.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.csievent.presentation.auth.LoginScreen
import com.example.csievent.presentation.auth.RegisterScreen
import com.example.csievent.presentation.judge.JudgeDashboardScreen
import com.example.csievent.presentation.judge.criteria.JudgeCriteriaScreen
import com.example.csievent.presentation.judge.teams.JudgeTeamsScreen
import com.example.csievent.presentation.organizer.CreateEventScreen
import com.example.csievent.presentation.organizer.OrganizerDashboardScreen
import com.example.csievent.presentation.organizer.assign.AssignJudgeScreen
import com.example.csievent.presentation.organizer.events.OrganizerEventDetailsScreen
import com.example.csievent.presentation.organizer.leaderboard.LeaderboardScreen
import com.example.csievent.presentation.organizer.roles.RoleManagementScreen
import com.example.csievent.presentation.splash.SplashScreen
import com.example.csievent.presentation.student.StudentDashboardScreen
import com.example.csievent.presentation.student.teams.StudentTeamsScreen


@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController    = navController,
        startDestination = Routes.SPLASH
    ) {


        composable(Routes.SPLASH) {
            SplashScreen(navController = navController)
        }

        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(navController = navController)
        }

        composable(Routes.ORGANIZER_DASHBOARD) {
            OrganizerDashboardScreen(navController = navController)
        }

        composable(Routes.CREATE_EVENT) {
            CreateEventScreen(navController = navController)
        }

        composable("${Routes.ORGANIZER_EVENT_DETAILS}/{eventId}") { back ->
            val eventId = back.arguments?.getString("eventId")?.toLongOrNull()
                ?: return@composable
            OrganizerEventDetailsScreen(
                eventId       = eventId,
                navController = navController
            )
        }

        composable("${Routes.ASSIGN_JUDGE}/{eventId}") { back ->
            val eventId = back.arguments?.getString("eventId")?.toLongOrNull()
                ?: return@composable
            AssignJudgeScreen(
                eventId       = eventId,
                navController = navController
            )
        }

        composable("${Routes.LEADERBOARD}/{eventId}") { back ->
            val eventId = back.arguments?.getString("eventId")?.toLongOrNull()
                ?: return@composable
            LeaderboardScreen(eventId = eventId)
        }

        // NEW — Role Management screen
        composable(Routes.ROLE_MANAGEMENT) {
            RoleManagementScreen(navController = navController)
        }

        composable(Routes.JUDGE_DASHBOARD) {
            JudgeDashboardScreen(navController = navController)
        }

        composable("${Routes.JUDGE_EVENT_TEAMS}/{eventId}") { back ->
            val eventId = back.arguments?.getString("eventId")?.toLongOrNull()
                ?: return@composable
            JudgeTeamsScreen(
                eventId       = eventId,
                navController = navController
            )
        }

        // Both eventId and teamId required — eventId loads criteria,
        // teamId identifies which team is being scored
        composable("${Routes.JUDGE_CRITERIA}/{eventId}/{teamId}") { back ->
            val eventId = back.arguments?.getString("eventId")?.toLongOrNull()
                ?: return@composable
            val teamId = back.arguments?.getString("teamId")?.toLongOrNull()
                ?: return@composable
            JudgeCriteriaScreen(
                eventId       = eventId,
                teamId        = teamId,
                navController = navController
            )
        }

        composable(Routes.STUDENT_DASHBOARD) {
            StudentDashboardScreen(navController = navController)
        }

        composable("${Routes.STUDENT_TEAMS}/{eventId}") { back ->
            val eventId = back.arguments?.getString("eventId")?.toLongOrNull()
                ?: return@composable
            StudentTeamsScreen(
                eventId       = eventId,
                navController = navController
            )
        }
    }
}