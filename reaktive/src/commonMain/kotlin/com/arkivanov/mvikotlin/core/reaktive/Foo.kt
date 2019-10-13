package com.arkivanov.mvikotlin.core.reaktive

import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.store.MviStoreFactory
import com.arkivanov.mvikotlin.core.reaktive.store.MviReaktiveBootstrapper
import com.arkivanov.mvikotlin.core.reaktive.store.MviReaktiveExecutor
import com.badoo.reaktive.disposable.Disposable

sealed class Intent

class State

sealed class Label

sealed class Action {
    class ExecuteIntent(val intent: Intent) : Action()
}

sealed class Result

class Bootstrapper : MviReaktiveBootstrapper<Action>() {
    override fun execute(dispatch: (Action) -> Unit): Disposable? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class Executor : MviReaktiveExecutor<State, Action, Result, Label>() {
    override fun execute(action: Action): Disposable? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

object Reducer : MviReducer<State, Result> {
    override fun State.reduce(result: Result): State {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

fun foo(factory: MviStoreFactory) {
    val store: MviStore<State, Intent, Label> =
        factory.create(
            initialState = State(),
            bootstrapper = Bootstrapper(),
            intentToAction = Action::ExecuteIntent,
            executorFactory = ::Executor,
            reducer = Reducer
        )
}
