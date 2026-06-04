package com.example.touristguide.ui.place_detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.touristguide.TouristGuideApp
import com.example.touristguide.data.local.entity.PlaceEntity
import com.example.touristguide.data.repository.RepositoryResult
import kotlinx.coroutines.launch

class PlaceDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer
    private val placeId = MutableLiveData<Long>()

    val place = placeId.switchMap { id -> container.placeRepository.observePlace(id).asLiveData() }
    val reviews = placeId.switchMap { id -> container.reviewRepository.observePlaceReviews(id).asLiveData() }
    val photos = placeId.switchMap { id -> container.photoRepository.observePlacePhotos(id).asLiveData() }

    private val _ratingResult = MutableLiveData<RepositoryResult<Double>>()
    val ratingResult: LiveData<RepositoryResult<Double>> = _ratingResult

    private val _favoriteResult = MutableLiveData<RepositoryResult<Boolean>>()
    val favoriteResult: LiveData<RepositoryResult<Boolean>> = _favoriteResult

    fun load(id: Long) {
        placeId.value = id
    }

    fun rate(value: Int) {
        val id = placeId.value ?: return
        viewModelScope.launch {
            _ratingResult.value = container.ratingRepository.ratePlace(
                com.example.touristguide.data.local.entity.RatingEntity(
                    placeId = id,
                    userId = container.sessionManager.currentUserIdValue(),
                    value = value
                )
            )
        }
    }

    fun toggleFavorite() {
        val id = placeId.value ?: return
        viewModelScope.launch {
            _favoriteResult.value = container.favoriteRepository.toggleFavorite(container.sessionManager.currentUserIdValue(), id)
        }
    }
}
