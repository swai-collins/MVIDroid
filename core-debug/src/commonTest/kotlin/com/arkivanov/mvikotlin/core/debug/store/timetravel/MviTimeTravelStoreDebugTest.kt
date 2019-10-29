package com.arkivanov.mvikotlin.core.debug.store.timetravel

import com.arkivanov.mvikotlin.base.store.mviReducer
import com.arkivanov.mvikotlin.core.debug.store.test.TestExecutor
import com.arkivanov.mvikotlin.core.debug.store.test.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MviTimeTravelStoreDebugTest {

    private val env = MviTimeTravelStoreTestingEnvironment()

    @Test
    fun intentToAction_called_WHEN_debug_intent() {
        lateinit var action: String
        val store =
            env.store(
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
        val store = env.store(executorFactory = { executor })

        store.eventDebugger.debug(env.createIntentEvent())

        assertFalse(isCalled)
    }

    @Test
    fun new_executor_called_WHEN_debug_action() {
        lateinit var action: String
        val executors = ExecutorQueue(TestExecutor(), TestExecutor { action = it })
        val store = env.store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createActionEvent())

        assertEquals("action", action)
    }

    @Test
    fun old_executor_not_called_WHEN_debug_action() {
        var isCalled = false
        val executors = ExecutorQueue(TestExecutor { isCalled = true }, TestExecutor())
        val store = env.store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createActionEvent())

        assertFalse(isCalled)
    }

    @Test
    fun new_executor_reads_original_state_WHEN_debug_action() {
        val newExecutor = TestExecutor()
        val executors = ExecutorQueue(TestExecutor(), newExecutor)
        val store = env.store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createActionEvent(state = "old_state"))
        assertEquals("old_state", newExecutor.stateSupplier())
    }

    @Test
    fun new_executor_reads_new_state_WHEN_debug_action_and_result_dispatched_by_new_executor() {
        val newExecutor = TestExecutor()
        val executors = ExecutorQueue(TestExecutor(), newExecutor)
        val store = env.store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createActionEvent())
        newExecutor.resultConsumer("result")

        assertEquals("state_result", newExecutor.stateSupplier())
    }

    @Test
    fun old_executor_reads_old_state_WHEN_debug_action_and_result_dispatched_by_new_executor() {
        val oldExecutor = TestExecutor()
        val newExecutor = TestExecutor()
        val executors = ExecutorQueue(oldExecutor, newExecutor)
        val store = env.store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createActionEvent())
        newExecutor.resultConsumer("result")

        assertEquals("state", oldExecutor.stateSupplier())
    }

    @Test
    fun state_not_emitted_WHEN_debug_action_and_result_dispatched_by_new_executor() {
        val newExecutor = TestExecutor()
        val executors = ExecutorQueue(TestExecutor(), newExecutor)
        val store = env.store(executorFactory = executors::next)
        val stateObserver = store.stateOutput.test(skipFirstValue = true)

        store.eventDebugger.debug(env.createActionEvent())
        newExecutor.resultConsumer("result")

        assertTrue(stateObserver.values.isEmpty())
    }

    @Test
    fun state_not_changed_WHEN_debug_action_and_result_dispatched_by_new_executor() {
        val newExecutor = TestExecutor()
        val executors = ExecutorQueue(TestExecutor(), newExecutor)
        val store = env.store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createActionEvent())
        newExecutor.resultConsumer("result")

        assertEquals("state", store.state)
    }

    @Test
    fun label_not_emitted_WHEN_debug_action_and_label_published_by_new_executor() {
        val newExecutor = TestExecutor()
        val executors = ExecutorQueue(TestExecutor(), newExecutor)
        val store = env.store(executorFactory = executors::next)
        val labelObserver = store.labelOutput.test()

        store.eventDebugger.debug(env.createActionEvent())
        newExecutor.labelConsumer("label")

        assertTrue(labelObserver.values.isEmpty())
    }

    @Test
    fun reducer_called_with_original_state_and_result_WHEN_debug_result() {
        lateinit var state: String
        lateinit var result: String

        val store =
            env.store(
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
        val store = env.store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createResultEvent(state = "old_state"))

        assertEquals("state", oldExecutor.stateSupplier())
    }

    @Test
    fun state_not_emitted_WHEN_debug_result() {
        val executors = ExecutorQueue(TestExecutor(), TestExecutor())
        val store = env.store(executorFactory = executors::next)
        val stateObserver = store.stateOutput.test(skipFirstValue = true)

        store.eventDebugger.debug(env.createResultEvent(state = "old_state"))

        assertTrue(stateObserver.values.isEmpty())
    }

    @Test
    fun state_not_changed_WHEN_debug_result() {
        val executors = ExecutorQueue(TestExecutor(), TestExecutor())
        val store = env.store(executorFactory = executors::next)

        store.eventDebugger.debug(env.createResultEvent(state = "old_state"))

        assertEquals("state", store.state)
    }

    @Test
    fun exception_WHEN_debug_state() {
        val store = env.store()

        assertFailsWith<Exception> {
            store.eventDebugger.debug(env.createStateEvent())
        }
    }

    @Test
    fun label_emitted_WHEN_debug_label() {
        val store = env.store()
        val labelObserver = store.labelOutput.test()

        store.eventDebugger.debug(env.createLabelEvent())

        assertEquals(listOf("label"), labelObserver.values)
    }

    private class ExecutorQueue(
        private vararg val executors: TestExecutor
    ) {
        private var index = 0

        fun next(): TestExecutor = executors[index++]
    }
}
