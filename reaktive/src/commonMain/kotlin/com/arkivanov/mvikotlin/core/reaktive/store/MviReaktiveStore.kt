package com.arkivanov.mvikotlin.core.reaktive.store

import com.arkivanov.mvikotlin.base.store.MviStore
import com.badoo.reaktive.observable.Observable

interface MviReaktiveStore<out State : Any, in Intent : Any, out Label : Any> : MviStore<State, Intent, Label> {

    val states: Observable<State>
    val labels: Observable<Label>
}
