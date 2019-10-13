package com.arkivanov.mvikotlin.base.store

import com.arkivanov.mvikotlin.base.observer.MviObserver

interface MviStore<out State : Any, in Intent : Any, out Label : Any> {

    val state: State
    val isDisposed: Boolean

    fun dispose()

    fun addStateObserver(observer: MviObserver<State>)

    fun removeStateObserver(observer: MviObserver<State>)

    fun addLabelObserver(observer: MviObserver<Label>)

    fun removeLabelObserver(observer: MviObserver<Label>)

    fun accept(intent: Intent)
}
