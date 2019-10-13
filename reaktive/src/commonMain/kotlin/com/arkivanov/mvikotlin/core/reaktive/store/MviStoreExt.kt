package com.arkivanov.mvikotlin.core.reaktive.store

import com.arkivanov.mvikotlin.base.observer.mviObserver
import com.arkivanov.mvikotlin.base.store.MviStore
import com.badoo.reaktive.disposable.disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observable

val <State : Any> MviStore<State, *, *>.states: Observable<State>
    get() =
        observable { emitter ->
            val observer = mviObserver(onNext = emitter::onNext, onComplete = emitter::onComplete)
            addStateObserver(observer)
            emitter.setDisposable(disposable { removeStateObserver(observer) })
        }

val <Label : Any> MviStore<*, *, Label>.labels: Observable<Label>
    get() =
        observable { emitter ->
            val observer = mviObserver(onNext = emitter::onNext, onComplete = emitter::onComplete)
            addLabelObserver(observer)
            emitter.setDisposable(disposable { removeLabelObserver(observer) })
        }
