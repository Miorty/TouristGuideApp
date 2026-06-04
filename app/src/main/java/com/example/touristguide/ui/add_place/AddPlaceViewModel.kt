package com.example.touristguide.ui.add_place

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.touristguide.TouristGuideApp
import com.example.touristguide.data.local.entity.PlaceEntity
import com.example.touristguide.data.repository.RepositoryResult
import kotlinx.coroutines.launch

class AddPlaceViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val categories = container.categoryRepository.observeCategories().asLiveData()

    private val _saveResult = MutableLiveData<RepositoryResult<PlaceEntity>>()
    val saveResult: LiveData<RepositoryResult<PlaceEntity>> = _saveResult

    fun savePlace(
        title: String,
        description: String,
        categoryId: Long,
        latitude: Double?,
        longitude: Double?,
        address: String,
        photoPath: String?
    ) {
        viewModelScope.launch {
            if (latitude == null || longitude == null) {
                _saveResult.value = RepositoryResult.failure("Выберите точку на карте")
                return@launch
            }

            val place = PlaceEntity(
                categoryId = categoryId,
                authorId = container.sessionManager.currentUserIdValue(),
                title = title.trim(),
                description = description.trim(),
                latitude = latitude,
                longitude = longitude,
                address = address.trim()
            )
            val placeResult = container.placeRepository.addUserPlace(place)
            val savedPlace = placeResult.data
            if (placeResult.isSuccess && savedPlace != null && !photoPath.isNullOrBlank()) {
                container.photoRepository.addPhoto(
                    placeId = savedPlace.id,
                    userId = container.sessionManager.currentUserIdValue(),
                    filePath = photoPath
                )
            }
            _saveResult.value = placeResult
        }
    }
}
