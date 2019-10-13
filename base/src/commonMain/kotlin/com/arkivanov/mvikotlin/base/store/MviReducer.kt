package com.arkivanov.mvikotlin.base.store

interface MviReducer<State : Any, in Result : Any> {

    fun State.reduce(result: Result): State
}