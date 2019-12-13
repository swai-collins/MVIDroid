package com.arkivanov.mvikotlin.core.store

import com.arkivanov.mvikotlin.base.observable.MviObservable
import com.arkivanov.mvikotlin.base.store.MviExecutor
import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.utils.assertOnMainThread
import com.arkivanov.mvikotlin.utils.observable.MviBehaviorSubject
import com.arkivanov.mvikotlin.utils.observable.MviPublishSubject
import com.badoo.reaktive.utils.atomic.AtomicBoolean

internal class MviDefaultStore<out State : Any, in Intent : Any, out Label : Any, Result : Any>(
    initialState: State,
    private val executor: MviExecutor<State, Intent, Result, Label>,
    private val reducer: MviReducer<State, Result>
) : MviStore<State, Intent, Label> {

    init {
        assertOnMainThread()
    }

    private val stateSubject = MviBehaviorSubject(initialState)
    override val stateOutput: MviObservable<State> = stateSubject
    override val state: State get() = stateSubject.value

    private val labelSubject = MviPublishSubject<Label>()
    override val labelOutput: MviObservable<Label> = labelSubject

    private val _isDisposed = AtomicBoolean()
    override val isDisposed: Boolean get() = _isDisposed.value

    init {
        executor.init(
            stateSupplier = {
                assertOnMainThread()
                state
            },
            resultConsumer = ::onResult,
            labelConsumer = ::onLabel
        )

        executor.bootstrap()
    }

    override fun accept(intent: Intent) {
        doIfNotDisposed {
            executor.execute(intent)
        }
    }

    override fun dispose() {
        doIfNotDisposed {
            _isDisposed.value = true
            executor.dispose()

            stateSubject.onComplete()
            labelSubject.onComplete()
        }
    }

    private fun onResult(result: Result) {
        doIfNotDisposed {
            val newState = with(reducer) { stateSubject.value.reduce(result) }
            stateSubject.onNext(newState)
        }
    }

    private fun onLabel(label: Label) {
        doIfNotDisposed {
            labelSubject.onNext(label)
        }
    }

    private inline fun doIfNotDisposed(block: () -> Unit) {
        assertOnMainThread()

        if (!isDisposed) {
            block()
        }
    }
}
