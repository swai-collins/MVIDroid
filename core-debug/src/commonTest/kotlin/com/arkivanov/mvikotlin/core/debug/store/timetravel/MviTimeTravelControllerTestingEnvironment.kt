package com.arkivanov.mvikotlin.core.debug.store.timetravel

import com.arkivanov.mvikotlin.base.observable.MviObservable
import com.arkivanov.mvikotlin.base.observable.MviPublishSubject
import com.arkivanov.mvikotlin.core.debug.store.MviEventType
import com.arkivanov.mvikotlin.core.debug.store.timetravel.MviTimeTravelStore.EventDebugger
import com.arkivanov.mvikotlin.core.debug.store.timetravel.MviTimeTravelStore.EventProcessor
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MviTimeTravelControllerTestingEnvironment {

    val store1Events = MviPublishSubject<MviTimeTravelEvent>()
    val store1EventProcessor = TestEventProcessor()
    val store1 = TestStore(store1Events, store1EventProcessor)

    private val store2Events = MviPublishSubject<MviTimeTravelEvent>()
    val store2EventProcessor = TestEventProcessor()
    val store2 = TestStore(store2Events, store2EventProcessor)

    val controller = MviTimeTravelController
    val state: MviTimeTravelState get() = controller.state
    val events: MviTimeTravelEvents get() = controller.events

    init {
        controller.attachStore(store1, "store1")
        controller.attachStore(store2, "store2")
    }

    fun release() {
        store1Events.onComplete()
        store2Events.onComplete()
        controller.cancel()
    }

    fun createIntentEventForStore1(value: String = "intent1", state: String = "state1"): MviTimeTravelEvent =
        MviTimeTravelEvent("store1", MviEventType.INTENT, value, state)

    fun createActionEventForStore1(value: String = "action1", state: String = "state1"): MviTimeTravelEvent =
        MviTimeTravelEvent("store1", MviEventType.ACTION, value, state)

    fun createResultEventForStore1(value: String = "result1", state: String = "state1"): MviTimeTravelEvent =
        MviTimeTravelEvent("store1", MviEventType.RESULT, value, state)

    fun createStateEventForStore1(value: String = "state1", state: String = "state1"): MviTimeTravelEvent =
        MviTimeTravelEvent("store1", MviEventType.STATE, value, state)

    fun createLabelEventForStore1(value: String = "label1", state: String = "state1"): MviTimeTravelEvent =
        MviTimeTravelEvent("store1", MviEventType.LABEL, value, state)

    fun createIntentEventForStore2(value: String = "intent2", state: String = "state1"): MviTimeTravelEvent =
        MviTimeTravelEvent("store2", MviEventType.INTENT, value, state)

    fun createActionEventForStore2(value: String = "action2", state: String = "state1"): MviTimeTravelEvent =
        MviTimeTravelEvent("store2", MviEventType.ACTION, value, state)

    fun createResultEventForStore2(value: String = "result2", state: String = "state1"): MviTimeTravelEvent =
        MviTimeTravelEvent("store2", MviEventType.RESULT, value, state)

    fun createStateEventForStore2(value: String = "state2", state: String = "state1"): MviTimeTravelEvent =
        MviTimeTravelEvent("store2", MviEventType.STATE, value, state)

    fun createLabelEventForStore2(value: String = "label2", state: String = "state1"): MviTimeTravelEvent =
        MviTimeTravelEvent("store2", MviEventType.LABEL, value, state)

    fun produceIntentEventForStore1(value: String = "intent1", state: String = "state1") {
        store1Events.onNext(createIntentEventForStore1(value, state))
    }

    fun produceActionEventForStore1(value: String = "action1", state: String = "state1") {
        store1Events.onNext(createActionEventForStore1(value, state))
    }

    fun produceResultEventForStore1(value: String = "result1", state: String = "state1") {
        store1Events.onNext(createResultEventForStore1(value, state))
    }

    fun produceStateEventForStore1(value: String = "state1", state: String = "state1") {
        store1Events.onNext(createStateEventForStore1(value, state))
    }

    fun produceLabelEventForStore1(value: String = "label1", state: String = "state1") {
        store1Events.onNext(createLabelEventForStore1(value, state))
    }

    fun produceIntentEventForStore2(value: String = "intent2", state: String = "state2") {
        store1Events.onNext(createIntentEventForStore2(value, state))
    }

    fun produceActionEventForStore2(value: String = "action2", state: String = "state2") {
        store1Events.onNext(createActionEventForStore2(value, state))
    }

    fun produceResultEventForStore2(value: String = "result2", state: String = "state2") {
        store1Events.onNext(createResultEventForStore2(value, state))
    }

    fun produceStateEventForStore2(value: String = "state2", state: String = "state2") {
        store1Events.onNext(createStateEventForStore2(value, state))
    }

    fun produceLabelEventForStore2(value: String = "label2", state: String = "state2") {
        store1Events.onNext(createLabelEventForStore2(value, state))
    }

    class TestStore(
        override val eventOutput: MviObservable<MviTimeTravelEvent>,
        override val eventProcessor: EventProcessor
    ) : MviTimeTravelStore<String, String, String> {
        override val eventDebugger: EventDebugger get() = TODO("not implemented")
        override val state: String get() = TODO("not implemented")
        override val stateOutput: MviObservable<String> get() = TODO("not implemented")
        override val labelOutput: MviObservable<String> get() = TODO("not implemented")
        override val isDisposed: Boolean get() = TODO("not implemented")
        var isStateRestored: Boolean = false

        override fun init() {
        }

        override fun restoreState() {
            isStateRestored = true
        }

        override fun dispose() {
        }

        override fun accept(intent: String) {
        }
    }

    class TestEventProcessor : EventProcessor {
        private val _calls = ArrayList<Pair<MviEventType, Any>>()
        val calls: List<Pair<MviEventType, Any>> = _calls

        override fun process(type: MviEventType, value: Any) {
            _calls.add(type to value)
        }

        fun assertCalled(type: MviEventType, value: Any) {
            assertTrue(_calls.contains(type to value))
        }

        fun assertNoCalls() {
            assertEquals(0, _calls.size)
        }

        fun reset() {
            _calls.clear()
        }
    }
}
