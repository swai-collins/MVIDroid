package com.arkivanov.mvikotlin.sample.mpp.todolist

import com.arkivanov.mvikotlin.core.view.MviBaseView
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListView.Event
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListView.Model

open class TodoListView : MviBaseView<Model, Event>() {

    data class Model(val items: List<TodoItem>)

    sealed class Event {
        data class ItemClicked(val id: Long) : Event()
        data class ItemDoneClicked(val id: Long) : Event()
        data class ItemDeleteClicked(val id: Long) : Event()
    }
}
