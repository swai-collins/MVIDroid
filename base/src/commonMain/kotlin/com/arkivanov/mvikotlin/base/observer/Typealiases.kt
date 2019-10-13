package com.arkivanov.mvikotlin.base.observer

import com.badoo.reaktive.utils.atomic.AtomicReference

typealias MviObservers<T> = AtomicReference<List<MviObserver<T>>>