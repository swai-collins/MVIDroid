package com.arkivanov.mvikotlin.extensions.reaktive

import com.arkivanov.mvikotlin.base.observable.MviDisposable
import com.arkivanov.mvikotlin.base.observable.MviObservable
import com.arkivanov.mvikotlin.base.observable.subscribe
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observable

fun MviDisposable.asReaktive(): Disposable = disposable(::dispose)

fun <T> MviObservable<T>.asReaktive(): Observable<T> =
    observable { emitter ->
        val disposable = subscribe(onComplete = emitter::onComplete, onNext = emitter::onNext)
        emitter.setDisposable(disposable.asReaktive())
    }
