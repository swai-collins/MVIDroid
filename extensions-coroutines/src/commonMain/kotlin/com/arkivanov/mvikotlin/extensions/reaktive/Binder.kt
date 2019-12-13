package com.arkivanov.mvikotlin.extensions.reaktive

import com.arkivanov.mvikotlin.base.bind.Binder
import com.arkivanov.mvikotlin.base.bind.Binding
import com.arkivanov.mvikotlin.base.bind.MviBindingMaker
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.view.MviView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@MviBindingMaker
interface FlowBindingBuilder {

    infix fun <T> Flow<T>.with(consumer: (T) -> Unit)

    infix fun <T> Flow<T>.with(view: MviView<T, *>)

    infix fun <T : Any> Flow<T>.with(store: MviStore<*, T, *>)
}

inline fun bind(block: FlowBindingBuilder.() -> Unit): Binder {
    val bindings = ArrayList<Binding>()

    block(
        object : FlowBindingBuilder {
            override fun <T> Flow<T>.with(consumer: (T) -> Unit) {
                bindings += ReaktiveBinding(source = this, consumer = consumer)
            }

            override fun <T> Flow<T>.with(view: MviView<T, *>) {
                bindings += ReaktiveBinding(source = this, consumer = view::bind)
            }

            override fun <T : Any> Flow<T>.with(store: MviStore<*, T, *>) {
                bindings += ReaktiveBinding(source = this, consumer = store::accept)
            }
        }
    )

    return Binder(bindings)
}

class ReaktiveBinding<T>(
    private val source: Flow<T>,
    private val consumer: (T) -> Unit
) : Binding {

    private var job: Job? = null

    override fun start() {
        job =
            GlobalScope.launch(Dispatchers.Main) {
                source.collect {
                    consumer(it)
                }
            }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }
}
