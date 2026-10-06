package com.example.csievent.presentation.organizer.criteria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.criteria.CriteriaResponseDto
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.domain.repository.CriteriaRepository
import com.example.csievent.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CriteriaState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val event: EventResponseDto? = null,
    val criteria: List<CriteriaResponseDto> = emptyList(),
    val error: String? = null,
    val message: String? = null,
    /** Bumped after a successful add so the form clears. */
    val addedCount: Int = 0
)

@HiltViewModel
class CriteriaViewModel @Inject constructor(
    private val criteriaRepository: CriteriaRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CriteriaState())
    val state: StateFlow<CriteriaState> = _state.asStateFlow()

    fun load(eventId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val eventD = async { eventRepository.getEvent(eventId) }
            val criteriaD = async { criteriaRepository.getCriteriaByEvent(eventId) }
            val criteria = criteriaD.await()
            val event = eventD.await()

            _state.update {
                it.copy(
                    isLoading = false,
                    event = event.getOrNull() ?: it.event,
                    criteria = criteria.getOrDefault(it.criteria),
                    error = criteria.exceptionOrNull()?.message
                )
            }
        }
    }

    fun add(eventId: Long, title: String, maxScore: Int) {
        if (_state.value.isSubmitting) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            criteriaRepository.createCriteria(eventId, title.trim(), maxScore)
                .onSuccess { created ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            criteria = it.criteria + created,
                            addedCount = it.addedCount + 1,
                            message = "Added \"${created.title}\""
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false, message = error.message ?: "Couldn't add criterion") }
                }
        }
    }

    fun delete(criteriaId: Long) {
        viewModelScope.launch {
            criteriaRepository.deleteCriteria(criteriaId)
                .onSuccess {
                    _state.update { s ->
                        s.copy(criteria = s.criteria.filterNot { it.id == criteriaId }, message = "Criterion removed")
                    }
                }
                .onFailure { error -> _state.update { it.copy(message = error.message ?: "Couldn't remove criterion") } }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}
