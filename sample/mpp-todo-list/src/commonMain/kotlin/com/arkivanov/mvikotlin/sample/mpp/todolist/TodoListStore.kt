package com.arkivanov.mvikotlin.sample.mpp.todolist

import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListStore.Intent
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListStore.State

internal interface TodoListStore : MviStore<State, Intent, Nothing> {

    data class State(
        val items: List<TodoItem> = emptyList()
    )

    sealed class Intent {
        data class Delete(val id: Long) : Intent()
        data class ToggleDone(val id: Long) : Intent()
    }
}
