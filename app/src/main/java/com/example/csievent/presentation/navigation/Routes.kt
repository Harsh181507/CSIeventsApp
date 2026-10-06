package com.example.csievent.presentation.navigation


object Routes {

    // Auth
    const val SPLASH   = "splash"
    const val LOGIN    = "login"
    const val REGISTER = "register"

    // Any role
    const val PROFILE = "profile"
    const val RESULTS = "results"              // /{eventId}

    // Student
    const val STUDENT_HOME  = "student_home"
    const val STUDENT_EVENT = "student_event"  // /{eventId}

    // Judge
    const val JUDGE_HOME    = "judge_home"
    const val JUDGE_TEAMS   = "judge_teams"    // /{eventId}
    const val JUDGE_SCORING = "judge_scoring"  // /{eventId}/{teamId}

    // Organizer
    const val ORGANIZER_HOME  = "organizer_home"
    const val CREATE_EVENT    = "create_event"
    const val ORGANIZER_EVENT = "organizer_event"  // /{eventId}
    const val CRITERIA        = "criteria"         // /{eventId}
    const val ASSIGN_JUDGE    = "assign_judge"     // /{eventId}
    const val ROLE_MANAGEMENT = "role_management"

    /** Home screen for a role, or null if the app has no screens for it. */
    fun dashboardFor(role: String?): String? = when (role) {
        "STUDENT"   -> STUDENT_HOME
        "JUDGE"     -> JUDGE_HOME
        "ORGANIZER" -> ORGANIZER_HOME
        else        -> null
    }
}
