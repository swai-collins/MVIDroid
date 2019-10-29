package com.arkivanov.mvikotlin.base.observable

interface MviObserver<in T> {

    fun onNext(value: T)

    fun onComplete()
}
