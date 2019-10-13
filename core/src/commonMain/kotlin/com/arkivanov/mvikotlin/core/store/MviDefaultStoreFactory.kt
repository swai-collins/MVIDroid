package com.arkivanov.mvikotlin.core.store

import com.arkivanov.mvikotlin.base.store.MviBootstrapper
import com.arkivanov.mvikotlin.base.store.MviExecutor
import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.store.MviStoreFactory

object MviDefaultStoreFactory : MviStoreFactory {

    private val bypassReducer =
        object : MviReducer<Any, Any> {
            override fun Any.reduce(result: Any): Any = this
        }

    @Suppress("UNCHECKED_CAST")
    override fun <State : Any, Intent : Any, Label : Any, Action : Any, Result : Any> create(
        initialState: State,
        bootstrapper: MviBootstrapper<Action>?,
        intentToAction: (Intent) -> Action,
        executorFactory: () -> MviExecutor<State, Action, Result, Label>,
        reducer: MviReducer<State, Result>?
    ): MviStore<State, Intent, Label> =
        MviDefaultStore(
            initialState = initialState,
            bootstrapper = bootstrapper,
            intentToAction = intentToAction,
            executor = executorFactory(),
            reducer = reducer ?: (bypassReducer as MviReducer<State, Result>)
        )
}
