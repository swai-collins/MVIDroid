package com.arkivanov.mvikotlin.extensions.reaktive

import com.arkivanov.mvikotlin.base.bind.Binder
import com.arkivanov.mvikotlin.base.bind.Binding
import com.arkivanov.mvikotlin.base.bind.MviBindingMaker
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.view.MviView
import com.badoo.reaktive.disposable.DisposableWrapper
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.subscribe

@MviBindingMaker
interface ReaktiveBindingBuilder {

    infix fun <T> Observable<T>.with(consumer: (T) -> Unit)

    infix fun <T> Observable<T>.with(view: MviView<T, *>)

    infix fun <T : Any> Observable<T>.with(store: MviStore<*, T, *>)
}

inline fun bind(block: ReaktiveBindingBuilder.() -> Unit): Binder {
    val bindings = ArrayList<Binding>()

    block(
        object : ReaktiveBindingBuilder {
            override fun <T> Observable<T>.with(consumer: (T) -> Unit) {
                bindings += ReaktiveBinding(source = this, consumer = consumer)
            }

            override fun <T> Observable<T>.with(view: MviView<T, *>) {
                bindings += ReaktiveBinding(source = this, consumer = view::bind)
            }

            override fun <T : Any> Observable<T>.with(store: MviStore<*, T, *>) {
                bindings += ReaktiveBinding(source = this, consumer = store::accept)
            }
        }
    )

    return Binder(bindings)
}

class ReaktiveBinding<T>(
    private val source: Observable<T>,
    private val consumer: (T) -> Unit
) : DisposableWrapper(), Binding {

    override fun start() {
        set(source.subscribe(isThreadLocal = true, onNext = consumer))
    }

    override fun stop() {
        set(null)
    }
}
