package com.arkivanov.mvikotlin.core.debug.timetravel

import com.arkivanov.mvikotlin.base.store.MviEventType
import com.arkivanov.mvikotlin.core.debug.test.TestExecutor
import com.arkivanov.mvikotlin.core.debug.test.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MviTimeTravelStoreProcessTest {

    private val env = MviTimeTravelStoreTestingEnvironment()

    @Test
    fun valid_intent_event_emitted_WHEN_intent_sent() {
        val store = env.store()
        val events = store.eventOutput.test()

        store.accept("intent")

        assertEquals(listOf(env.createIntentEvent()), events.values)
    }

    @Test
    fun valid_action_event_emitted_WHEN_precess_intent() {
        val store = env.store(intentToAction = { if (it == "intent") "action" else "" })
        val events = store.eventOutput.test()

        store.eventProcessor.process(MviEventType.INTENT, "intent")

        assertEquals(listOf(env.createActionEvent()), events.values)
    }

    @Test
    fun executor_executed_with_valid_action_WHEN_process_action() {
        lateinit var action: String
        val store = env.store(executorFactory = { TestExecutor { action = it } })

        store.eventProcessor.process(MviEventType.ACTION, "action")

        assertEquals("action", action)
    }

    @Test
    fun valid_result_event_emitted_WHEN_executor_dispatched_result() {
        val executor = TestExecutor()
        val store = env.store(executorFactory = { executor })
        val events = store.eventOutput.test()

        executor.resultConsumer("result")

        assertEquals(listOf(env.createResultEvent()), events.values)
    }

    @Test
    fun valid_state_events_emitted_WHEN_process_result() {
        val store = env.store()
        val events = store.eventOutput.test()

        store.eventProcessor.process(MviEventType.RESULT, "result1")
        store.eventProcessor.process(MviEventType.RESULT, "result2")

        assertEquals(
            listOf(
                env.createStateEvent(value = "state_result1", state = "state"),
                env.createStateEvent(value = "state_result1_result2", state = "state_result1")
            ),
            events.values
        )
    }

    @Test
    fun executor_reads_new_state_WHEN_process_result() {
        val executor = TestExecutor()
        val store = env.store(executorFactory = { executor })

        store.eventProcessor.process(MviEventType.RESULT, "result1")
        store.eventProcessor.process(MviEventType.RESULT, "result2")

        assertEquals("state_result1_result2", executor.stateSupplier())
    }

    @Test
    fun store_state_not_emitted_WHEN_process_result() {
        val store = env.store()
        val states = store.stateOutput.test(skipFirstValue = true)

        store.eventProcessor.process(MviEventType.RESULT, "result")

        assertTrue(states.values.isEmpty())
    }

    @Test
    fun store_state_not_changed_WHEN_process_result() {
        val store = env.store()

        store.eventProcessor.process(MviEventType.RESULT, "result")

        assertEquals("state", store.state)
    }

    @Test
    fun valid_state_emitted_WHEN_process_state() {
        val store = env.store()
        val states = store.stateOutput.test(skipFirstValue = true)

        store.eventProcessor.process(MviEventType.STATE, "new state")

        assertEquals(listOf("new state"), states.values)
    }

    @Test
    fun valid_state_WHEN_process_state() {
        val store = env.store()

        store.eventProcessor.process(MviEventType.STATE, "new state")

        assertEquals("new state", store.state)
    }

    @Test
    fun valid_label_event_emitted_WHEN_executor_published_label() {
        val executor = TestExecutor()
        val store = env.store(executorFactory = { executor })
        val events = store.eventOutput.test()

        executor.labelConsumer("label")

        assertEquals(listOf(env.createLabelEvent()), events.values)
    }

    @Test
    fun label_not_emitted_WHEN_executor_published_label() {
        val executor = TestExecutor()
        val store = env.store(executorFactory = { executor })
        val labels = store.labelOutput.test()

        executor.labelConsumer("label")

        assertTrue(labels.values.isEmpty())
    }

    @Test
    fun label_emitted_WHEN_process_label() {
        val store = env.store()
        val labels = store.labelOutput.test()

        store.eventProcessor.process(MviEventType.LABEL, "label")

        assertEquals(listOf("label"), labels.values)
    }

    @Test
    fun restored_actual_state_WHEN_process_result_AND_precess_state() {
        val store = env.store()

        store.eventProcessor.process(MviEventType.RESULT, "result")
        store.eventProcessor.process(MviEventType.STATE, "state")
        store.restoreState()

        assertEquals("state_result", store.state)
    }
}
