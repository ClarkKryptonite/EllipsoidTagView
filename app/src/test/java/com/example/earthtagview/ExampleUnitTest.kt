package com.example.earthtagview

import org.junit.Test

import org.junit.Assert.*
import kotlin.math.sin

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        println("sin(Math.PI / 2) = ${sin(Math.PI / 2)}")
        println("sin(Math.PI / 360) = ${sin(Math.PI / 360)}")
    }
}