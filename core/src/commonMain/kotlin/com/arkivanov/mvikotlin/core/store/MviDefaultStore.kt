package com.arkivanov.mvikotlin.core.store

import com.arkivanov.mvikotlin.base.observer.MviObserver
import com.arkivanov.mvikotlin.base.observer.MviObservers
import com.arkivanov.mvikotlin.base.observer.clearAndComplete
import com.arkivanov.mvikotlin.base.observer.minusAssign
import com.arkivanov.mvikotlin.base.observer.onNext
import com.arkivanov.mvikotlin.base.observer.plusAssign
import com.arkivanov.mvikotlin.base.store.MviBootstrapper
import com.arkivanov.mvikotlin.base.store.MviExecutor
import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.utils.assertOnMainThread
import com.badoo.reaktive.utils.atomic.AtomicBoolean
import com.badoo.reaktive.utils.atomic.AtomicReference

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

    private val _state = AtomicReference<State>(initialState)
    override val state: State get() = _state.value

    private val stateObservers = MviObservers<State>(emptyList())
    private val labelObservers = MviObservers<Label>(emptyList())

    private val _isDisposed = AtomicBoolean()
    override val isDisposed: Boolean get() = _isDisposed.value

    init {
        executor.init(
            stateSupplier = {
                assertOnMainThread()
                _state.value
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

    override fun addStateObserver(observer: MviObserver<State>) {
        doIfNotDisposed {
            stateObservers += observer
            observer.onNext(_state.value)
        }
    }

    override fun removeStateObserver(observer: MviObserver<State>) {
        doIfNotDisposed {
            stateObservers -= observer
        }
    }

    override fun addLabelObserver(observer: MviObserver<Label>) {
        doIfNotDisposed {
            labelObservers += observer
        }
    }

    override fun removeLabelObserver(observer: MviObserver<Label>) {
        doIfNotDisposed {
            labelObservers -= observer
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

            stateObservers.clearAndComplete()
            labelObservers.clearAndComplete()
        }
    }

    private fun onResult(result: Result) {
        doIfNotDisposed {
            val newState = with(reducer) { _state.value.reduce(result) }
            _state.value = newState
            stateObservers.onNext(newState)
        }
    }

    private fun onLabel(label: Label) {
        doIfNotDisposed {
            labelObservers.onNext(label)
        }
    }

    private inline fun doIfNotDisposed(block: () -> Unit) {
        assertOnMainThread()

        if (!isDisposed) {
            block()
        }
    }
}
