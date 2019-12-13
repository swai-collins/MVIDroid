package com.arkivanov.mvikotlin.sample.mpp.todolist

import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListStore.State
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListView.Model

internal fun stateToViewModel(state: State): Model =
    Model(items = state.items)
