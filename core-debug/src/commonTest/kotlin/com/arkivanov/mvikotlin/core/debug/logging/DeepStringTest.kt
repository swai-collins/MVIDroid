package com.arkivanov.mvikotlin.core.debug.logging

import kotlin.test.Test
import kotlin.test.assertTrue

class DeepStringTest {

    @Test
    fun string_has_all_names_and_values_without_format() {
        val value = TestData()

        val result = value.toDeepString(mode = DeepStringMode.FULL, format = false)

        assertResult(result)
    }

    @Test
    fun string_has_all_names_and_values_with_format() {
        val value = TestData()

        val result = value.toDeepString(mode = DeepStringMode.FULL, format = true)

        assertResult(result)
    }

    private fun assertResult(result: String) {
        assertTrue(result.contains("123"))
        assertTrue(result.contains("234"))
        assertTrue(result.contains("456.7"))
        assertTrue(result.contains("567.8"))
        assertTrue(result.contains("678"))
        assertTrue(result.contains("321"))
        assertTrue(result.contains("432"))
        assertTrue(result.contains("TestSubData"))
        assertTrue(result.contains("789"))
        assertTrue(result.contains("890"))
    }

    data class TestData(
        val intValue: Int = 123,
        val longValue: Long = 234L,
        val floatValue: Float = 456.7F,
        val doubleValue: Double = 567.8,
        val stringValue: String = "678",
        val listValue: List<Any> = listOf(321, "432", TestSubData())
    )

    data class TestSubData(
        val intValue: Int = 789,
        val stringValue: String = "890"
    )
}
