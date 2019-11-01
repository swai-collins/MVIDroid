package com.arkivanov.mvikotlin.base.observable

inline fun <T, R> MviObservable<T>.diff(
    crossinline mapper: (T) -> R,
    crossinline comparator: (newValue: R, oldValue: R) -> Boolean,
    crossinline consumer: (R) -> Unit
): MviDisposable {
    var isPreviousValueAvailable = false
    var previousValue: R? = null

    return subscribe { newModel ->
        val newValue = mapper(newModel)

        if (isPreviousValueAvailable) {
            @Suppress("UNCHECKED_CAST")
            val prevValue = previousValue as R
            previousValue = newValue
            if (!comparator(newValue, prevValue)) {
                consumer(newValue)
            }
        } else {
            isPreviousValueAvailable = true
            previousValue = newValue
            consumer(newValue)
        }
    }
}

inline fun <T, R> MviObservable<T>.diff(
    crossinline mapper: (T) -> R,
    crossinline consumer: (R) -> Unit
): MviDisposable =
    diff(mapper = mapper, comparator = { a, b -> a == b }, consumer = consumer)
