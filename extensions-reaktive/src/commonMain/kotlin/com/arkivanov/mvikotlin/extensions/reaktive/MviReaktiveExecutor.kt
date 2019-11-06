package com.arkivanov.mvikotlin.extensions.reaktive

import com.arkivanov.mvikotlin.base.store.MviAbstractExecutor
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable

abstract class MviReaktiveExecutor<State : Any, in Action : Any, Result : Any, Label : Any> :
    MviAbstractExecutor<State, Action, Result, Label>() {

    private val disposables = CompositeDisposable()

    final override fun executeAction(action: Action) {
        execute(action)?.also(disposables::add)
    }

    override fun dispose() {
        disposables.dispose()
    }

    abstract fun execute(action: Action): Disposable?
}
