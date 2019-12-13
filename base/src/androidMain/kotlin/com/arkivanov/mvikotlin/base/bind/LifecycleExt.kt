package com.arkivanov.mvikotlin.base.bind

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

fun Lifecycle.attachBinder(binder: Binder) {
    binder
        .asLifecycleObserver(currentState)
        ?.also(::addObserver)
}

fun LifecycleOwner.attachBinder(binder: Binder) {
    lifecycle.attachBinder(binder)
}

operator fun Lifecycle.plusAssign(binder: Binder) {
    attachBinder(binder)
}

operator fun LifecycleOwner.plusAssign(binder: Binder) {
    attachBinder(binder)
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
