package com.example.touristguide.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.touristguide.TouristGuideApp
import com.example.touristguide.data.local.entity.PlaceEntity
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val favoriteRows = container.sessionManager.currentUserId.asLiveData().switchMap { userId ->
        container.favoriteRepository.observeUserFavorites(userId).asLiveData()
    }

    private val _places = MutableLiveData<List<PlaceEntity>>(emptyList())
    val places: LiveData<List<PlaceEntity>> = _places

    fun refresh() {
        viewModelScope.launch {
            _places.value = container.favoriteRepository.getFavoritePlaces(container.sessionManager.currentUserIdValue())
        }
    }

    fun toggle(placeId: Long) {
        viewModelScope.launch {
            container.favoriteRepository.toggleFavorite(container.sessionManager.currentUserIdValue(), placeId)
            refresh()
        }
    }
}
