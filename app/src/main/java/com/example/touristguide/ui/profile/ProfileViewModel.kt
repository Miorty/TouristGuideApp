package com.example.touristguide.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.example.touristguide.TouristGuideApp

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val profile = container.sessionManager.currentUserId.asLiveData().switchMap { userId ->
        container.userRepository.observeUser(userId).asLiveData()
    }

    val achievements = container.sessionManager.currentUserId.asLiveData().switchMap { userId ->
        container.achievementRepository.observeUserAchievements(userId).asLiveData()
    }

    val tasks = container.sessionManager.currentUserId.asLiveData().switchMap { userId ->
        container.taskRepository.observeUserTasks(userId).asLiveData()
    }
}
