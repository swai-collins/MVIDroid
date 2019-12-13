package com.arkivanov.mvikotlin.extensions.reaktive

import com.arkivanov.mvikotlin.base.observable.MviObservable
import com.arkivanov.mvikotlin.base.observable.subscribe
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

//fun MviDisposable.asReaktive(): Disposable = disposable(::dispose)

fun <T> MviObservable<T>.asFlow(): Flow<T> =
    callbackFlow {
        val disposable = subscribe(onComplete = { channel.close() }, onNext = { channel.offer(it) })
        awaitClose(disposable::dispose)
    }
