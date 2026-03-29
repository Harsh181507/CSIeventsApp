package com.example.csievent.data.remote.dto.score

data class SubmitScoreRequestDto(
    val teamId: Long,
    val criteriaId: Long,
    val scoreValue: Int
)
