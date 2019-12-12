package com.arkivanov.mvikotlin.base.store

interface MviStoreFactory {

    fun <State : Any, Intent : Any, Label : Any, Result : Any> create(
        name: String,
        initialState: State,
        executorFactory: () -> MviExecutor<State, Intent, Result, Label>,
        @Suppress("UNCHECKED_CAST")
        reducer: MviReducer<State, Result> = bypassReducer as MviReducer<State, Result>
    ): MviStore<State, Intent, Label>

    private companion object {
        private val bypassReducer = mviReducer<Any, Any> { this }
    }
}
