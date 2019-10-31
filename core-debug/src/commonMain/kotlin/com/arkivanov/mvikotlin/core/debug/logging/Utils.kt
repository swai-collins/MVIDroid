package com.arkivanov.mvikotlin.core.debug.logging

import com.arkivanov.mvikotlin.base.store.MviEventType

internal fun getLogText(storeName: String, eventType: MviEventType, value: Any, deepStringMode: DeepStringMode): String =
    "$storeName ($eventType, ${value::class.simpleName}): ${value.toDeepString(deepStringMode, false)}"

internal inline operator fun ((String) -> Unit).invoke(loggingMode: MviLoggingMode, text: () -> String) {
    if (loggingMode != MviLoggingMode.DISABLED) {
        invoke(text())
    }
}

internal operator fun ((String) -> Unit).invoke(loggingMode: MviLoggingMode, storeName: String, eventType: MviEventType, value: Any) {
    loggingMode
        .toDeepStringMode()
        ?.let { getLogText(storeName, eventType, value, it) }
        ?.also(this)
}

private fun MviLoggingMode.toDeepStringMode(): DeepStringMode? =
    when (this) {
        MviLoggingMode.DISABLED -> null
        MviLoggingMode.SHORT -> DeepStringMode.SHORT
        MviLoggingMode.MEDIUM -> DeepStringMode.MEDIUM
        MviLoggingMode.FULL -> DeepStringMode.FULL
    }
