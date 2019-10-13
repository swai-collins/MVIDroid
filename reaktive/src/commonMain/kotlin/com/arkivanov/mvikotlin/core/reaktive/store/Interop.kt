package com.arkivanov.mvikotlin.core.reaktive.store

import com.arkivanov.mvikotlin.base.observer.mviObserver
import com.arkivanov.mvikotlin.base.store.MviStore
import com.badoo.reaktive.disposable.disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observable

fun <State : Any, Intent : Any, Label : Any> MviStore<State, Intent, Label>.asReaktive(): MviReaktiveStore<State, Intent, Label> =
    object : MviReaktiveStore<State, Intent, Label>, MviStore<State, Intent, Label> by this {
        override val states: Observable<State> =
            observable { emitter ->
                val observer = mviObserver(onNext = emitter::onNext, onComplete = emitter::onComplete)
                addStateObserver(observer)
                emitter.setDisposable(disposable { removeStateObserver(observer) })
            }

        override val labels: Observable<Label> =
            observable { emitter ->
                val observer = mviObserver(onNext = emitter::onNext, onComplete = emitter::onComplete)
                addLabelObserver(observer)
                emitter.setDisposable(disposable { removeLabelObserver(observer) })
            }
    }
