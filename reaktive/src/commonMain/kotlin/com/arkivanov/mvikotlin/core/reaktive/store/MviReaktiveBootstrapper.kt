package com.arkivanov.mvikotlin.core.reaktive.store

import com.arkivanov.mvikotlin.base.store.MviBootstrapper
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable

abstract class MviReaktiveBootstrapper<out Action : Any> : MviBootstrapper<Action> {

    private val disposables = CompositeDisposable()

    final override fun bootstrap(dispatch: (Action) -> Unit) {
        execute(dispatch)?.also(disposables::add)
    }

    abstract fun execute(dispatch: (Action) -> Unit): Disposable?

    override fun dispose() {
        disposables.dispose()
    }
}
