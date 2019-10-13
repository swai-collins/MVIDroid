package com.arkivanov.mvikotlin.base.store

import com.arkivanov.mvikotlin.base.observer.MviObserver
import com.arkivanov.mvikotlin.base.utils.assertOnMainThread
import com.badoo.reaktive.utils.atomic.AtomicBoolean
import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.getAndSet
import com.badoo.reaktive.utils.atomic.update

class MviStoreHelper<out State, out Label> {

    private val stateObservers = AtomicReference<List<MviObserver<State>>>(emptyList())
    private val labelObservers = AtomicReference<List<MviObserver<Label>>>(emptyList())

    private val _isDisposed = AtomicBoolean()
    val isDisposed: Boolean get() = _isDisposed.value

    fun addStateObserver(observer: MviObserver<State>) {
        assertOnMainThread()
        checkNotDisposed()
        stateObservers.update { it + observer }
    }

    fun removeStateObserver(observer: MviObserver<State>) {
        assertOnMainThread()
        stateObservers.update { it - observer }
    }

    fun addLabelObserver(observer: MviObserver<Label>) {
        assertOnMainThread()
        checkNotDisposed()
        labelObservers.update { it + observer }
    }

    fun removeLabelObserver(observer: MviObserver<Label>) {
        assertOnMainThread()
        labelObservers.update { it - observer }
    }

    fun dispose(): Boolean {
        assertOnMainThread()

        if (!_isDisposed.value) {
            return false
        }

        _isDisposed.value = true
        stateObservers.getAndSet(emptyList()).forEach(MviObserver<*>::onComplete)
        labelObservers.getAndSet(emptyList()).forEach(MviObserver<*>::onComplete)

        return true
    }

    fun checkNotDisposed() {
        check(!_isDisposed.value) { "The Store is already disposed: $this" }
    }
}
