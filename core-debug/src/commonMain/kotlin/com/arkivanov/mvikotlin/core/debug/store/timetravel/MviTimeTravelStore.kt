package com.arkivanov.mvikotlin.core.debug.store.timetravel

import com.arkivanov.mvikotlin.base.observable.MviObservable
import com.arkivanov.mvikotlin.base.store.MviBootstrapper
import com.arkivanov.mvikotlin.base.store.MviExecutor
import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.observable.MviBehaviorSubject
import com.arkivanov.mvikotlin.base.observable.MviPublishSubject
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

    private val stateSubject = MviBehaviorSubject(initialState)
    override val stateOutput: MviObservable<State> = stateSubject
    override val state: State get() = stateSubject.value
    private var internalState = AtomicReference(initialState)

    private val labelSubject = MviPublishSubject<Label>()
    override val labelOutput: MviObservable<Label> get() = labelSubject

    private val eventSubject = MviPublishSubject<MviTimeTravelEvent>()
    val eventOutput: MviObservable<MviTimeTravelEvent> = eventSubject

    private val _isDisposed = AtomicBoolean()
    override val isDisposed: Boolean get() = _isDisposed.value

    val eventProcessor = EventProcessor()
    val eventDebugger = EventDebugger()

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

            stateSubject.onComplete()
            labelSubject.onComplete()
            eventSubject.onComplete()
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

    inner class EventProcessor {
        @Suppress("UNCHECKED_CAST")
        fun process(type: MviEventType, value: Any) {
            doIfNotDisposed {
                when (type) {
                    MviEventType.INTENT -> processIntent(value as Intent)
                    MviEventType.ACTION -> processAction(value as Action)
                    MviEventType.RESULT -> processResult(value as Result)
                    MviEventType.STATE -> processState(value as State)
                    MviEventType.LABEL -> processLabel(value as Label)
                }
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
            stateSubject.onNext(state)
        }

        private fun processLabel(label: Label) {
            labelSubject.onNext(label)
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
            labelSubject.onNext(label)
        }
    }
}
