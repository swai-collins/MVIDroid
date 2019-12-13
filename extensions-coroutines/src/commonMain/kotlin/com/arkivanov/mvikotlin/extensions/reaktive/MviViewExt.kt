package com.arkivanov.mvikotlin.extensions.reaktive

import com.arkivanov.mvikotlin.base.view.MviView
import com.badoo.reaktive.observable.Observable

val <Event : Any> MviView<*, Event>.events: Observable<Event> get() = eventsOutput.asReaktive()
