package com.arkivanov.mvikotlin.sample.mpp.todolist.reaktive

import com.arkivanov.mvikotlin.base.store.MviStoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.Binder
import com.arkivanov.mvikotlin.extensions.reaktive.bind
import com.arkivanov.mvikotlin.extensions.reaktive.events
import com.arkivanov.mvikotlin.extensions.reaktive.states
import com.arkivanov.mvikotlin.sample.mpp.commonapi.TodoItemQueries
import com.arkivanov.mvikotlin.sample.mpp.todolist.EventToIntentMapper
import com.arkivanov.mvikotlin.sample.mpp.todolist.StateToViewModelMapper
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListView
import com.badoo.reaktive.observable.map

object ReaktiveTodoListBinder {

    fun bind(
        storeFactory: MviStoreFactory,
        queries: TodoItemQueries,
        view: TodoListView
    ): Binder =
        bind {
            val store = ReaktiveTodoListStoreFactory(storeFactory = storeFactory, queries = queries).create()
            store.states.map(StateToViewModelMapper) with view
            view.events.map(EventToIntentMapper) with store
        }
}
