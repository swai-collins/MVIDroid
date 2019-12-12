package com.arkivanov.mvikotlin.core.store

import com.arkivanov.mvikotlin.base.store.MviExecutor
import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.store.MviStoreFactory

object MviDefaultStoreFactory : MviStoreFactory {

    override fun <State : Any, Intent : Any, Label : Any, Result : Any> create(
        name: String,
        initialState: State,
        executorFactory: () -> MviExecutor<State, Intent, Result, Label>,
        reducer: MviReducer<State, Result>
    ): MviStore<State, Intent, Label> =
        MviDefaultStore(
            initialState = initialState,
            executor = executorFactory(),
            reducer = reducer
        )
}
