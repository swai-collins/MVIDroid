package com.arkivanov.mvikotlin.core.debug.store.timetravel

import com.arkivanov.mvikotlin.base.store.MviBootstrapper
import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.mviReducer
import com.arkivanov.mvikotlin.core.debug.store.test.TestExecutor
import com.arkivanov.mvikotlin.core.debug.store.test.TestObserver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MviTimeTravelStoreProcessTest {

    private val env = MviTimeTravelStoreTestingEnvironment()

    @Test
    fun intentToAction_called_WHEN_debug_intent() {
        lateinit var action: String
        val store =
            store(
                intentToAction = {
                    action = it
                    it
                }
            )

        store.eventDebugger.debug(env.createIntentEvent())

        assertEquals("intent", action)
    }

    @Test
    fun executor_not_called_WHEN_debug_intent() {
        var isCalled = false
        val executor = TestExecutor { isCalled = true }
        val store = store(executorFactory = { executor })

        store.eventDebugger.debug(env.createIntentEvent())

        assertFalse(isCalled)
    }

    @Test
    fun new_executor_called_WHEN_debug_action() {
        lateinit var action: String
        val executors = ExecutorQueue(TestExecutor(), TestExecutor { action = it })
        val store = store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createActionEvent())

        assertEquals("action", action)
    }

    @Test
    fun old_executor_not_called_WHEN_debug_action() {
        var isCalled = false
        val executors = ExecutorQueue(TestExecutor { isCalled = true }, TestExecutor())
        val store = store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createActionEvent())

        assertFalse(isCalled)
    }

    @Test
    fun new_executor_reads_original_state_WHEN_debug_action() {
        val newExecutor = TestExecutor()
        val executors = ExecutorQueue(TestExecutor(), newExecutor)
        val store = store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createActionEvent(state = "old_state"))
        assertEquals("old_state", newExecutor.stateSupplier())
    }

    @Test
    fun new_executor_reads_new_state_WHEN_debug_action_and_result_dispatched_by_new_executor() {
        val newExecutor = TestExecutor()
        val executors = ExecutorQueue(TestExecutor(), newExecutor)
        val store = store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createActionEvent())
        newExecutor.resultConsumer("result")

        assertEquals("state_result", newExecutor.stateSupplier())
    }

    @Test
    fun old_executor_reads_old_state_WHEN_debug_action_and_result_dispatched_by_new_executor() {
        val oldExecutor = TestExecutor()
        val newExecutor = TestExecutor()
        val executors = ExecutorQueue(oldExecutor, newExecutor)
        val store = store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createActionEvent())
        newExecutor.resultConsumer("result")

        assertEquals("state", oldExecutor.stateSupplier())
    }

    @Test
    fun state_not_emitted_WHEN_debug_action_and_result_dispatched_by_new_executor() {
        val newExecutor = TestExecutor()
        val executors = ExecutorQueue(TestExecutor(), newExecutor)
        val store = store(executorFactory = executors::next)
        val stateObserver = TestObserver<String>(skipFirstValue = true)
        store.stateOutput.subscribe(stateObserver)

        store.eventDebugger.debug(env.createActionEvent())
        newExecutor.resultConsumer("result")

        assertTrue(stateObserver.values.isEmpty())
    }

    @Test
    fun state_not_changed_WHEN_debug_action_and_result_dispatched_by_new_executor() {
        val newExecutor = TestExecutor()
        val executors = ExecutorQueue(TestExecutor(), newExecutor)
        val store = store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createActionEvent())
        newExecutor.resultConsumer("result")

        assertEquals("state", store.state)
    }

    @Test
    fun label_not_emitted_WHEN_debug_action_and_label_published_by_new_executor() {
        val newExecutor = TestExecutor()
        val executors = ExecutorQueue(TestExecutor(), newExecutor)
        val store = store(executorFactory = executors::next)
        val labelObserver = TestObserver<String>()
        store.labelOutput.subscribe(labelObserver)

        store.eventDebugger.debug(env.createActionEvent())
        newExecutor.labelConsumer("label")

        assertTrue(labelObserver.values.isEmpty())
    }

    @Test
    fun reducer_called_with_original_state_and_result_WHEN_debug_result() {
        lateinit var state: String
        lateinit var result: String

        val store =
            store(
                reducer = mviReducer {
                    state = this
                    result = it
                    this
                }
            )

        store.eventDebugger.debug(env.createResultEvent(state = "old_state"))

        assertEquals("old_state", state)
        assertEquals("result", result)
    }

    @Test
    fun old_executor_reads_main_state_WHEN_debug_result() {
        val oldExecutor = TestExecutor()
        val executors = ExecutorQueue(oldExecutor, TestExecutor())
        val store = store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createResultEvent(state = "old_state"))

        assertEquals("state", oldExecutor.stateSupplier())
    }

    @Test
    fun state_not_emitted_WHEN_debug_result() {
        val executors = ExecutorQueue(TestExecutor(), TestExecutor())
        val store = store(executorFactory = executors::next)
        val stateObserver = TestObserver<String>(skipFirstValue = true)
        store.stateOutput.subscribe(stateObserver)

        store.eventDebugger.debug(env.createResultEvent(state = "old_state"))

        assertTrue(stateObserver.values.isEmpty())
    }

    @Test
    fun state_not_changed_WHEN_debug_result() {
        val executors = ExecutorQueue(TestExecutor(), TestExecutor())
        val store = store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createResultEvent(state = "old_state"))

        assertEquals("state", store.state)
    }

    @Test
    fun exception_WHEN_debug_state() {
        val store = store()

        assertFailsWith<Exception> {
            store.eventDebugger.debug(env.createStateEvent())
        }
    }

    @Test
    fun label_emitted_WHEN_debug_label() {
        val store = store()
        val labelObserver = TestObserver<String>()
        store.labelOutput.subscribe(labelObserver)

        store.eventDebugger.debug(env.createLabelEvent())

        assertEquals(listOf("label"), labelObserver.values)
    }

    private fun store(
        init: Boolean = true,
        bootstrapper: MviBootstrapper<String>? = null,
        intentToAction: (String) -> String = { it },
        executorFactory: () -> TestExecutor = { TestExecutor() },
        reducer: MviReducer<String, String> = mviReducer { "${this}_$it" }
    ): MviTimeTravelStore<String, String, String, String, String> {
        val store =
            MviTimeTravelStore(
                name = "store",
                initialState = "state",
                bootstrapper = bootstrapper,
                intentToAction = intentToAction,
                executorFactory = executorFactory,
                reducer = reducer
            )

        if (init) {
            store.init()
        }

        return store
    }

    private class ExecutorQueue(
        private vararg val executors: TestExecutor
    ) {
        private var index = 0

        fun next(): TestExecutor = executors[index++]
    }
}
