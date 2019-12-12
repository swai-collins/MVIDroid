package com.arkivanov.mvikotlin.sample.mpp.todolist.reaktive

import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.store.MviStoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.MviReaktiveExecutor
import com.arkivanov.mvikotlin.sample.mpp.common.asObservable
import com.arkivanov.mvikotlin.sample.mpp.commonapi.TodoItemQueries
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoItem
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListStore
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListStore.Intent
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListStore.State
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.completable.observeOn
import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.completable.subscribeOn
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.single.flatMapObservable
import com.badoo.reaktive.single.singleFromFunction
import com.badoo.reaktive.single.subscribeOn
import com.squareup.sqldelight.Query

internal class ReaktiveTodoListStoreFactory(
    private val storeFactory: MviStoreFactory,
    private val queries: TodoItemQueries
) {

    fun create(): TodoListStore =
        object : TodoListStore, MviStore<State, Intent, Nothing> by storeFactory.create(
            name = "ReaktiveTodoListStore",
            initialState = State(),
            executorFactory = ::Executor,
            reducer = Reducer
        ) {
        }

    private sealed class Result {
        data class Items(val items: List<TodoItem>) : Result()
    }

    private inner class Executor : MviReaktiveExecutor<State, Intent, Result, Nothing>() {
        override fun bootstrap() {
            singleFromFunction { queries.selectAll(::TodoItem) }
                .subscribeOn(ioScheduler)
                .flatMapObservable { it.asObservable() }
                .map(Query<TodoItem>::executeAsList)
                .map(Result::Items)
                .observeOn(mainScheduler)
                .subscribe(isThreadLocal = true, onNext = ::dispatch)
                .scope()
        }

        override fun execute(intent: Intent) {
            when (intent) {
                is Intent.Delete -> delete(intent.id)
                is Intent.ToggleDone -> toggleDone(intent.id)
            }.let { }
        }

        private fun delete(id: Long) {
            completableFromFunction { queries.delete(id) }
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe()
                .scope()
        }

        private fun toggleDone(id: Long) {
            completableFromFunction {
                queries.toggleDone(id)
            }
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe()
                .scope()
        }
    }

    private object Reducer : MviReducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.Items -> copy(items = result.items)
            }
    }
}
