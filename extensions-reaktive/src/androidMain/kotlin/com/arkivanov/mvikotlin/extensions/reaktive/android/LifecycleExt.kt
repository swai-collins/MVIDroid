package com.arkivanov.mvikotlin.extensions.reaktive.android

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.arkivanov.mvikotlin.extensions.reaktive.BindingBuilder
import com.arkivanov.mvikotlin.extensions.reaktive.bind

inline fun LifecycleOwner.bind(block: BindingBuilder.() -> Unit) {
    bind(lifecycle, block)
}

inline fun bind(lifecycle: Lifecycle, block: BindingBuilder.() -> Unit) {
    bind(block).attachTo(lifecycle)
}
