package com.arkivanov.mvikotlin.base.observable

inline fun mviDisposable(crossinline onDispose: () -> Unit): MviDisposable =
    object : MviDisposable {
        override fun dispose() {
            onDispose()
        }
    }
