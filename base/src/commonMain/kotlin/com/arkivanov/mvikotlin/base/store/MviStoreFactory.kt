package com.arkivanov.mvikotlin.base.store

interface MviStoreFactory {

    fun <State : Any, Intent : Any, Label : Any, Action : Any, Result : Any> create(
        name: String,
        initialState: State,
        bootstrapper: MviBootstrapper<Action>? = null,
        intentToAction: (Intent) -> Action,
        executorFactory: () -> MviExecutor<State, Action, Result, Label>,
        reducer: MviReducer<State, Result> = mviReducer { this }
    ): MviStore<State, Intent, Label>
}
