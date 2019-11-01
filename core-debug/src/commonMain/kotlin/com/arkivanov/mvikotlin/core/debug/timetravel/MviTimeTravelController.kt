package com.arkivanov.mvikotlin.core.debug.timetravel

import com.arkivanov.mvikotlin.base.observable.MviBehaviorSubject
import com.arkivanov.mvikotlin.base.observable.MviObservable
import com.arkivanov.mvikotlin.base.observable.subscribe
import com.arkivanov.mvikotlin.base.store.MviEventType
import com.arkivanov.mvikotlin.base.utils.assertOnMainThread
import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.update
import kotlin.native.concurrent.ThreadLocal

/**
 * Provides methods to control time travel feature.
 * Time travel is a very powerful debug tool. With time travel you can:
 * * record all events of all active Stores
 * * view and explore events
 * * step back and forward over the recorded events
 * * fire any event again to debug
 *
 * See [MviTimeTravelStoreFactory], [MviTimeTravelView][com.arkivanov.mvidroid.widget.MviTimeTravelView]
 * and [MviTimeTravelDrawer][com.arkivanov.mvidroid.widget.MviTimeTravelDrawer] for more information.
 */
@ThreadLocal
object MviTimeTravelController {

    private val statesSubject = MviBehaviorSubject(MviTimeTravelState.IDLE)
    private val eventsSubject = MviBehaviorSubject(MviTimeTravelEvents())
    private val postponedEvents = ArrayList<MviTimeTravelEvent>()
    private val stores = AtomicReference<Map<String, MviTimeTravelStore<*, *, *>>>(emptyMap())

    /**
     * Returns current time travel state, see [MviTimeTravelState] for more information
     */
    val state: MviTimeTravelState get() = statesSubject.value

    /**
     * Returns current time travel events, see [MviTimeTravelEvents] for more information
     */
    val events: MviTimeTravelEvents get() = eventsSubject.value

    /**
     * Observable of time travel state, see [MviTimeTravelState] for more information
     */
    val stateOutput: MviObservable<MviTimeTravelState> =
        statesSubject

    /**
     * Obserable of time travel events, see [MviTimeTravelEvents] for more information
     */
    val eventsOutput: MviObservable<MviTimeTravelEvents> =
        eventsSubject

    internal fun <State : Any, Intent : Any, Action : Any> attachStore(
        store: MviTimeTravelStore<State, Intent, Action>,
        storeName: String
    ) {
        assertOnMainThread()

        if (stores.value.containsKey(storeName)) {
            throw IllegalStateException("Duplicate store: $storeName")
        } else {
            stores.update { it + (storeName to store) }
        }

        store.eventOutput.subscribe(
            onComplete = { stores.update { it - storeName } },
            onNext = MviTimeTravelController::onEvent
        )

        store.init()
    }

    /**
     * Sets current state to [MviTimeTravelState.STOPPED] and replaces any existing events with the provided ones
     */
    fun restoreEvents(events: MviTimeTravelEvents) {
        assertOnMainThread()

        if (events.items.isNotEmpty()) {
            statesSubject.onNext(MviTimeTravelState.STOPPED)
            eventsSubject.onNext(events.copy(index = -1))
            moveToEnd()
        }
    }

    /**
     * Starts event recording
     */
    fun startRecording() {
        assertOnMainThread()

        if (state === MviTimeTravelState.IDLE) {
            statesSubject.onNext(MviTimeTravelState.RECORDING)
        }
    }

    /**
     * Stops event recording and switches to STOPPED state if at least one event was recorded or
     * to IDLE state if no events were recorded
     */
    fun stop() {
        assertOnMainThread()

        if (state === MviTimeTravelState.RECORDING) {
            statesSubject.onNext(if (events.items.isNotEmpty()) MviTimeTravelState.STOPPED else MviTimeTravelState.IDLE)
        }
    }

    /**
     * Moves to the beginning of the events (right before first event, event index will be -1)
     */
    fun moveToStart() {
        assertOnMainThread()

        if (state === MviTimeTravelState.STOPPED) {
            move(
                events,
                -1
            )
        }
    }

    /**
     * Steps to the previous STATE event, or to the beginning of the events (right before first event, event index will be -1)
     */
    fun stepBackward() {
        assertOnMainThread()

        if (state === MviTimeTravelState.STOPPED) {
            step(
                events,
                false
            )
        }
    }

    /**
     * Steps to the next STATE event
     */
    fun stepForward() {
        assertOnMainThread()

        if (state === MviTimeTravelState.STOPPED) {
            step(
                events,
                true
            )
        }
    }

    /**
     * Moves to the end of the events
     */
    fun moveToEnd() {
        assertOnMainThread()

        if (state === MviTimeTravelState.STOPPED) {
            move(
                events,
                events.items.lastIndex
            )
        }
    }

    /**
     * Cancels time travel session and switches to IDLE state
     */
    fun cancel() {
        assertOnMainThread()

        if (state !== MviTimeTravelState.IDLE) {
            eventsSubject.onNext(MviTimeTravelEvents())
            val oldState = state
            statesSubject.onNext(MviTimeTravelState.IDLE)

            if (oldState !== MviTimeTravelState.RECORDING) {
                stores.value.values.forEach(MviTimeTravelStore<*, *, *>::restoreState)
                postponedEvents.forEach {
                    process(
                        it
                    )
                }
            }

            postponedEvents.clear()
        }
    }

    /**
     * Fires the provided event allowing its debugging.
     * Please note that events of type STATE can not be debugged.
     * * If event type is INTENT, executes intentToAction function of the appropriate Store. Resulting Action will be dropped.
     * * If event type is ACTION, executes an Executor of the appropriate Store.
     * A new temporary instance of Executor will be created, its State will be same as when original event was recorded,
     * any dispatched Resutls will be redirected to the Reducer and State of this temporary Executor will be updated,
     * any dispatched Labels will be dropped.
     * Original Executor will not be executed and state of the Store will not be changed.
     * * If event type is RESULT, executes a Reducer of the appropriate Store. Resulting State will be dropped.
     * * If event type is STATE, throws an exception as events of type STATE can not be debugged
     * * If event type is LABEL, emits the Label from the appropriate Store
     */
    fun debugEvent(event: MviTimeTravelEvent) {
        assertOnMainThread()

        stores.value[event.storeName]?.eventDebugger?.debug(event)
    }

    private fun onEvent(event: MviTimeTravelEvent) {
        when {
            state === MviTimeTravelState.RECORDING -> {
                eventsSubject.onNext(events.copy(items = events.items + event, index = events.items.size))
                process(event)
            }

            state === MviTimeTravelState.IDLE -> process(
                event
            )

            state === MviTimeTravelState.STOPPED -> {
                if (event.type === MviEventType.RESULT) {
                    process(event)
                } else {
                    postponedEvents.add(event)
                }
            }
        }
    }

    private fun step(events: MviTimeTravelEvents, isForward: Boolean) {
        with(events) {
            val progression =
                if (isForward) {
                    index + 1..items.lastIndex
                } else {
                    index - 1 downTo -1
                }

            for (i in progression) {
                val item = items.getOrNull(i)
                if ((item == null) || (item.type === MviEventType.STATE)) {
                    move(events, i)
                    break
                }
            }
        }
    }

    private fun move(events: MviTimeTravelEvents, to: Int, publish: Boolean = true) {
        val from = events.index
        if (from == to) {
            return
        }

        val set = HashSet<String>()
//        val deque: Deque<MviTimeTravelEvent> = LinkedList()
        val deque = ArrayList<MviTimeTravelEvent>()
        val isForward = to > from
        val progression =
            if (isForward) {
                to downTo from + 1
            } else {
                to + 1..from
            }

        for (i in progression) {
            val event = events.items[i]
            if ((event.type === MviEventType.STATE) && stores.value.containsKey(event.storeName) && !set.contains(event.storeName)) {
                set.add(event.storeName)
                deque += event
                if (set.size == stores.value.size) {
                    break
                }
            }
        }
        while (!deque.isEmpty()) {
            deque.removeAt(0).also { event ->
                if ((event.type === MviEventType.STATE) && !isForward) {
                    process(event, event.state)
                } else {
                    process(event)
                }
            }
        }

        if (publish) {
            eventsSubject.onNext(events.copy(index = to))
        }
    }

    private fun process(event: MviTimeTravelEvent, previousValue: Any? = null) {
        stores.value[event.storeName]?.eventProcessor?.process(event.type, previousValue ?: event.value)
    }
}
