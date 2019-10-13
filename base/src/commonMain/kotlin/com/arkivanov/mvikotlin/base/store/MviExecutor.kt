package com.arkivanov.mvikotlin.base.store

interface MviExecutor<in State : Any, in Action : Any, out Result : Any, out Label : Any> {

    /**
     * Called internally by Store
     */
    fun init(stateSupplier: () -> State, resultConsumer: (Result) -> Unit, labelConsumer: (Label) -> Unit)

    /**
     * Invoked by Store with Action to execute, always on Main thread.
     *
     * @param action an Action that should be executed
     * @return disposable if there are any background operations, null otherwise.
     * A returned disposable will be managed by Store and disposed at the end of life-cycle.
     */
    fun executeAction(action: Action)

    fun dispose()
}