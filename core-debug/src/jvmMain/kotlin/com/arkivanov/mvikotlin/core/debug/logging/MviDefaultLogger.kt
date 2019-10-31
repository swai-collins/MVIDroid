package com.arkivanov.mvikotlin.core.debug.logging

internal actual val mviDefaultLogger: (String) -> Unit = ::println
