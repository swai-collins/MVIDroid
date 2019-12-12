package com.arkivanov.mvikotlin.extensions.reaktive

import com.arkivanov.mvikotlin.base.store.MviAbstractExecutor
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable

open class MviReaktiveExecutor<State : Any, in Intent : Any, Result : Any, Label : Any> :
    MviAbstractExecutor<State, Intent, Result, Label>() {

    private val disposables = CompositeDisposable()

    override fun dispose() {
        disposables.dispose()
    }

    override fun bootstrap() {
    }

    override fun execute(intent: Intent) {
    }

    protected fun <T : Disposable> T.scope(): T {
        disposables += this

        return this
    }
}
