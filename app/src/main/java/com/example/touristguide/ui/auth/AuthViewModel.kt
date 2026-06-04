package com.example.touristguide.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.touristguide.TouristGuideApp
import com.example.touristguide.data.local.entity.UserEntity
import com.example.touristguide.data.repository.RepositoryResult
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    private val _authResult = MutableLiveData<RepositoryResult<UserEntity>>()
    val authResult: LiveData<RepositoryResult<UserEntity>> = _authResult

    fun loginAsGuest() {
        viewModelScope.launch {
            val userId = container.authRepository.loginAsGuest()
            container.sessionManager.setCurrentUser(userId)
            val user = container.userRepository.getUser(userId)
            _authResult.value = user?.let { RepositoryResult.success(it) }
                ?: RepositoryResult.failure("Не удалось войти гостем")
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = container.authRepository.loginByEmail(email, password)
            result.data?.let { container.sessionManager.setCurrentUser(it.id) }
            _authResult.value = result
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            val result = container.authRepository.register(username, email, password)
            result.data?.let { container.sessionManager.setCurrentUser(it.id) }
            _authResult.value = result
        }
    }
}
