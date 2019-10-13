package com.arkivanov.mvikotlin.base.utils

@Volatile
private var mainThreadId: Long? = null

fun setMainThreadId(id: Long) {
    mainThreadId = id
}

private val monitor = Any()

actual fun assertOnMainThread() {
    val currentThread = Thread.currentThread()

    if (mainThreadId == null) {
        synchronized(monitor) {
            if (mainThreadId == null) {
                mainThreadId = currentThread.id
                System.err.println("MviKotlin: mainThreadId is not specified, current thread is considered main: ${currentThread.name}, use setMainThreadId() to set one")
            }
        }
    }

    if (currentThread.id != mainThreadId!!) {
        throw RuntimeException("Not on Main thread, current thread: ${currentThread.name}")
    }
}
