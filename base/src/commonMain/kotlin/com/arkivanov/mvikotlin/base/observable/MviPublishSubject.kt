package com.arkivanov.mvikotlin.base.observable

import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.getAndSet
import com.badoo.reaktive.utils.atomic.update

class MviPublishSubject<T> : MviObservable<T> {

    private val observers = AtomicReference<List<MviObserver<T>>>(emptyList())

    override fun subscribe(observer: MviObserver<T>): MviDisposable {
        observers.update { it + observer }

        return mviDisposable {
            observers.update { it - observer }
        }
    }

    fun onNext(value: T) {
        observers.value.forEach { it.onNext(value) }
    }

    fun onComplete() {
        observers
            .getAndSet(emptyList())
            .forEach(MviObserver<*>::onComplete)
    }
}
