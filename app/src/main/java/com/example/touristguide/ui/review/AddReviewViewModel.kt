package com.example.touristguide.ui.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.touristguide.TouristGuideApp
import com.example.touristguide.data.local.entity.ReviewEntity
import com.example.touristguide.data.repository.RepositoryResult
import kotlinx.coroutines.launch

class AddReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    private val _result = MutableLiveData<RepositoryResult<ReviewEntity>>()
    val result: LiveData<RepositoryResult<ReviewEntity>> = _result

    fun addReview(placeId: Long, text: String) {
        viewModelScope.launch {
            _result.value = container.reviewRepository.addReview(
                placeId = placeId,
                userId = container.sessionManager.currentUserIdValue(),
                text = text
            )
        }
    }
}
