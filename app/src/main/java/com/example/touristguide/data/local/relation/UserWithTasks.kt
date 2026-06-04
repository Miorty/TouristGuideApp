package com.example.touristguide.data.local.relation

import com.example.touristguide.data.local.entity.TaskEntity
import com.example.touristguide.data.local.entity.UserEntity

data class UserWithTasks(
    val user: UserEntity,
    val tasks: List<TaskEntity>
)
