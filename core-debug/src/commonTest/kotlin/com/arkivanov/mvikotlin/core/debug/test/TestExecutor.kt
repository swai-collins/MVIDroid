package com.arkivanov.mvikotlin.core.debug.test

import com.arkivanov.mvikotlin.base.store.MviExecutor

class TestExecutor(
    private val onExecute: TestExecutor.(label: String) -> Unit = {}
) : MviExecutor<String, String, String, String> {

    var isInitialized = false
    var isDisposed = false

    lateinit var stateSupplier: () -> String
        private set

    lateinit var resultConsumer: (String) -> Unit
        private set

    lateinit var labelConsumer: (String) -> Unit
        private set

    override fun init(stateSupplier: () -> String, resultConsumer: (String) -> Unit, labelConsumer: (String) -> Unit) {
        isInitialized = true
        this.stateSupplier = stateSupplier
        this.resultConsumer = resultConsumer
        this.labelConsumer = labelConsumer
    }

    override fun executeAction(action: String) {
        onExecute(action)
    }

    override fun dispose() {
        isDisposed = true
    }
}
