package com.example.touristguide.core.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionManager {
    private val _currentUserId = MutableStateFlow(1L)
    val currentUserId: StateFlow<Long> = _currentUserId

    fun setCurrentUser(userId: Long) {
        _currentUserId.value = userId
    }

    fun currentUserIdValue(): Long = _currentUserId.value
}
