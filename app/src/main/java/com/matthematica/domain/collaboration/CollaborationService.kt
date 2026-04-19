package com.matthematica.domain.collaboration

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollaborationService @Inject constructor() {

    private val _sessionState = MutableStateFlow(CollaborationSessionState())
    val sessionState: StateFlow<CollaborationSessionState> = _sessionState.asStateFlow()

    fun createSession(sessionId: String, displayName: String): Result<String> {
        return try {
            require(sessionId.isNotBlank()) { "Session ID cannot be empty" }
            require(displayName.isNotBlank()) { "Display name cannot be empty" }

            _sessionState.value = CollaborationSessionState(
                sessionId = sessionId.trim(),
                participants = listOf(displayName.trim()),
                sharedExpression = ""
            )
            Result.success("Created session ${sessionId.trim()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun joinSession(displayName: String): Result<String> {
        return try {
            require(displayName.isNotBlank()) { "Display name cannot be empty" }
            val state = _sessionState.value
            require(state.sessionId.isNotBlank()) { "Create a session first" }

            if (!state.participants.contains(displayName.trim())) {
                _sessionState.value = state.copy(participants = state.participants + displayName.trim())
            }
            Result.success("${displayName.trim()} joined ${state.sessionId}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateSharedExpression(expression: String) {
        _sessionState.value = _sessionState.value.copy(sharedExpression = expression)
    }
}

data class CollaborationSessionState(
    val sessionId: String = "",
    val participants: List<String> = emptyList(),
    val sharedExpression: String = ""
)

