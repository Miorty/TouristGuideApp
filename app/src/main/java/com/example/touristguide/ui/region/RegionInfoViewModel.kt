package com.example.touristguide.ui.region

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegionInfoViewModel : ViewModel() {
    private val _screenTitle = MutableLiveData("")
    val screenTitle: LiveData<String> = _screenTitle
}
