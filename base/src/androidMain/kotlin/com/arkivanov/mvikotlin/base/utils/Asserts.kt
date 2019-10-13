package com.arkivanov.mvikotlin.base.utils

import android.os.Looper

private val mainThreadId: Long? by lazy {
    try {
        Looper.getMainLooper().thread.id
    } catch (ignored: Exception) {
        null
    }
}

actual fun assertOnMainThread() {
    val currentThread = Thread.currentThread()
    if ((mainThreadId != null) && (currentThread.id != mainThreadId)) {
        throw RuntimeException("Not on Main thread, current thread: ${currentThread.name}")
    }
}
