package com.arkivanov.mvikotlin.base.store

// FIXME: Reorder generics
interface MviExecutor<in State : Any, in Intent : Any, out Result : Any, out Label : Any> {

    fun init(stateSupplier: () -> State, resultConsumer: (Result) -> Unit, labelConsumer: (Label) -> Unit)

    fun dispose()

    fun bootstrap()

    fun execute(intent: Intent)
}
