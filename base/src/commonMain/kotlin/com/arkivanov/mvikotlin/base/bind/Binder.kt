package com.arkivanov.mvikotlin.base.bind

class Binder(
    private val bindings: Iterable<Binding>
) {
    fun start() {
        bindings.forEach(Binding::start)
    }

    fun stop() {
        bindings.forEach(Binding::stop)
    }
}
