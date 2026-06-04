package com.example.touristguide.data.repository

data class RepositoryResult<out T>(
    val data: T? = null,
    val error: String? = null
) {
    val isSuccess: Boolean = error == null

    companion object {
        fun <T> success(data: T): RepositoryResult<T> = RepositoryResult(data = data)
        fun failure(message: String): RepositoryResult<Nothing> = RepositoryResult(error = message)
    }
}
