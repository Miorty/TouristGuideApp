package com.example.touristguide.ui.photo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.touristguide.TouristGuideApp
import com.example.touristguide.data.local.entity.PlacePhotoEntity
import com.example.touristguide.data.repository.RepositoryResult
import kotlinx.coroutines.launch

class AddPhotoViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    private val _result = MutableLiveData<RepositoryResult<PlacePhotoEntity>>()
    val result: LiveData<RepositoryResult<PlacePhotoEntity>> = _result

    fun addPhoto(placeId: Long, filePath: String?) {
        viewModelScope.launch {
            _result.value = container.photoRepository.addPhoto(
                placeId = placeId,
                userId = container.sessionManager.currentUserIdValue(),
                filePath = filePath
            )
        }
    }
}
