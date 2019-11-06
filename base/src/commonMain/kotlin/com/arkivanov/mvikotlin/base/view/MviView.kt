package com.arkivanov.mvikotlin.base.view

import com.arkivanov.mvikotlin.base.observable.MviObservable

/**
 * Interface of View, accepts View Models and produces View Events.
 * See [MviBinder][com.arkivanov.mvidroid.bind.MviBinder] to find out how to bind Components with Views.
 *
 * @param ViewModel type of View Model, typically a data class
 * @param ViewEvent type of View Events
 */
interface MviView<in ViewModel, out ViewEvent> {

    /**
     * Observable of View Events, emissions must be performed only on Main thread
     */
    val eventsOutput: MviObservable<ViewEvent>

    /**
     * Called when a new View Model is available, called on Main thread
     *
     * @param model a View Model
     */
    fun bind(model: ViewModel)
}
