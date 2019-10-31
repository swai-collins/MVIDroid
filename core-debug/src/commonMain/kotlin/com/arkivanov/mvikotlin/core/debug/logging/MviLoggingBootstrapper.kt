package com.arkivanov.mvikotlin.core.debug.logging

import com.arkivanov.mvikotlin.base.store.MviBootstrapper
import com.arkivanov.mvikotlin.base.store.MviEventType

internal class MviLoggingBootstrapper<out Action : Any>(
    private val delegate: MviBootstrapper<Action>,
    private val logger: (String) -> Unit,
    private val loggingMode: MviLoggingMode,
    private val storeName: String
) : MviBootstrapper<Action> by delegate {

    override fun bootstrap(dispatch: (Action) -> Unit) {
        delegate.bootstrap {
            logger(loggingMode, storeName, MviEventType.ACTION, it)
            dispatch(it)
        }
    }
}
