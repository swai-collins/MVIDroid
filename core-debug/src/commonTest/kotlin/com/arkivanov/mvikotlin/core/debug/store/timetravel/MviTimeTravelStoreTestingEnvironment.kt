package com.arkivanov.mvikotlin.core.debug.store.timetravel

import com.arkivanov.mvikotlin.base.store.MviBootstrapper
import com.arkivanov.mvikotlin.base.store.MviExecutor
import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.mviReducer
import com.arkivanov.mvikotlin.core.debug.store.MviEventType
import kotlin.test.assertEquals

internal class MviTimeTravelStoreTestingEnvironment {

//    val intentToAction = mock<(String) -> String>()
//    val executor = TestExecutor()
//    val newExecutor = TestExecutor()
//    val reducer = mviReducer<String, String> { this + it }
//    lateinit var store: MviTimeTravelStore<String, String, String, String, String>
//    private lateinit var receivedEvents: TestObserver<Any>
//
//    init {
//        createStore()
//    }

    fun createIntentEvent(value: String = "intent", state: String = "state"): MviTimeTravelEvent =
        MviTimeTravelEvent("store", MviEventType.INTENT, value, state)

    fun createActionEvent(value: String = "action", state: String = "state"): MviTimeTravelEvent =
        MviTimeTravelEvent("store", MviEventType.ACTION, value, state)

    fun createResultEvent(value: String = "result", state: String = "state"): MviTimeTravelEvent =
        MviTimeTravelEvent("store", MviEventType.RESULT, value, state)

    fun createStateEvent(value: String = "state", state: String = "state"): MviTimeTravelEvent =
        MviTimeTravelEvent("store", MviEventType.STATE, value, state)

    fun createLabelEvent(value: String = "label", state: String = "state"): MviTimeTravelEvent =
        MviTimeTravelEvent("store", MviEventType.LABEL, value, state)

//    fun assertEvents(vararg events: MviTimeTravelEvent) {
//        assertEquals(listOf(*events), receivedEvents.values())
//    }

//    private fun createStore(
//        bootstrapper: MviBootstrapper<String>? = null,
//        executorFactory: () -> TestExecutor
//    ) {
//        store = MviTimeTravelStore(
//            name = "store",
//            initialState = "state",
//            bootstrapper = bootstrapper,
//            intentToAction = intentToAction,
//            executorFactory = executorFactory,
//            reducer = reducer
//        )
//        receivedEvents = TestObserver<Any>().also { store.events.subscribe(it) }
//        store.init()
//    }


}
