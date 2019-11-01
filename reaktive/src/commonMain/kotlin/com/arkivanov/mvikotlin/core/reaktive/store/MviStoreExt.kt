package com.arkivanov.mvikotlin.core.reaktive.store

import com.arkivanov.mvikotlin.base.observable.subscribe
import com.arkivanov.mvikotlin.base.store.MviStore
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observable

val <State : Any> MviStore<State, *, *>.states: Observable<State>
    get() =
        observable { emitter ->
            val disposable = stateOutput.subscribe(onComplete = emitter::onComplete, onNext = emitter::onNext)
            emitter.setDisposable(disposable.asReaktive())
        }

val <Label : Any> MviStore<*, *, Label>.labels: Observable<Label>
    get() =
        observable { emitter ->
            val disposable = labelOutput.subscribe(onNext = emitter::onNext, onComplete = emitter::onComplete)
            emitter.setDisposable(disposable.asReaktive())
        }
