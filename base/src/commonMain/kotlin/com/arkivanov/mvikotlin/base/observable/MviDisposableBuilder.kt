package com.arkivanov.mvikotlin.base.observable

import com.badoo.reaktive.utils.atomic.AtomicBoolean

inline fun mviDisposable(crossinline onDispose: () -> Unit): MviDisposable =
    object : MviDisposable {
        @Suppress("ObjectPropertyName")
        private val _isDisposed = AtomicBoolean()
        override val isDisposed: Boolean get() = _isDisposed.value

        override fun dispose() {
            _isDisposed.value = true
            onDispose()
        }
    }
