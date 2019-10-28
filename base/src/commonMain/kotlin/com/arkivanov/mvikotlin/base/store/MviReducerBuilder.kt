package com.arkivanov.mvikotlin.base.store

inline fun <State : Any, Result : Any> mviReducer(crossinline reduce: State.(Result) -> State): MviReducer<State, Result> =
    object : MviReducer<State, Result> {
        override fun State.reduce(result: Result): State = reduce.invoke(this, result)
    }