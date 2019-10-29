package com.arkivanov.mvikotlin.base.store

import com.arkivanov.mvikotlin.base.observable.MviObservable

interface MviStore<out State : Any, in Intent : Any, out Label : Any> {

    val state: State
    val stateOutput: MviObservable<State>
    val labelOutput: MviObservable<Label>
    val isDisposed: Boolean

    fun dispose()

    fun accept(intent: Intent)
}
