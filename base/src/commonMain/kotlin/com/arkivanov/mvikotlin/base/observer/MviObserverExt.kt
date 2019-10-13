package com.arkivanov.mvikotlin.base.observer

import com.badoo.reaktive.utils.atomic.getAndSet
import com.badoo.reaktive.utils.atomic.update

fun <T> MviObservers<T>.onNext(value: T) {
    this.value.forEach { it.onNext(value) }
}

fun <T> MviObservers<T>.clearAndComplete() {
    getAndSet(emptyList())
        .forEach(MviObserver<*>::onComplete)
}

operator fun <T> MviObservers<T>.plusAssign(observer: MviObserver<T>) {
    update { it + observer }
}

operator fun <T> MviObservers<T>.minusAssign(observer: MviObserver<T>) {
    update { it - observer }
}
