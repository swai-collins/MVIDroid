package com.arkivanov.mvikotlin.core.debug.logging

import android.util.Log

internal actual val mviDefaultLogger: (String) -> Unit = { Log.v("MviKotlin", it) }
