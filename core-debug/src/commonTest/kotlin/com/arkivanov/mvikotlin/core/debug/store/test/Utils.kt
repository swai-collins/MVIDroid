package com.arkivanov.mvikotlin.core.debug.store.test

import com.arkivanov.mvikotlin.base.observable.MviObservable

fun <T> MviObservable<T>.test(skipFirstValue: Boolean = false): TestObserver<T> {
    val observer = TestObserver<T>(skipFirstValue)
    subscribe(observer)

    return observer
}
