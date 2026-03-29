package com.example.csievent.presentation.navigation


object Routes {

    // Auth
    const val SPLASH   = "splash"
    const val LOGIN    = "login"
    const val REGISTER = "register"

    // Dashboards
    const val STUDENT_DASHBOARD   = "student_dashboard"
    const val JUDGE_DASHBOARD     = "judge_dashboard"
    const val ORGANIZER_DASHBOARD = "organizer_dashboard"

    // Organizer
    const val CREATE_EVENT            = "create_event"
    const val ORGANIZER_EVENT_DETAILS = "organizer_event_details"
    const val ASSIGN_JUDGE            = "assign_judge"
    const val LEADERBOARD             = "leaderboard"
    const val ROLE_MANAGEMENT         = "role_management"   // ← NEW

    // Student
    const val STUDENT_TEAMS = "student_teams"

    // Judge
    const val JUDGE_EVENT_TEAMS = "judge_event_teams"
    const val JUDGE_CRITERIA    = "judge_criteria"
}