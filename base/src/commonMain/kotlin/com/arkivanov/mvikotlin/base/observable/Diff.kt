package com.arkivanov.mvikotlin.base.observable

inline fun <T, R> MviObservable<T>.diff(
    crossinline mapper: (T) -> R,
    crossinline comparator: (newValue: R, oldValue: R) -> Boolean,
    crossinline consumer: (R) -> Unit
): MviObserver<T> {
    var isPreviousValueAvailable = false
    var previousValue: R? = null

    val observer =
        mviObserver<T> { newModel ->
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

    subscribe(observer)

    return observer
}

inline fun <T, R> MviObservable<T>.diff(
    crossinline mapper: (T) -> R,
    crossinline consumer: (R) -> Unit
): MviObserver<T> =
    diff(mapper = mapper, comparator = { a, b -> a == b }, consumer = consumer)
