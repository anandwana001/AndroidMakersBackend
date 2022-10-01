package dev.johnoreilly.confetti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.*
import kotlinx.datetime.LocalDate


class ConfettiViewModel(private val repository: ConfettiRepository): ViewModel() {
    val enabledLanguages: Flow<Set<String>> = repository.enabledLanguages

    val speakers = repository.speakers
    val rooms = repository.rooms

    var selectedDateIndex = MutableStateFlow<Int>(0)

    val uiState: StateFlow<SessionsUiState> =
        combine(
            repository.confDates,
            repository.sessionsMap,
            selectedDateIndex
        ) { confDates, sessionsMap, selectedDateIndex ->

            val selectedDate = confDates[selectedDateIndex]
            val sessions = sessionsMap[selectedDate] ?: emptyList()
            SessionsUiState.Success(confDates, selectedDateIndex, sessions)

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionsUiState.Loading)


    fun switchTab(newIndex: Int) {
        selectedDateIndex.value = newIndex
    }

    fun onLanguageChecked(language: String, checked: Boolean) {
        repository.updateEnableLanguageSetting(language, checked)
    }

    suspend fun getSession(sessionId: String): SessionDetails? {
        return repository.getSession(sessionId)
    }

    fun getSessionTime(session: SessionDetails): String {
        return repository.getSessionTime(session)
    }
}

sealed interface SessionsUiState {
    object Loading : SessionsUiState

    data class Success(
        val confDates: List<LocalDate>,
        val selectedDateIndex: Int,
        val sessions: List<SessionDetails>
    ) : SessionsUiState
}
