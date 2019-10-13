package com.arkivanov.mvikotlin.base.store

interface MviBootstrapper<out Action : Any> {

    fun bootstrap(dispatch: (Action) -> Unit)

    fun dispose()
}