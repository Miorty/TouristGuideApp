package com.example.touristguide.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.touristguide.TouristGuideApp

class PlacesMapViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val places = container.placeRepository.observePublishedPlaces().asLiveData()
}
