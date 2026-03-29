package com.example.csievent.presentation.organizer.criteria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.criteria.CriteriaResponseDto
import com.example.csievent.domain.repository.CriteriaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// =============================================================================
// STATE
// =============================================================================
data class AddCriteriaState(
    val isLoading:      Boolean                   = false,
    val isSubmitting:   Boolean                   = false,
    val criteria:       List<CriteriaResponseDto> = emptyList(),
    val error:          String?                   = null,
    val successMessage: String?                   = null
)

// =============================================================================
// VIEWMODEL
// =============================================================================
@HiltViewModel
class AddCriteriaViewModel @Inject constructor(
    private val criteriaRepository: CriteriaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddCriteriaState())
    val state: StateFlow<AddCriteriaState> = _state.asStateFlow()

    fun loadCriteria(eventId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            criteriaRepository.getCriteriaByEvent(eventId)
                .fold(
                    onSuccess = { list ->
                        _state.update { it.copy(isLoading = false, criteria = list) }
                    },
                    onFailure = { err ->
                        _state.update { it.copy(isLoading = false, error = err.message ?: "Failed to load criteria") }
                    }
                )
        }
    }

    fun addCriteria(eventId: Long, title: String, maxScore: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, successMessage = null) }
            criteriaRepository.createCriteria(eventId, title, maxScore)
                .fold(
                    onSuccess = {
                        _state.update { it.copy(isSubmitting = false,
                            successMessage = "Crystal forged: $title") }
                        loadCriteria(eventId)
                    },
                    onFailure = { err ->
                        _state.update { it.copy(isSubmitting = false,
                            error = err.message ?: "Failed to add criterion") }
                    }
                )
        }
    }

    fun deleteCriteria(criteriaId: Long) {
        viewModelScope.launch {
            criteriaRepository.deleteCriteria(criteriaId)
                .fold(
                    onSuccess = {
                        val updated = _state.value.criteria.filter { c -> c.id != criteriaId }
                        _state.update { it.copy(criteria = updated, successMessage = "Crystal removed") }
                    },
                    onFailure = { err ->
                        _state.update { it.copy(error = err.message ?: "Failed to delete criterion") }
                    }
                )
        }
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}