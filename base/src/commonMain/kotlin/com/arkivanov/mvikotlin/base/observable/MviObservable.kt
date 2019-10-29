package com.arkivanov.mvikotlin.base.observable

interface MviObservable<out T> {

    fun subscribe(observer: MviObserver<T>)

    fun unsubscribe(observer: MviObserver<T>)
}
