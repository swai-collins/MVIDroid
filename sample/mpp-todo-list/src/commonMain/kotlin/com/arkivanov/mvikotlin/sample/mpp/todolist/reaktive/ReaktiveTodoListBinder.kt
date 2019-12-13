package com.arkivanov.mvikotlin.sample.mpp.todolist.reaktive

import com.arkivanov.mvikotlin.base.bind.Binder
import com.arkivanov.mvikotlin.base.observable.MviDisposable
import com.arkivanov.mvikotlin.base.store.MviStoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.bind
import com.arkivanov.mvikotlin.extensions.reaktive.events
import com.arkivanov.mvikotlin.extensions.reaktive.states
import com.arkivanov.mvikotlin.sample.mpp.commonapi.TodoItemQueries
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListStore
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListView
import com.arkivanov.mvikotlin.sample.mpp.todolist.eventToIntent
import com.arkivanov.mvikotlin.sample.mpp.todolist.stateToViewModel
import com.badoo.reaktive.observable.map

class ReaktiveTodoListBinder private constructor(
    private val store: TodoListStore
) : MviDisposable by store {

    constructor(
        storeFactory: MviStoreFactory,
        queries: TodoItemQueries
    ) : this(ReaktiveTodoListStoreFactory(storeFactory = storeFactory, queries = queries).create())

    fun bind(view: TodoListView): Binder =
        bind {
            store.states.map(::stateToViewModel) with view
            view.events.map(::eventToIntent) with store
        }
}
