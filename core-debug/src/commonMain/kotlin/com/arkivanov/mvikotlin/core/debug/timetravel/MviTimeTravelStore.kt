package com.arkivanov.mvikotlin.core.debug.timetravel

import com.arkivanov.mvikotlin.base.observable.MviObservable
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.store.MviEventType

internal interface MviTimeTravelStore<out State : Any, in Intent : Any, out Label : Any> : MviStore<State, Intent, Label> {

    val eventOutput: MviObservable<MviTimeTravelEvent>
    val eventProcessor: EventProcessor
    val eventDebugger: EventDebugger

    fun init()

    fun restoreState()

    interface EventProcessor {
        fun process(type: MviEventType, value: Any)
    }

    interface EventDebugger {
        fun debug(event: MviTimeTravelEvent)
    }
}
