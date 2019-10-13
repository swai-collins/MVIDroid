package com.arkivanov.mvikotlin.base.utils

import platform.posix.fprintf
import platform.posix.pthread_self
import platform.posix.stderr
import kotlin.native.concurrent.AtomicLong
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
private val mainThreadId = AtomicLong(Long.MIN_VALUE)

fun setMainThreadId(id: Long) {
    mainThreadId.value = id
}

actual fun assertOnMainThread() {
    val currentThreadId = pthread_self().toLong()

    if (mainThreadId.compareAndSet(Long.MIN_VALUE, currentThreadId)) {
        fprintf(
            stderr,
            "MviKotlin: mainThreadId is not specified, current thread is considered main: $currentThreadId, use setMainThreadId() to set one"
        )
    }

    if (currentThreadId != mainThreadId.value) {
        throw RuntimeException("Not on Main thread, current thread id: $currentThreadId")
    }
}
