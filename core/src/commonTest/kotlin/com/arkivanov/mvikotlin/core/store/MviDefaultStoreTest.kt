package com.arkivanov.mvikotlin.core.store

import com.arkivanov.mvikotlin.base.observer.mviObserver
import com.arkivanov.mvikotlin.base.store.MviBootstrapper
import com.arkivanov.mvikotlin.base.store.MviExecutor
import com.arkivanov.mvikotlin.base.store.MviReducer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MviDefaultStoreTest {

    @Test
    fun executor_invoked_WHEN_bootstrapper_dispatched_action() {
        lateinit var action: String

        store(
            bootstrapper = TestBootstrapper { dispatch -> dispatch("action") },
            executor = TestExecutor { action = it }
        )

        assertEquals("action", action)
    }

    @Test
    fun disposes_bootstrapper_WHEN_store_disposed() {
        val bootstrapper = TestBootstrapper()
        val store = store(bootstrapper = bootstrapper)

        store.dispose()

        assertTrue(bootstrapper.isDisposed)
    }

    @Test
    fun disposes_executor_WHEN_store_disposed() {
        val executor = TestExecutor()
        val store = store(executor = executor)

        store.dispose()

        assertTrue(executor.isDisposed)
    }

    @Test
    fun executor_initialized_WHEN_store_created() {
        val executor = TestExecutor()

        store(executor = executor)

        assertTrue(executor.isInitialized)
    }

    @Test
    fun executor_can_read_store_state() {
        val executor = TestExecutor()
        store(executor = executor)

        val state = executor.stateSupplier()

        assertEquals(INITIAL_STATE, state)
    }

    @Test
    fun state_updated_WHEN_executor_dispatched_result() {
        val executor = TestExecutor()
        val store = store(executor = executor)

        executor.resultConsumer("result")

        assertEquals("result", store.state)
    }

    @Test
    fun state_emitted_WHEN_executor_dispatched_result() {
        lateinit var state: String
        val executor = TestExecutor()
        val store = store(executor = executor)
        store.addStateObserver(mviObserver(onNext = { state = it }))

        executor.resultConsumer("result")

        assertEquals("result", state)
    }

    @Test
    fun label_emitted_WHEN_executor_published_label() {
        lateinit var label: String
        val executor = TestExecutor()
        val store = store(executor = executor)
        store.addLabelObserver(mviObserver(onNext = { label = it }))

        executor.labelConsumer("label")

        assertEquals("label", label)
    }

    @Test
    fun last_state_emitted_WHEN_subscribed() {
        lateinit var state: String
        val executor = TestExecutor()
        val store = store(executor = executor)
        executor.resultConsumer("result")

        store.addStateObserver(mviObserver(onNext = { state = it }))

        assertEquals("result", state)
    }

    @Test
    fun last_label_not_emitted_WHEN_subscribed() {
        var label: String? = null
        val executor = TestExecutor()
        val store = store(executor = executor)
        executor.labelConsumer("label")

        store.addLabelObserver(mviObserver(onNext = { label = it }))

        assertNull(label)
    }

    @Test
    fun executor_invoked_WHEN_intent_send() {
        lateinit var action: String
        val store = store(executor = TestExecutor { action = it })

        store.accept("intent")

        assertEquals("intent", action)
    }

    @Test
    fun isDisposed_returns_true_WHEN_store_disposed() {
        val store = store()

        store.dispose()

        assertTrue(store.isDisposed)
    }

    @Test
    fun state_observer_completed_WHEN_store_disposed() {
        var isComplete = false
        val store = store()
        store.addStateObserver(mviObserver(onComplete = { isComplete = true }))

        store.dispose()

        assertTrue(isComplete)
    }

    @Test
    fun label_observer_completed_WHEN_store_disposed() {
        var isComplete = false
        val store = store()
        store.addLabelObserver(mviObserver(onComplete = { isComplete = true }))

        store.dispose()

        assertTrue(isComplete)
    }

    @Test
    fun valid_state_read_for_last_intent_WHEN_two_intents_for_label() {
        lateinit var lastState: String

        val executor =
            TestExecutor {
                if (it == "intent2") {
                    lastState = stateSupplier()
                }
                resultConsumer(it)
            }

        val store = store(executor = executor)

        store.addLabelObserver(
            mviObserver(
                onNext = {
                    store.accept("intent1")
                    store.accept("intent2")
                }
            )
        )

        executor.labelConsumer("label")

        assertEquals("intent1", lastState)
    }

    private fun store(
        bootstrapper: MviBootstrapper<String>? = null,
        executor: TestExecutor = TestExecutor()
    ): MviDefaultStore<String, String, String, String, String> =
        MviDefaultStore(
            initialState = INITIAL_STATE,
            bootstrapper = bootstrapper,
            intentToAction = { it },
            executor = executor,
            reducer = object : MviReducer<String, String> {
                override fun String.reduce(result: String): String = result
            }
        )

    private companion object {
        private const val INITIAL_STATE = "state"
    }

    private class TestBootstrapper(
        private val onBootstrap: (dispatch: (String) -> Unit) -> Unit = {}
    ) : MviBootstrapper<String> {
        var isDisposed = false

        override fun bootstrap(dispatch: (String) -> Unit) {
            onBootstrap(dispatch)
        }

        override fun dispose() {
            isDisposed = true
        }
    }

    private class TestExecutor(
        private val onExecute: TestExecutor.(label: String) -> Unit = {}
    ) : MviExecutor<String, String, String, String> {
        var isInitialized = false
        var isDisposed = false
        lateinit var stateSupplier: () -> String
        lateinit var resultConsumer: (String) -> Unit
        lateinit var labelConsumer: (String) -> Unit

        override fun init(stateSupplier: () -> String, resultConsumer: (String) -> Unit, labelConsumer: (String) -> Unit) {
            isInitialized = true
            this.stateSupplier = stateSupplier
            this.resultConsumer = resultConsumer
            this.labelConsumer = labelConsumer
        }

        override fun executeAction(action: String) {
            onExecute(action)
        }

        override fun dispose() {
            isDisposed = true
        }
    }
}
