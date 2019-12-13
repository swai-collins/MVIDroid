package com.arkivanov.mvikotlin.base.observable

interface MviDisposable {

    val isDisposed: Boolean

    fun dispose()
}
