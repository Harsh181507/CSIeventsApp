package com.example.csievent.presentation.organizer.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.criteria.CriteriaResponseDto
import com.example.csievent.domain.repository.CriteriaRepository
import com.example.csievent.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrganizerEventDetailsState(
    val isLoading: Boolean = false,
    val criteria: List<CriteriaResponseDto> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class OrganizerEventDetailsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val criteriaRepository: CriteriaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OrganizerEventDetailsState())
    val state: StateFlow<OrganizerEventDetailsState> = _state

    fun lockScoring(eventId: Long) {
        viewModelScope.launch {

            _state.value = _state.value.copy(isLoading = true)

            val result = eventRepository.lockScoring(eventId)

            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        successMessage = "Scoring Locked Successfully 🔒"
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = it.message
                    )
                }
            )
        }
    }

    fun fetchCriteria(eventId: Long) {
        viewModelScope.launch {

            _state.value = _state.value.copy(isLoading = true)

            criteriaRepository.getCriteriaByEvent(eventId)
                .fold(
                    onSuccess = { list ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            criteria = list
                        )
                    },
                    onFailure = {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = it.message
                        )
                    }
                )
        }
    }

    fun createCriteria(
        eventId: Long,
        title: String,
        maxScore: Int
    ) {
        viewModelScope.launch {

            _state.value = _state.value.copy(isLoading = true)

            criteriaRepository.createCriteria(eventId, title, maxScore)
                .fold(
                    onSuccess = {
                        _state.value = _state.value.copy(
                            successMessage = "Criteria added successfully"
                        )
                        fetchCriteria(eventId)
                    },
                    onFailure = {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = it.message
                        )
                    }
                )
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(
            error = null,
            successMessage = null
        )
    }
}
