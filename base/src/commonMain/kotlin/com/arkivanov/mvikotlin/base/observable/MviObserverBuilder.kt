package com.arkivanov.mvikotlin.base.observable

inline fun <T> mviObserver(crossinline onComplete: () -> Unit = {}, crossinline onNext: (T) -> Unit = {}): MviObserver<T> =
    object : MviObserver<T> {
        override fun onNext(value: T) {
            onNext.invoke(value)
        }

        override fun onComplete() {
            onComplete.invoke()
        }
    }
