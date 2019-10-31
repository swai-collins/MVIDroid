package com.arkivanov.mvikotlin.core.debug.logging

internal expect fun Any.toDeepString(mode: DeepStringMode, format: Boolean): String

internal enum class DeepStringMode {
    SHORT, MEDIUM, FULL
}
