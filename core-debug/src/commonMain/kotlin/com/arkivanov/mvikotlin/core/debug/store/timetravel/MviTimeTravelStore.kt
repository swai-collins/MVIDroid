package com.arkivanov.mvikotlin.core.debug.store.timetravel

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
import com.arkivanov.mvikotlin.core.debug.store.MviEventType
import com.badoo.reaktive.utils.atomic.AtomicBoolean
import com.badoo.reaktive.utils.atomic.AtomicReference

internal class MviTimeTravelStore<out State : Any, in Intent : Any, out Label : Any, Action : Any, Result : Any>(
    private val name: String,
    initialState: State,
    private val bootstrapper: MviBootstrapper<Action>?,
    private val intentToAction: (Intent) -> Action,
    private val executorFactory: () -> MviExecutor<State, Action, Result, Label>,
    private val reducer: MviReducer<State, Result>
) : MviStore<State, Intent, Label> {

    init {
        assertOnMainThread()
    }

    private val executor = executorFactory()

    private val _state = AtomicReference(initialState)
    override val state: State get() = _state.value
    private var internalState = AtomicReference(initialState)

    private val stateObservers = MviObservers<State>(emptyList())
    private val labelObservers = MviObservers<Label>(emptyList())
    private val eventObservers = MviObservers<MviTimeTravelEvent>(emptyList())

    private val _isDisposed = AtomicBoolean()
    override val isDisposed: Boolean get() = _isDisposed.value

    val eventProcessor = EventProcessor()
    val eventDebugger = EventDebugger()

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

    fun addEventObserver(observer: MviObserver<MviTimeTravelEvent>) {
        doIfNotDisposed {
            eventObservers += observer
        }
    }

    fun removeEventObserver(observer: MviObserver<MviTimeTravelEvent>) {
        doIfNotDisposed {
            eventObservers -= observer
        }
    }

    override fun accept(intent: Intent) {
        doIfNotDisposed {
            onEvent(MviEventType.INTENT, intent)
        }
    }

    override fun dispose() {
        doIfNotDisposed {
            _isDisposed.value = true
            bootstrapper?.dispose()
            executor.dispose()

            stateObservers.clearAndComplete()
            labelObservers.clearAndComplete()
            eventObservers.clearAndComplete()
        }
    }

    fun init() {
        executor.init(
            stateSupplier = {
                assertOnMainThread()
                internalState.value
            },
            resultConsumer = { onEvent(MviEventType.RESULT, it) },
            labelConsumer = { onEvent(MviEventType.LABEL, it) }
        )

        bootstrapper?.bootstrap { onEvent(MviEventType.ACTION, it) }
    }

    fun restoreState() {
        onState(internalState.value)
    }

    private fun onState(state: State) {
        doIfNotDisposed {
            _state.value = state
            stateObservers.onNext(state)
        }
    }

    private fun onLabel(label: Label) {
        doIfNotDisposed {
            labelObservers.onNext(label)
        }
    }

    private fun onEvent(type: MviEventType, value: Any, state: State? = null) {
        doIfNotDisposed {
            eventObservers.onNext(MviTimeTravelEvent(name, type, value, state ?: this.state))
        }
    }

    private inline fun doIfNotDisposed(block: () -> Unit) {
        assertOnMainThread()

        if (!isDisposed) {
            block()
        }
    }

    inner class EventProcessor {
        @Suppress("UNCHECKED_CAST")
        fun process(type: MviEventType, value: Any) {
            when (type) {
                MviEventType.INTENT -> processIntent(value as Intent)
                MviEventType.ACTION -> processAction(value as Action)
                MviEventType.RESULT -> processResult(value as Result)
                MviEventType.STATE -> processState(value as State)
                MviEventType.LABEL -> processLabel(value as Label)
            }
        }

        private fun processIntent(intent: Intent) {
            onEvent(MviEventType.ACTION, intentToAction(intent))
        }

        private fun processAction(action: Action) {
            executor.executeAction(action)
        }

        private fun processResult(result: Result) {
            val previousState = internalState.value
            val newState = with(reducer) { previousState.reduce(result) }
            internalState.value = newState
            onEvent(MviEventType.STATE, newState, previousState)
        }

        private fun processState(state: State) {
            onState(state)
        }

        private fun processLabel(label: Label) {
            onLabel(label)
        }
    }

    inner class EventDebugger {
        @Suppress("UNCHECKED_CAST")
        fun debug(event: MviTimeTravelEvent) {
            assertOnMainThread()

            when (event.type) {
                MviEventType.INTENT -> debugIntent(event.value as Intent)
                MviEventType.ACTION -> debugAction(event.value as Action, event.state as State)
                MviEventType.RESULT -> debugResult(event.value as Result, event.state as State)
                MviEventType.STATE -> throw IllegalArgumentException("Can't debug event: $event")
                MviEventType.LABEL -> debugLabel(event.value as Label)
            }
        }

        private fun debugIntent(intent: Intent) {
            intentToAction(intent)
        }

        private fun debugAction(action: Action, initialState: State) {
            val localState = AtomicReference(initialState)
            executorFactory()
                .apply {
                    init(
                        stateSupplier = {
                            assertOnMainThread()
                            localState.value
                        },
                        resultConsumer = {
                            doIfNotDisposed {
                                localState.value = with(reducer) { localState.value.reduce(it) }
                            }
                        },
                        labelConsumer = { assertOnMainThread() }
                    )
                }
                .executeAction(action)
        }

        private fun debugResult(result: Result, initialState: State) {
            with(reducer) {
                initialState.reduce(result)
            }
        }

        private fun debugLabel(label: Label) {
            onLabel(label)
        }
    }
}
