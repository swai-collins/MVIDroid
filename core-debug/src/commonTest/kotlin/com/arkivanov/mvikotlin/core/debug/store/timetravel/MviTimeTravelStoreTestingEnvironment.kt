package com.arkivanov.mvikotlin.core.debug.store.timetravel

import com.arkivanov.mvikotlin.base.store.MviBootstrapper
import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.mviReducer
import com.arkivanov.mvikotlin.core.debug.store.MviEventType
import com.arkivanov.mvikotlin.core.debug.store.test.TestExecutor

internal class MviTimeTravelStoreTestingEnvironment {

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

    fun store(
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
}
