package com.arkivanov.mvikotlin.extensions.reaktive

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.arkivanov.mvikotlin.base.bind.attachTo

inline fun LifecycleOwner.bindWithLifecycle(block: ReaktiveBindingBuilder.() -> Unit) {
    lifecycle.bind(block)
}

inline fun Lifecycle.bind(block: ReaktiveBindingBuilder.() -> Unit) {
    bindInternal(this, block)
}

// FIXME: Avoid?
@PublishedApi
internal inline fun bindInternal(lifecycle: Lifecycle, block: ReaktiveBindingBuilder.() -> Unit) {
    bind(block).attachTo(lifecycle)
}
