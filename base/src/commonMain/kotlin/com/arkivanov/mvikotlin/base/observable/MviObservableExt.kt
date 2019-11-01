package com.arkivanov.mvikotlin.base.observable

inline fun <T> MviObservable<T>.subscribe(
    crossinline onComplete: () -> Unit = {},
    crossinline onNext: (T) -> Unit = {}
): MviDisposable =
    subscribe(
        object : MviObserver<T> {
            override fun onNext(value: T) {
                onNext.invoke(value)
            }

            override fun onComplete() {
                onComplete.invoke()
            }
        }
    )
