package com.arkivanov.mvikotlin.base.store

import com.arkivanov.mvikotlin.base.observable.MviDisposable
import com.arkivanov.mvikotlin.base.observable.MviObservable

interface MviStore<out State : Any, in Intent : Any, out Label : Any> : MviDisposable {

    val state: State
    val stateOutput: MviObservable<State>
    val labelOutput: MviObservable<Label>

    fun accept(intent: Intent)
}
