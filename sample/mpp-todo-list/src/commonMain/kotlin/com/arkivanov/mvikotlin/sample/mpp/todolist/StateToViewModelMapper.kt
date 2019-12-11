package com.arkivanov.mvikotlin.sample.mpp.todolist

import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListStore.State
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListView.Model

internal object StateToViewModelMapper : (State) -> Model {

    override fun invoke(state: State): Model =
        Model(items = state.items)
}
