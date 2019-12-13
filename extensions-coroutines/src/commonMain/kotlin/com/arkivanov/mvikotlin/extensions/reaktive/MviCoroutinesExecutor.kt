package com.arkivanov.mvikotlin.extensions.reaktive

import com.arkivanov.mvikotlin.base.store.MviAbstractExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

open class MviCoroutinesExecutor<State : Any, in Intent : Any, Result : Any, Label : Any> :
    MviAbstractExecutor<State, Intent, Result, Label>() {

    private val scope = CoroutineScope(GlobalScope.coroutineContext)

    override fun dispose() {
        cancel()
    }

    final override fun bootstrap() {
        launch {  }
    }

    open suspend fun bootstrapExecutor() {
    }

    final override fun execute(intent: Intent) {
    }

    open suspend fun executeSuspendable(intent: Intent) {
    }
}

class Kek : MviCoroutinesExecutor<String, String, String, String>() {
    override suspend fun bootstrapSuspendable() {

    }
}

suspend fun foo() {
    coroutineScope {

    }
}
