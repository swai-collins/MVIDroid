package com.arkivanov.mvikotlin.base.store

private val bypassExecutor: MviExecutor<Any, Any, Any, Nothing> =
    object : MviAbstractExecutor<Any, Any, Any, Nothing>() {
        override fun dispose() {
        }

        override fun bootstrap() {
        }

        override fun execute(intent: Any) {
            dispatch(intent)
        }
    }

fun <State : Any, Intent : Any> MviStoreFactory.create(
    name: String,
    initialState: State,
    reducer: MviReducer<State, Intent>
): MviStore<State, Intent, Nothing> =
    create(
        name = name,
        initialState = initialState,
        executorFactory = {
            @Suppress("UNCHECKED_CAST")
            bypassExecutor as MviAbstractExecutor<State, Intent, Intent, Nothing>
        },
        reducer = reducer
    )
