package com.arkivanov.mvikotlin.core.debug.timetravel

import com.arkivanov.mvikotlin.base.store.MviExecutor
import com.arkivanov.mvikotlin.base.store.MviReducer
import com.arkivanov.mvikotlin.base.store.MviStore
import com.arkivanov.mvikotlin.base.store.MviStoreFactory

/**
 * An implementation of [MviStoreFactory] that creates Stores with time travel functionality.
 *
 * See [MviTimeTravelController], [MviTimeTravelView][com.arkivanov.mvidroid.widget.MviTimeTravelView]
 * and [MviTimeTravelDrawer][com.arkivanov.mvidroid.widget.MviTimeTravelDrawer] for more information.
 */
object MviTimeTravelStoreFactory : MviStoreFactory {

    override fun <State : Any, Intent : Any, Label : Any, Result : Any> create(
        name: String,
        initialState: State,
        executorFactory: () -> MviExecutor<State, Intent, Result, Label>,
        reducer: MviReducer<State, Result>
    ): MviStore<State, Intent, Label> =
        MviTimeTravelStoreImpl(
            name = name,
            initialState = initialState,
            executorFactory = executorFactory,
            reducer = reducer
        )
            .also { MviTimeTravelController.attachStore(it, name) }
}
