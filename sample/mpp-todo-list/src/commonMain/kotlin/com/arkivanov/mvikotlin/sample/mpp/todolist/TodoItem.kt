package com.arkivanov.mvikotlin.sample.mpp.todolist

data class TodoItem(
    val id: Long,
    val text: String?,
    val isDone: Boolean
)
