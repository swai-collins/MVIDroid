package com.arkivanov.mvikotlin.sample.mpp

import com.arkivanov.mvikotlin.base.store.component.MviReducer
import com.arkivanov.mvikotlin.core.reaktive.store.MviDefaultStore
import com.arkivanov.mvikotlin.core.reaktive.store.MviBootstrapper
import com.arkivanov.mvikotlin.core.reaktive.store.MviExecutor
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.subscribe

data class State(
    val text: String = ""
)

sealed class Intent

sealed class Action

sealed class Result

sealed class Label

class Bootstrapper : MviBootstrapper<Action> {
    override fun bootstrap(publish: (Action) -> Unit): Disposable? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class Executor: MviExecutor<State, Action, Result, Label>() {
    override fun execute(action: Action): Disposable? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class Reducer : MviReducer<State, Result> {
    override fun State.reduce(result: Result): State {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

fun foo() {
    val store =
        MviDefaultStore<Intent, State, Label, Action, Result>(
            initialState = State(),
            intentToAction = {},
            bootstrapper = Bootstrapper(),
            executor = Executor(),
            reducer = Reducer()
        )

    store.accept()

    store.state
    store.states.subscribe()
    store.labels.subscribe()
}