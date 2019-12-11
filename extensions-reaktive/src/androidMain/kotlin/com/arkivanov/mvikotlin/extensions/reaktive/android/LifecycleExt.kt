package com.arkivanov.mvikotlin.extensions.reaktive.android

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.arkivanov.mvikotlin.extensions.reaktive.Binder
import com.arkivanov.mvikotlin.extensions.reaktive.BindingBuilder
import com.arkivanov.mvikotlin.extensions.reaktive.bind

inline fun LifecycleOwner.bind(block: BindingBuilder.() -> Unit) {
    bind(lifecycle, block)
}

inline fun bind(lifecycle: Lifecycle, block: BindingBuilder.() -> Unit) {
    bind(block).attachTo(lifecycle)
}

fun Lifecycle.attachBinder(binder: Binder) {
    binder
        .asLifecycleObserver(currentState)
        ?.also(::addObserver)
}

fun LifecycleOwner.attachBinder(binder: Binder) {
    lifecycle.attachBinder(binder)
}

operator fun Lifecycle.plusAssign(binder: Binder) {
    attachBinder(binder)
}

operator fun LifecycleOwner.plusAssign(binder: Binder) {
    attachBinder(binder)
}
