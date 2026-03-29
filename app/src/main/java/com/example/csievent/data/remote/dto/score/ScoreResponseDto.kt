package com.example.csievent.data.remote.dto.score


data class ScoreResponseDto(
    val scoreId:       Long,
    val teamId:        Long,
    val teamName:      String,
    val criteriaId:    Long,
    val criteriaTitle: String,
    val scoreValue:    Int
)