package com.arkivanov.mvikotlin.core.debug.store.timetravel

import com.arkivanov.mvikotlin.core.debug.store.MviEventType
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MviTimeTravelControllerIdleTest {

    private val env = MviTimeTravelControllerTestingEnvironment()

    @AfterTest
    fun after() {
        env.release()
    }

    @Test
    fun `initial state is idle`() {
        assertEquals(MviTimeTravelState.IDLE, env.state)
    }

    @Test
    fun `processes intent WHEN intent emitted in idle state`() {
        env.produceIntentEventForStore1()

        env.store1EventProcessor.assertCalled(MviEventType.INTENT, "intent1")
    }

    @Test
    fun `processes action WHEN action emitted in idle state`() {
        env.produceActionEventForStore1()

        env.store1EventProcessor.assertCalled(MviEventType.ACTION, "action1")
    }

    @Test
    fun `processes result WHEN result emitted in idle state`() {
        env.produceResultEventForStore1()

        env.store1EventProcessor.assertCalled(MviEventType.RESULT, "result1")
    }

    @Test
    fun `processes state WHEN state emitted in idle state`() {
        env.produceStateEventForStore1()

        env.store1EventProcessor.assertCalled(MviEventType.STATE, "state1")
    }

    @Test
    fun `processes label WHEN label emitted in idle state`() {
        env.produceLabelEventForStore1()

        env.store1EventProcessor.assertCalled(MviEventType.LABEL, "label1")
    }

    @Test
    fun `restores events`() {
        val events =
            listOf(
                env.createIntentEventForStore1(),
                env.createStateEventForStore1(value = "state_1_2", state = "state_1_1"),
                env.createIntentEventForStore2(),
                env.createStateEventForStore2(value = "state_2_2", state = "state_2_1"),
                env.createStateEventForStore1(value = "state_1_3", state = "state_1_2"),
                env.createStateEventForStore2(value = "state_2_3", state = "state_2_2")
            )

        env.controller.restoreEvents(MviTimeTravelEvents(items = events, index = 1))

        assertEquals(MviTimeTravelState.STOPPED, env.state)
        assertEquals(MviTimeTravelEvents(items = events, index = 5), env.events)
    }

    @Test
    fun `switched to last state for all stores WHEN restore events`() {
        env.controller.restoreEvents(
            MviTimeTravelEvents(
                items = listOf(
                    env.createIntentEventForStore1(),
                    env.createStateEventForStore1(value = "state_1_2", state = "state_1_1"),
                    env.createIntentEventForStore2(),
                    env.createStateEventForStore2(value = "state_2_2", state = "state_2_1"),
                    env.createStateEventForStore1(value = "state_1_3", state = "state_1_2"),
                    env.createStateEventForStore2(value = "state_2_3", state = "state_2_2")
                ),
                index = 1
            )
        )

        env.store1EventProcessor.assertCalled(MviEventType.STATE, "state_1_3")
        assertEquals(1, env.store1EventProcessor.calls.size)
        env.store2EventProcessor.assertCalled(MviEventType.STATE, "state_2_3")
        assertEquals(1, env.store2EventProcessor.calls.size)
    }
}
