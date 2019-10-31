package com.arkivanov.mvikotlin.core.debug.timetravel

import com.arkivanov.mvikotlin.base.store.MviEventType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MviTimeTravelControllerStoppedTest {

    private val env = MviTimeTravelControllerTestingEnvironment()

    @BeforeTest
    fun before() {
        env.controller.startRecording()
    }

    @AfterTest
    fun after() {
        env.release()
    }

    @Test
    fun `in stopped state WHEN stopped with events`() {
        env.produceIntentEventForStore1()
        env.controller.stop()
        assertEquals(MviTimeTravelState.STOPPED, env.state)
    }

    @Test
    fun `in idle state WHEN stopped and cancelled`() {
        env.produceIntentEventForStore1()
        env.controller.stop()
        env.controller.cancel()
        assertEquals(MviTimeTravelState.IDLE, env.state)
    }

    @Test
    fun `points to last event WHEN recorded and not stopped`() {
        env.produceIntentEventForStore1()
        env.produceActionEventForStore2()
        env.produceResultEventForStore1()
        env.produceStateEventForStore2()
        env.produceLabelEventForStore1()
        assertEquals(4, env.events.index)
    }

    @Test
    fun `points to last event WHEN recorded and not stopped and step backward`() {
        env.produceStateEventForStore1()
        env.produceResultEventForStore1()
        env.controller.stepBackward()
        assertEquals(1, env.events.index)
    }

    @Test
    fun `points to previous state WHEN stopped and step backward`() {
        env.produceIntentEventForStore1()
        env.produceActionEventForStore2()
        env.produceResultEventForStore1()
        env.produceStateEventForStore2()
        env.produceLabelEventForStore1()
        env.controller.stop()
        env.controller.stepBackward()
        assertEquals(3, env.events.index)
    }

    @Test
    fun `points to past previous state WHEN stopped and step backward twice`() {
        env.produceIntentEventForStore1()
        env.produceActionEventForStore1()
        env.produceResultEventForStore1()
        env.produceStateEventForStore1()
        env.produceLabelEventForStore1()
        env.produceIntentEventForStore2()
        env.produceActionEventForStore2()
        env.produceResultEventForStore2()
        env.produceStateEventForStore2()
        env.produceLabelEventForStore2()
        env.controller.stop()
        env.controller.stepBackward()
        env.controller.stepBackward()
        assertEquals(3, env.events.index)
    }

    @Test
    fun `points to start WHEN stopped and step backward until end`() {
        env.produceStateEventForStore1()
        env.produceResultEventForStore1()
        env.controller.stop()
        env.controller.stepBackward()
        env.controller.stepBackward()
        assertEquals(-1, env.events.index)
    }

    @Test
    fun `points to last state WHEN stopped and step backward and step forward`() {
        env.produceStateEventForStore2()
        env.produceStateEventForStore1()
        env.produceResultEventForStore1()
        env.controller.stop()
        env.controller.stepBackward()
        env.controller.stepForward()
        assertEquals(1, env.events.index)
    }

    @Test
    fun `points to start WHEN stopped and move to start`() {
        env.produceStateEventForStore2()
        env.produceStateEventForStore1()
        env.produceResultEventForStore1()
        env.controller.stop()
        env.controller.moveToStart()
        assertEquals(-1, env.events.index)
    }

    @Test
    fun `points to last event WHEN recorded and move to start and move to end`() {
        env.produceStateEventForStore2()
        env.produceStateEventForStore1()
        env.produceResultEventForStore1()
        env.controller.stop()
        env.controller.moveToStart()
        env.controller.moveToEnd()
        assertEquals(2, env.events.index)
    }

    @Test
    fun `no events processed WHEN moved from end to last state`() {
        env.produceStateEventForStore1()
        env.produceResultEventForStore1()
        env.controller.stop()
        env.store1EventProcessor.reset()
        env.controller.stepBackward()

        env.store1EventProcessor.assertNoCalls()
    }

    @Test
    fun `previous state processed WHEN moved from last state to previous event`() {
        env.produceResultEventForStore1()
        env.produceStateEventForStore2(state = "previous_state")
        env.controller.stop()
        env.store2EventProcessor.reset()
        env.controller.stepBackward()

        env.store2EventProcessor.assertCalled(MviEventType.STATE, "previous_state")
    }

    @Test
    fun `state processed WHEN moved from event past state to state`() {
        env.produceResultEventForStore1()
        env.produceStateEventForStore2(state = "previous_state")
        env.controller.stop()
        env.controller.stepBackward()
        env.store2EventProcessor.reset()
        env.controller.stepForward()
        env.store2EventProcessor.assertCalled(MviEventType.STATE, "state2")
    }

    @Test
    fun `switched to first state for all stores WHEN moved from start`() {
        env.produceResultEventForStore1()
        env.produceStateEventForStore1(value = "state_1_2", state = "state_1_1")
        env.produceResultEventForStore2()
        env.produceStateEventForStore2(value = "state_2_2", state = "state_2_1")
        env.produceResultEventForStore1()
        env.produceStateEventForStore1(value = "state_1_3", state = "state_1_2")
        env.produceResultEventForStore2()
        env.produceStateEventForStore2(value = "state_2_3", state = "state_2_2")
        env.produceResultEventForStore1()
        env.produceResultEventForStore2()
        env.controller.stop()
        env.store1EventProcessor.reset()
        env.store2EventProcessor.reset()
        env.controller.moveToStart()
        env.store1EventProcessor.assertCalled(MviEventType.STATE, "state_1_1")
        assertEquals(1, env.store1EventProcessor.calls.size)
        env.store2EventProcessor.assertCalled(MviEventType.STATE, "state_2_1")
        assertEquals(1, env.store2EventProcessor.calls.size)
    }

    @Test
    fun `switched to last state for all stores WHEN moved from start to last event`() {
        env.produceResultEventForStore1()
        env.produceStateEventForStore1(value = "state_1_2", state = "state_1_1")
        env.produceResultEventForStore2()
        env.produceStateEventForStore2(value = "state_2_2", state = "state_2_1")
        env.produceResultEventForStore1()
        env.produceStateEventForStore1(value = "state_1_3", state = "state_1_2")
        env.produceResultEventForStore2()
        env.produceStateEventForStore2(value = "state_2_3", state = "state_2_2")
        env.produceResultEventForStore1()
        env.produceResultEventForStore2()
        env.controller.stop()
        env.controller.moveToStart()
        env.store1EventProcessor.reset()
        env.store2EventProcessor.reset()
        env.controller.moveToEnd()
        env.store1EventProcessor.assertCalled(MviEventType.STATE, "state_1_3")
        assertEquals(1, env.store1EventProcessor.calls.size)
        env.store2EventProcessor.assertCalled(MviEventType.STATE, "state_2_3")
        assertEquals(1, env.store2EventProcessor.calls.size)
    }

    @Test
    fun `second store state processed WHEN first store disposed after stopped`() {
        env.produceStateEventForStore1(value = "state_1_2", state = "state_1_1")
        env.produceStateEventForStore2(value = "state_2_2", state = "state_2_1")
        env.controller.stop()
        env.store1Events.onComplete()
        env.store2EventProcessor.reset()
        env.controller.moveToStart()
        env.store2EventProcessor.assertCalled(MviEventType.STATE, "state_2_1")
    }

    @Test
    fun `first store ignored WHEN disposed after stopped`() {
        env.produceStateEventForStore1(value = "state_1_2", state = "state_1_1")
        env.produceStateEventForStore2(value = "state_2_2", state = "state_2_1")
        env.controller.stop()
        env.store1Events.onComplete()
        env.store1EventProcessor.reset()
        env.controller.moveToStart()
        env.store1EventProcessor.assertNoCalls()
    }

    @Test
    fun `state restored in all stores WHEN recorded and cancelled`() {
        env.produceResultEventForStore1()
        env.produceResultEventForStore2()
        env.controller.stop()
        env.controller.cancel()
        assertTrue(env.store1.isStateRestored)
        assertTrue(env.store2.isStateRestored)
    }
}
