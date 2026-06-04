package com.example.touristguide.ui.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.touristguide.TouristGuideApp

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val feed = container.activityLogRepository.observeFeed().asLiveData()
}
