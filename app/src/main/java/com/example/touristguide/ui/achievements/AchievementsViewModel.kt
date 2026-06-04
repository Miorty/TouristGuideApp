package com.example.touristguide.ui.achievements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.example.touristguide.TouristGuideApp

class AchievementsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val allAchievements = container.achievementRepository.observeAll().asLiveData()
    val userAchievements = container.sessionManager.currentUserId.asLiveData().switchMap { userId ->
        container.achievementRepository.observeUserAchievements(userId).asLiveData()
    }
}
