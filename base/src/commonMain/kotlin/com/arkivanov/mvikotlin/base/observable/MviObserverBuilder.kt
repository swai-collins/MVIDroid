package com.arkivanov.mvikotlin.base.observable

inline fun <T> mviObserver(crossinline onNext: (T) -> Unit = {}, crossinline onComplete: () -> Unit = {}): MviObserver<T> =
    object : MviObserver<T> {
        override fun onNext(value: T) {
            onNext.invoke(value)
        }

        override fun onComplete() {
            onComplete.invoke()
        }
    }
