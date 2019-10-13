package com.arkivanov.mvikotlin.core.debug.store.timetravel

data class MviTimeTravelEvents(
    val items: List<MviTimeTravelEvent> = emptyList(),
    val index: Int = -1
)
