package com.example.touristguide.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.touristguide.TouristGuideApp
import com.example.touristguide.data.local.entity.UserTaskEntity
import com.example.touristguide.data.repository.RepositoryResult
import kotlinx.coroutines.launch

class WeeklyTaskViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TouristGuideApp).appContainer

    val weeklyTasks = container.taskRepository.observeWeeklyTasks().asLiveData()

    private val _taskResult = MutableLiveData<RepositoryResult<UserTaskEntity>>()
    val taskResult: LiveData<RepositoryResult<UserTaskEntity>> = _taskResult

    fun ensureTasks() {
        viewModelScope.launch {
            container.taskRepository.ensureWeeklyTasksForUser(container.sessionManager.currentUserIdValue())
        }
    }

    fun addProgress(taskId: Long) {
        viewModelScope.launch {
            _taskResult.value = container.taskRepository.progressTask(container.sessionManager.currentUserIdValue(), taskId)
        }
    }
}
