package com.arkivanov.mvikotlin.base.store

abstract class MviAbstractExecutor<State : Any, in Intent : Any, Result : Any, Label : Any> : MviExecutor<State, Intent, Result, Label> {

    private var isInitialized: Boolean = false
    private lateinit var stateSupplier: () -> State
    private lateinit var resultConsumer: (Result) -> Unit
    private lateinit var labelConsumer: (Label) -> Unit

    /**
     * Provides current State of Store, must be accessed only on Main thread
     */
    protected val state: State get() = stateSupplier()

    /**
     * Called internally by Store
     */
    final override fun init(stateSupplier: () -> State, resultConsumer: (Result) -> Unit, labelConsumer: (Label) -> Unit) {
        check(!isInitialized) { "MviExecutor cannot be reused, please make sure that it is not a singleton" }

        isInitialized = true
        this.stateSupplier = stateSupplier
        this.resultConsumer = resultConsumer
        this.labelConsumer = labelConsumer
    }

    /**
     * Dispatches Result. Any dispatched Result will be synchronously processed by Store
     * which means a new State will synchronously applied to Store and emitted.
     * You can get a new State right after this method return. Must be called only on Main thread.
     *
     * @param result a Result to dispatch
     */
    protected fun dispatch(result: Result) {
        resultConsumer(result)
    }

    /**
     * Publishes Label. Any published Label will be synchronously processed and emitted by Store.
     * Must be called only on Main thread
     *
     * @param label a Label to publish
     */
    protected fun publish(label: Label) {
        labelConsumer(label)
    }
}
