package com.arkivanov.mvikotlin.base.observer

interface MviObserver<in T> {

    fun onNext(value: T)

    fun onComplete()
}
