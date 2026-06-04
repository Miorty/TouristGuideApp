package com.example.touristguide.ui.routes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.touristguide.TouristGuideApp
import com.example.touristguide.data.local.entity.PlaceEntity
import com.example.touristguide.data.local.entity.RouteEntity
import kotlinx.coroutines.launch

class RouteDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    private val _route = MutableLiveData<RouteEntity?>()
    val route: LiveData<RouteEntity?> = _route

    private val _places = MutableLiveData<List<PlaceEntity>>(emptyList())
    val places: LiveData<List<PlaceEntity>> = _places

    fun load(routeId: Long) {
        viewModelScope.launch {
            _route.value = container.routeRepository.getRoute(routeId)
            _places.value = container.routeRepository.getRoutePlaces(routeId)
        }
    }
}
