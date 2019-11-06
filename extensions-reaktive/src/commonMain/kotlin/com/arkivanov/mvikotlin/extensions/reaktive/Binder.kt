package com.arkivanov.mvikotlin.extensions.reaktive

import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.view.MviView
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.subscribe

interface BindingBuilder {
    infix fun <T> Observable<T>.with(consumer: (T) -> Unit)

    infix fun <T> Observable<T>.with(view: MviView<T, *>)

    infix fun <T : Any> Observable<T>.with(store: MviStore<*, T, *>)
}

class Binder(
    private val bindings: Iterable<Binding<*>>
) {
    private val disposables = CompositeDisposable()

    fun start() {
        bindings.forEach {
            disposables += bind(it)
        }
    }

    fun stop() {
        disposables.clear()
    }

    private fun <T> bind(binding: Binding<T>): Disposable =
        binding.source.subscribe(isThreadLocal = true, onNext = binding.consumer)
}

inline fun bind(block: BindingBuilder.() -> Unit): Binder {
    val bindings = ArrayList<Binding<*>>()

    block(
        object : BindingBuilder {
            override fun <T> Observable<T>.with(consumer: (T) -> Unit) {
                bindings += Binding(source = this, consumer = consumer)
            }

            override fun <T> Observable<T>.with(view: MviView<T, *>) {
                bindings += Binding(source = this, consumer = view::bind)
            }

            override fun <T : Any> Observable<T>.with(store: MviStore<*, T, *>) {
                bindings += Binding(source = this, consumer = store::accept)
            }
        }
    )

    return Binder(bindings)
}

class Binding<T>(
    val source: Observable<T>,
    val consumer: (T) -> Unit
)
