package com.arkivanov.mvikotlin.core.debug.logging

import com.arkivanov.mvikotlin.base.store.MviExecutor
import com.arkivanov.mvikotlin.base.store.MviEventType

internal class MviLoggingExecutor<in State : Any, in Action : Any, out Result : Any, out Label : Any>(
    private val delegate: MviExecutor<State, Action, Result, Label>,
    private val logger: (String) -> Unit,
    private val loggingMode: MviLoggingMode,
    private val storeName: String
) : MviExecutor<State, Action, Result, Label> by delegate {

    override fun init(stateSupplier: () -> State, resultConsumer: (Result) -> Unit, labelConsumer: (Label) -> Unit) {
        delegate.init(
            stateSupplier = stateSupplier,
            resultConsumer = {
                logger(loggingMode, storeName, MviEventType.RESULT, it)
                resultConsumer(it)
            },
            labelConsumer = {
                logger(loggingMode, storeName, MviEventType.LABEL, it)
                labelConsumer(it)
            }
        )
    }
}
