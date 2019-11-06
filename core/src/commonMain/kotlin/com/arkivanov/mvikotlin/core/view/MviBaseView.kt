package com.arkivanov.mvikotlin.core.view

import com.arkivanov.mvikotlin.base.observable.MviObservable
import com.arkivanov.mvikotlin.base.observable.MviPublishSubject
import com.arkivanov.mvikotlin.base.view.MviView

/**
 * Base class for [MviView] implementation
 */
open class MviBaseView<ViewModel : Any, ViewEvent : Any> : MviView<ViewModel, ViewEvent> {

    private val modelsSubject = MviPublishSubject<ViewModel>()
    protected val models: MviObservable<ViewModel> = modelsSubject

    private val eventsSubject = MviPublishSubject<ViewEvent>()
    override val eventsOutput: MviObservable<ViewEvent> = eventsSubject

    override fun bind(model: ViewModel) {
        modelsSubject.onNext(model)
    }

    /**
     * Dispatches View Events
     */
    protected fun dispatch(event: ViewEvent) {
        eventsSubject.onNext(event)
    }
}
