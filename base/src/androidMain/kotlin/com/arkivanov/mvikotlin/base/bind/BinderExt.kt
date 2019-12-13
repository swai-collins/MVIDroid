package com.arkivanov.mvikotlin.base.bind

import androidx.lifecycle.Lifecycle

fun Binder.attachTo(lifecycle: Lifecycle) {
    lifecycle.attachBinder(this)
}

