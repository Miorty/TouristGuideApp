package com.example.touristguide.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.touristguide.TouristGuideApp

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val places = container.placeRepository.observePublishedPlaces().asLiveData()
    val routes = container.routeRepository.observePublishedRoutes().asLiveData()
    val activity = container.activityLogRepository.observeFeed().asLiveData()
}
