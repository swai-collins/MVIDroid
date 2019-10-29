package com.arkivanov.mvikotlin.core.store

import com.arkivanov.mvikotlin.base.observable.MviObservable
import com.arkivanov.mvikotlin.base.store.MviBootstrapper
import com.arkivanov.mvikotlin.base.store.MviExecutor
import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.observable.MviBehaviorSubject
import com.arkivanov.mvikotlin.base.observable.MviPublishSubject
import com.arkivanov.mvikotlin.base.utils.assertOnMainThread
import com.badoo.reaktive.utils.atomic.AtomicBoolean

class MviDefaultStore<out State : Any, in Intent : Any, out Label : Any, Action : Any, Result : Any>(
    initialState: State,
    private val bootstrapper: MviBootstrapper<Action>?,
    private val intentToAction: (Intent) -> Action,
    private val executor: MviExecutor<State, Action, Result, Label>,
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

        bootstrapper?.bootstrap {
            doIfNotDisposed {
                executor.executeAction(it)
            }
        }
    }

    override fun accept(intent: Intent) {
        doIfNotDisposed {
            executor.executeAction(intentToAction(intent))
        }
    }

    override fun dispose() {
        doIfNotDisposed {
            _isDisposed.value = true
            bootstrapper?.dispose()
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
