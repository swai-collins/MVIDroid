package com.arkivanov.mvikotlin.utils.observable

import com.arkivanov.mvikotlin.base.observable.MviDisposable
import com.arkivanov.mvikotlin.base.observable.MviObservable
import com.arkivanov.mvikotlin.base.observable.MviObserver
import com.arkivanov.mvikotlin.base.observable.mviDisposable
import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.getAndSet
import com.badoo.reaktive.utils.atomic.update
import com.badoo.reaktive.utils.atomic.updateAndGet

class MviBehaviorSubject<T>(initialValue: T) : MviObservable<T> {

    private val observers = AtomicReference<List<MviObserver<T>>>(emptyList())
    private val _value = AtomicReference(initialValue)
    val value: T get() = _value.value

    override fun subscribe(observer: MviObserver<T>): MviDisposable {
        val currentValue = _value.value

        observers
            .updateAndGet { it + observer }
            .forEach { it.onNext(currentValue) }

        return mviDisposable {
            observers.update { it - observer }
        }
    }

    fun onNext(value: T) {
        _value.value = value
        observers.value.forEach { it.onNext(value) }
    }

    fun onComplete() {
        observers
            .getAndSet(emptyList())
            .forEach(MviObserver<*>::onComplete)
    }
}
