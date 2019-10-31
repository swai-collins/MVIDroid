package com.arkivanov.mvikotlin.core.debug.logging

import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.MviEventType

internal class MviLoggingReducer<State : Any, in Result : Any>(
    private val delegate: MviReducer<State, Result>,
    private val logger: (String) -> Unit,
    private val loggingMode: MviLoggingMode,
    private val storeName: String
) : MviReducer<State, Result> {

    override fun State.reduce(result: Result): State {
        val newState = with(delegate) { reduce(result) }
        logger(loggingMode, storeName, MviEventType.STATE, newState)

        return newState
    }
}
