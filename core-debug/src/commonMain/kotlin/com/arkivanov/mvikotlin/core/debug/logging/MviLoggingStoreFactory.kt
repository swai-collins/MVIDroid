package com.arkivanov.mvikotlin.core.debug.logging

import com.arkivanov.mvikotlin.base.store.MviExecutor
import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.store.MviStoreFactory

/**
 * An implementation of [MviStoreFactory] that wraps another Store factory and provides logging
 *
 * @param delegate an instance of another factory that will be wrapped by this factory
 * @param logger A logger that can be used to implement custom logging. By default [MviDefaultLogger] is used.
 * @param mode logging mode, see [MviLoggingStoreFactory.Mode] for more information
 */
class MviLoggingStoreFactory(
    private val delegate: MviStoreFactory,
    private val logger: (String) -> Unit = mviDefaultLogger,
    var mode: MviLoggingMode = MviLoggingMode.MEDIUM
) : MviStoreFactory {

    override fun <State : Any, Intent : Any, Label : Any, Result : Any> create(
        name: String,
        initialState: State,
        executorFactory: () -> MviExecutor<State, Intent, Result, Label>,
        reducer: MviReducer<State, Result>
    ): MviStore<State, Intent, Label> {
        logger(mode) { "$name: created" }

        val delegateStore =
            delegate.create(
                name = name,
                initialState = initialState,
                executorFactory = { executorFactory().wrap(name) },
                reducer = reducer.wrap(name)
            )

        return MviLoggingStore(
            delegate = delegateStore,
            logger = logger,
            loggingMode = mode,
            name = name
        )
    }

    private fun <State : Any, Action : Any, Result : Any, Label : Any> MviExecutor<State, Action, Result, Label>.wrap(
        storeName: String
    ): MviExecutor<State, Action, Result, Label> =
        MviLoggingExecutor(
            delegate = this,
            logger = logger,
            loggingMode = mode,
            storeName = storeName
        )

    private fun <State : Any, Result : Any> MviReducer<State, Result>.wrap(storeName: String): MviReducer<State, Result> =
        MviLoggingReducer(
            delegate = this,
            logger = logger,
            loggingMode = mode,
            storeName = storeName
        )
}
