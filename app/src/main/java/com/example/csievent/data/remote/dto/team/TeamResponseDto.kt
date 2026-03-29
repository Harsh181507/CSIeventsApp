package com.example.csievent.data.remote.dto.team


data class TeamResponseDto(

    /** Unique team ID from the database. */
    val id: Long,

    /** Display name of the team. */
    val teamName: String,

    /** The event this team is registered for. */
    val eventId: Long,

    /** User ID of the team leader. */
    val leaderId: Long? = null,

    /** Display name of the team leader. */
    val leaderName: String? = null,

    /** List of all member display names in this team. */
    val members: List<String> = emptyList(),

    /**
     * The unique join code for this team (e.g. "A3F9B2C1").
     * Only shown to the leader in the UI.
     * Null for teams created before this feature was added.
     */
    val joinCode: String? = null
)