package com.example.touristguide.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.touristguide.TouristGuideApp
import com.example.touristguide.data.local.entity.PlaceEntity
import kotlinx.coroutines.launch

class RouteMapViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    private val _routePlaces = MutableLiveData<List<PlaceEntity>>(emptyList())
    val routePlaces: LiveData<List<PlaceEntity>> = _routePlaces

    fun load(routeId: Long) {
        viewModelScope.launch {
            _routePlaces.value = container.routeRepository.getRoutePlaces(routeId)
        }
    }
}
