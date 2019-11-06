package com.arkivanov.mvikotlin.extensions.reaktive

import com.arkivanov.mvikotlin.base.store.MviStore
import com.badoo.reaktive.observable.Observable

val <State : Any> MviStore<State, *, *>.states: Observable<State> get() = stateOutput.asReaktive()

val <Label : Any> MviStore<*, *, Label>.labels: Observable<Label> get() = labelOutput.asReaktive()
