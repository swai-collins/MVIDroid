package com.arkivanov.mvikotlin.core.reaktive.store

import com.arkivanov.mvikotlin.base.observable.MviDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.disposable

fun MviDisposable.asReaktive(): Disposable = disposable(::dispose)
