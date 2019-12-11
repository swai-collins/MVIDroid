package com.arkivanov.mvikotlin.extensions.reaktive.android

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.arkivanov.mvikotlin.extensions.reaktive.Binder

fun Binder.attachTo(lifecycle: Lifecycle) {
    lifecycle.attachBinder(this)
}

internal fun Binder.asLifecycleObserver(currentState: Lifecycle.State): LifecycleObserver? =
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
