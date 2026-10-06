package com.example.csievent.data.remote.dto.score

/** All of a judge's scores for one team, saved together (all or nothing). */
data class BatchScoreRequestDto(
    val teamId: Long,
    val scores: List<Entry>
) {
    data class Entry(
        val criteriaId: Long,
        val scoreValue: Int
    )
}
