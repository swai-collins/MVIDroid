package com.arkivanov.mvikotlin.sample.mpp.todolist

import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListStore.Intent
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListView.Event

internal object EventToIntentMapper : (Event) -> Intent {

    override fun invoke(event: Event): Intent =
        when (event) {
            is Event.ItemClicked -> TODO()
            is Event.ItemDoneClicked -> Intent.ToggleDone(event.id)
            is Event.ItemDeleteClicked -> Intent.Delete(event.id)
        }
}
