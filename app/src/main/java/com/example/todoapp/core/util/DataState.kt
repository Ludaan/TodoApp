package com.example.todoapp.core.util

sealed class DataState<out T> {
    data object Loading :
        DataState<Nothing>()
    data class Success<T>(val data: T) : DataState<T>()
    data class Error(val message: String) : DataState<Nothing>()
}