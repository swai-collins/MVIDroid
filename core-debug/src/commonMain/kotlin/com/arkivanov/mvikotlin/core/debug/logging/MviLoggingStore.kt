package com.arkivanov.mvikotlin.core.debug.logging

import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.store.MviEventType

internal class MviLoggingStore<out State : Any, in Intent : Any, out Label : Any>(
    private val delegate: MviStore<State, Intent, Label>,
    private val logger: (String) -> Unit,
    private val loggingMode: MviLoggingMode,
    private val name: String
) : MviStore<State, Intent, Label> by delegate {

    override fun dispose() {
        delegate.dispose()
        logger(loggingMode) { "$name: disposed" }
    }

    override fun accept(intent: Intent) {
        logger(loggingMode, name, MviEventType.INTENT, intent)
        delegate.accept(intent)
    }
}
