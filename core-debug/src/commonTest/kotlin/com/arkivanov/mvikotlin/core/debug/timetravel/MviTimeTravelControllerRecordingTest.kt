package com.arkivanov.mvikotlin.core.debug.timetravel

import com.arkivanov.mvikotlin.base.store.MviEventType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MviTimeTravelControllerRecordingTest {

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
    fun `state is recording WHEN recording started`() {
        assertEquals(MviTimeTravelState.RECORDING, env.state)
    }

    @Test
    fun `processes intent WHEN intent emitted in recording state`() {
        env.produceIntentEventForStore1()

        env.store1EventProcessor.assertCalled(MviEventType.INTENT, "intent1")
    }

    @Test
    fun `processes action WHEN action emitted in recording state`() {
        env.produceActionEventForStore1()

        env.store1EventProcessor.assertCalled(MviEventType.ACTION, "action1")
    }

    @Test
    fun `processes result WHEN result emitted in recording state`() {
        env.produceResultEventForStore1()

        env.store1EventProcessor.assertCalled(MviEventType.RESULT, "result1")
    }

    @Test
    fun `processes state WHEN state emitted in recording state`() {
        env.produceStateEventForStore1()

        env.store1EventProcessor.assertCalled(MviEventType.STATE, "state1")
    }

    @Test
    fun `processes label WHEN label emitted in recording state`() {
        env.produceLabelEventForStore1()

        env.store1EventProcessor.assertCalled(MviEventType.LABEL, "label1")
    }

    @Test
    fun `events added to list in order WHEN emitted by store`() {
        env.produceIntentEventForStore1()
        env.produceActionEventForStore1()
        env.produceResultEventForStore1()
        env.produceStateEventForStore1()
        env.produceLabelEventForStore1()

        assertEquals(
            listOf(
                env.createIntentEventForStore1(),
                env.createActionEventForStore1(),
                env.createResultEventForStore1(),
                env.createStateEventForStore1(),
                env.createLabelEventForStore1()
            ),
            env.events.items
        )
    }

    @Test
    fun `in idle state WHEN cancelled without events`() {
        env.controller.cancel()

        assertEquals(MviTimeTravelState.IDLE, env.state)
    }

    @Test
    fun `in idle state WHEN cancelled with events`() {
        env.produceIntentEventForStore1()

        env.controller.cancel()

        assertEquals(MviTimeTravelState.IDLE, env.state)
    }

    @Test
    fun `in idle state WHEN stopped without events`() {
        env.controller.stop()

        assertEquals(MviTimeTravelState.IDLE, env.state)
    }
}
