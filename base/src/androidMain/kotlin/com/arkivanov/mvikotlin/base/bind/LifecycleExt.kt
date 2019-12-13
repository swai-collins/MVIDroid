package com.arkivanov.mvikotlin.base.bind

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.arkivanov.mvikotlin.base.observable.MviDisposable

fun Lifecycle.attach(binder: Binder) {
    binder
        .asLifecycleObserver(currentState)
        ?.also(::addObserver)
}

fun LifecycleOwner.attach(binder: Binder) {
    lifecycle.attach(binder)
}

operator fun Lifecycle.plusAssign(binder: Binder) {
    attach(binder)
}

operator fun LifecycleOwner.plusAssign(binder: Binder) {
    attach(binder)
}

fun Binder.attachTo(lifecycle: Lifecycle): Binder {
    lifecycle.attach(this)

    return this
}

fun Lifecycle.attach(disposable: MviDisposable) {
    addObserver(
        object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                disposable.dispose()
            }
        }
    )
}

operator fun Lifecycle.plusAssign(disposable: MviDisposable) {
    attach(disposable)
}

fun <T : MviDisposable> T.attachTo(lifecycle: Lifecycle): T {
    lifecycle.attach(this)

    return this
}

private fun Binder.asLifecycleObserver(currentState: Lifecycle.State): LifecycleObserver? =
    when (currentState) {
        Lifecycle.State.DESTROYED -> null

        Lifecycle.State.INITIALIZED ->
            object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    this@asLifecycleObserver.start()
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    this@asLifecycleObserver.stop()
                }
            }

        Lifecycle.State.CREATED ->
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    this@asLifecycleObserver.start()
                }

                override fun onStop(owner: LifecycleOwner) {
                    this@asLifecycleObserver.stop()
                }
            }

        Lifecycle.State.STARTED,
        Lifecycle.State.RESUMED ->
            object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    this@asLifecycleObserver.start()
                }

                override fun onPause(owner: LifecycleOwner) {
                    this@asLifecycleObserver.stop()
                }
            }
    }
