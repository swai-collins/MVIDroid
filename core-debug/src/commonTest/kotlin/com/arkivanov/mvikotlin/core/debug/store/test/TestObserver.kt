package com.arkivanov.mvikotlin.core.debug.store.test

import com.arkivanov.mvikotlin.base.observable.MviObserver

class TestObserver<T>(
    private var skipFirstValue: Boolean = false
) : MviObserver<T> {

    private val _values = ArrayList<T>()
    val values: List<T> get() = _values
    private var isFirstValueSkipped = false

    var isComplete: Boolean = false
        private set

    override fun onNext(value: T) {
        if (skipFirstValue && !isFirstValueSkipped) {
            isFirstValueSkipped = true
        } else {
            _values += value
        }
    }

    override fun onComplete() {
        isComplete = true
    }

    fun reset() {
        _values.clear()
        isFirstValueSkipped = false
        isComplete = false
    }
}
