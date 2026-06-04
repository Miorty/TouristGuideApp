package com.example.touristguide.ui.moderation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.touristguide.TouristGuideApp
import com.example.touristguide.data.local.entity.ModerationQueueEntity
import com.example.touristguide.data.repository.RepositoryResult
import kotlinx.coroutines.launch

class CheckPlaceViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val pendingItems = container.moderationRepository.observePending().asLiveData()

    private val _decisionResult = MutableLiveData<RepositoryResult<ModerationQueueEntity>>()
    val decisionResult: LiveData<RepositoryResult<ModerationQueueEntity>> = _decisionResult

    fun approve(queueId: Long, comment: String = "") {
        viewModelScope.launch {
            _decisionResult.value = container.moderationRepository.approve(
                queueId = queueId,
                moderatorId = container.sessionManager.currentUserIdValue(),
                comment = comment
            )
        }
    }

    fun reject(queueId: Long, comment: String) {
        viewModelScope.launch {
            _decisionResult.value = container.moderationRepository.reject(
                queueId = queueId,
                moderatorId = container.sessionManager.currentUserIdValue(),
                comment = comment
            )
        }
    }
}
