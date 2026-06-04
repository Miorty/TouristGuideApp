package com.example.touristguide.ui.routes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.example.touristguide.TouristGuideApp

class MyRoutesViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val routes = container.sessionManager.currentUserId.asLiveData().switchMap { userId ->
        container.routeRepository.observeUserRoutes(userId).asLiveData()
    }
}
