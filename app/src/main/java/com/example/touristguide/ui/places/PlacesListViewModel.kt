package com.example.touristguide.ui.places

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.example.touristguide.TouristGuideApp
import com.example.touristguide.data.local.entity.CategoryEntity
import com.example.touristguide.data.local.entity.PlaceEntity

class PlacesListViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val categories = container.categoryRepository.observeCategories().asLiveData()
    private val query = MutableLiveData("")
    private val selectedCategoryId = MutableLiveData<Long?>(null)

    val places = query.switchMap { searchText ->
        selectedCategoryId.switchMap { categoryId ->
            when {
                searchText.isNotBlank() -> container.placeRepository.searchPlaces(searchText).asLiveData()
                categoryId != null -> container.placeRepository.observeByCategory(categoryId).asLiveData()
                else -> container.placeRepository.observePublishedPlaces().asLiveData()
            }
        }
    }

    fun search(value: String) {
        query.value = value
    }

    fun selectCategory(category: CategoryEntity?) {
        selectedCategoryId.value = category?.id
    }
}
