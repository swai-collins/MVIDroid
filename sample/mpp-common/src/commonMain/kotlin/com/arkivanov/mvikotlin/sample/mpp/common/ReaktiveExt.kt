package com.arkivanov.mvikotlin.sample.mpp.common

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observable
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.scheduler.ioScheduler
import com.squareup.sqldelight.Query

fun <T : Any> Query<T>.asObservable(scheduler: Scheduler = ioScheduler): Observable<Query<T>> =
    observable<Query<T>> { emitter ->
        val listener =
            object : Query.Listener {
                override fun queryResultsChanged() {
                    emitter.onNext(this@asObservable)
                }
            }

        emitter.setDisposable(Disposable { removeListener(listener) })
        addListener(listener)
        emitter.onNext(this)
    }
        .observeOn(scheduler)
