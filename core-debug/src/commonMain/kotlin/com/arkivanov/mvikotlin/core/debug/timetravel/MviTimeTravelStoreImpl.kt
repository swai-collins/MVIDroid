package com.arkivanov.mvikotlin.core.debug.timetravel

import com.arkivanov.mvikotlin.base.observable.MviBehaviorSubject
import com.arkivanov.mvikotlin.base.observable.MviObservable
import com.arkivanov.mvikotlin.base.observable.MviPublishSubject
import com.arkivanov.mvikotlin.base.store.MviEventType
import com.arkivanov.mvikotlin.base.store.MviExecutor
import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.utils.assertOnMainThread
import com.arkivanov.mvikotlin.core.debug.timetravel.MviTimeTravelStore.EventDebugger
import com.arkivanov.mvikotlin.core.debug.timetravel.MviTimeTravelStore.EventProcessor
import com.badoo.reaktive.utils.atomic.AtomicBoolean
import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.update

internal class MviTimeTravelStoreImpl<out State : Any, in Intent : Any, out Label : Any, Result : Any>(
    private val name: String,
    initialState: State,
    private val executorFactory: () -> MviExecutor<State, Intent, Result, Label>,
    private val reducer: MviReducer<State, Result>
) : MviTimeTravelStore<State, Intent, Label> {

    init {
        assertOnMainThread()
    }

    private val executor = executorFactory()

    private val stateSubject = MviBehaviorSubject(initialState)
    override val stateOutput: MviObservable<State> = stateSubject
    override val state: State get() = stateSubject.value
    private var internalState = AtomicReference(initialState)

    private val labelSubject = MviPublishSubject<Label>()
    override val labelOutput: MviObservable<Label> get() = labelSubject

    private val eventSubject = MviPublishSubject<MviTimeTravelEvent>()
    override val eventOutput: MviObservable<MviTimeTravelEvent> = eventSubject

    private val _isDisposed = AtomicBoolean()
    override val isDisposed: Boolean get() = _isDisposed.value

    override val eventProcessor: EventProcessor = EventProcessorImpl()
    override val eventDebugger: EventDebugger = EventDebuggerImpl()

    override fun accept(intent: Intent) {
        doIfNotDisposed {
            onEvent(MviEventType.INTENT, intent)
        }
    }

    override fun dispose() {
        doIfNotDisposed {
            _isDisposed.value = true
            executor.dispose()

            stateSubject.onComplete()
            labelSubject.onComplete()
            eventSubject.onComplete()
        }
    }

    override fun init() {
        executor.init(
            stateSupplier = {
                assertOnMainThread()
                internalState.value
            },
            resultConsumer = { onEvent(MviEventType.RESULT, it) },
            labelConsumer = { onEvent(MviEventType.LABEL, it) }
        )

    }

    override fun restoreState() {
        doIfNotDisposed {
            stateSubject.onNext(internalState.value)
        }
    }

    private fun onEvent(type: MviEventType, value: Any, state: State? = null) {
        doIfNotDisposed {
            eventSubject.onNext(MviTimeTravelEvent(name, type, value, state ?: this.state))
        }
    }

    private inline fun doIfNotDisposed(block: () -> Unit) {
        assertOnMainThread()

        if (!isDisposed) {
            block()
        }
    }

    private inner class EventProcessorImpl : EventProcessor {
        @Suppress("UNCHECKED_CAST")
        override fun process(type: MviEventType, value: Any) {
            doIfNotDisposed {
                when (type) {
                    MviEventType.INTENT -> processIntent(value as Intent)
                    MviEventType.RESULT -> processResult(value as Result)
                    MviEventType.STATE -> processState(value as State)
                    MviEventType.LABEL -> processLabel(value as Label)
                }
            }
        }

        private fun processIntent(intent: Intent) {
            executor.execute(intent)
        }

        private fun processResult(result: Result) {
            val previousState = internalState.value
            val newState = with(reducer) { previousState.reduce(result) }
            internalState.value = newState
            onEvent(MviEventType.STATE, newState, previousState)
        }

        private fun processState(state: State) {
            stateSubject.onNext(state)
        }

        private fun processLabel(label: Label) {
            labelSubject.onNext(label)
        }
    }

    private inner class EventDebuggerImpl : EventDebugger {
        @Suppress("UNCHECKED_CAST")
        override fun debug(event: MviTimeTravelEvent) {
            assertOnMainThread()

            when (event.type) {
                MviEventType.INTENT -> debugIntent(event.value as Intent, event.state as State)
                MviEventType.RESULT -> debugResult(event.value as Result, event.state as State)
                MviEventType.STATE -> throw IllegalArgumentException("Can't debug event: $event")
                MviEventType.LABEL -> debugLabel(event.value as Label)
            }
        }

        private fun debugIntent(intent: Intent, initialState: State) {
            val localState = AtomicReference(initialState)
            executorFactory()
                .apply {
                    init(
                        stateSupplier = {
                            assertOnMainThread()
                            localState.value
                        },
                        resultConsumer = { result ->
                            doIfNotDisposed {
                                localState.update { state ->
                                    reducer.run { state.reduce(result) }
                                }
                            }
                        },
                        labelConsumer = { assertOnMainThread() }
                    )
                }
                .execute(intent)
        }

        private fun debugResult(result: Result, initialState: State) {
            with(reducer) {
                initialState.reduce(result)
            }
        }

        private fun debugLabel(label: Label) {
            labelSubject.onNext(label)
        }
    }
}
