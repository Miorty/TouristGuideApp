package com.example.touristguide.ui.routes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.touristguide.TouristGuideApp

class RoutesViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val routes = container.routeRepository.observePublishedRoutes().asLiveData()
}
