package com.example.earthtaglib

import com.example.earthtaglib.bean.Point3D
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        val point3D = Point3D(0f, 0f, 0f)
    }
}