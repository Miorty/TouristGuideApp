package com.example.touristguide.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.example.touristguide.TouristGuideApp

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val notifications = container.sessionManager.currentUserId.asLiveData().switchMap { userId ->
        container.activityLogRepository.observeUserActivity(userId).asLiveData()
    }
}
