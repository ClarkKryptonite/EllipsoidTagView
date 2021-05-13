package com.example.earthtaglib.bean

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * @author kun
 * @since 5/13/21
 */
class Point3D(val x: Float, val y: Float, val z: Float)
class TagMatrix(val column: Int, val row: Int, val matrix: Array<FloatArray>)

fun tagMatrixMake(column: Int, row: Int): TagMatrix {
    val matrix = Array(column) { FloatArray(row) { 0f } }
    return TagMatrix(column, row, matrix)
}

fun tagMatrixMakeFromArray(column: Int, row: Int, data: Array<FloatArray>): TagMatrix {
    val tagMatrix = tagMatrixMake(column, row)
    for (i in 0 until column) {
        System.arraycopy(data[i], 0, tagMatrix.matrix[i], 0, data[i].size)
    }
    return tagMatrix
}

fun tagMatrixMultiply(m1: TagMatrix, m2: TagMatrix): TagMatrix {
    val result = tagMatrixMake(m1.column, m2.row)
    for (i in 0 until m1.column) {
        for (j in 0 until m2.row) {
            for (k in 0 until m1.row) {
                result.matrix[i][j] += m1.matrix[i][k] * m2.matrix[k][j]
            }
        }
    }
    return result
}

fun Tag.pointRotation(direction: Point3D, angle: Float) {
    if (angle == 0f) return
    val temp = Array(1) { floatArrayOf(this.spatialX, this.spatialY, this.spatialZ, 1f) }
    var result = tagMatrixMakeFromArray(1, 4, temp)

    val distanceYZ = direction.z * direction.z + direction.y * direction.y
    if (distanceYZ != 0f) {
        val cos1 = direction.z / sqrt(distanceYZ)
        val sin1 = direction.y / sqrt(distanceYZ)
        val t1 = Array(4) { FloatArray(4) }.apply {
            set(0, floatArrayOf(1f, 0f, 0f, 0f))
            set(1, floatArrayOf(0f, cos1, sin1, 0f))
            set(2, floatArrayOf(0f, -sin1, cos1, 0f))
            set(3, floatArrayOf(0f, 0f, 0f, 1f))
        }
        val m1 = tagMatrixMakeFromArray(4, 4, t1)
        result = tagMatrixMultiply(result, m1)
    }

    val distanceXYZ =
        direction.x * direction.x + direction.y * direction.y + direction.y * direction.y
    if (distanceXYZ != 0f) {
        val cos2 = sqrt(distanceYZ) / sqrt(distanceXYZ)
        val sin2 = -direction.x / sqrt(distanceXYZ)
        val t2 = Array(4) { FloatArray(4) }.apply {
            set(0, floatArrayOf(cos2, 0f, -sin2, 0f))
            set(1, floatArrayOf(0f, 1f, 0f, 0f))
            set(2, floatArrayOf(sin2, 0f, cos2, 0f))
            set(3, floatArrayOf(0f, 0f, 0f, 1f))
        }
        val m2 = tagMatrixMakeFromArray(4, 4, t2)
        result = tagMatrixMultiply(result, m2)
    }

    val cos3 = cos(angle)
    val sin3 = sin(angle)
    val t3 = Array(4) { FloatArray(4) }.apply {
        set(0, floatArrayOf(cos3, sin3, 0f, 0f))
        set(1, floatArrayOf(-sin3, cos3, 0f, 0f))
        set(2, floatArrayOf(0f, 0f, 1f, 0f))
        set(3, floatArrayOf(0f, 0f, 0f, 1f))
    }
    val m3 = tagMatrixMakeFromArray(4, 4, t3)
    result = tagMatrixMultiply(result, m3)

    if (distanceXYZ != 0f) {
        val cos2 = sqrt(distanceYZ) / sqrt(distanceXYZ)
        val sin2 = -direction.x / sqrt(distanceXYZ)
        val t2 = Array(4) { FloatArray(4) }.apply {
            set(0, floatArrayOf(cos2, 0f, sin2, 0f))
            set(1, floatArrayOf(0f, 1f, 0f, 0f))
            set(2, floatArrayOf(-sin2, 0f, cos2, 0f))
            set(3, floatArrayOf(0f, 0f, 0f, 1f))
        }
        val m2 = tagMatrixMakeFromArray(4, 4, t2)
        result = tagMatrixMultiply(result, m2)
    }

    if (distanceYZ != 0f) {
        val cos1 = direction.z / sqrt(distanceYZ)
        val sin1 = direction.y / sqrt(distanceYZ)
        val t1 = Array(4) { FloatArray(4) }.apply {
            set(0, floatArrayOf(1f, 0f, 0f, 0f))
            set(1, floatArrayOf(0f, cos1, -sin1, 0f))
            set(2, floatArrayOf(0f, sin1, cos1, 0f))
            set(3, floatArrayOf(0f, 0f, 0f, 1f))
        }
        val m1 = tagMatrixMakeFromArray(4, 4, t1)
        result = tagMatrixMultiply(result, m1)
    }

    this.spatialX = result.matrix[0][0]
    this.spatialY = result.matrix[0][1]
    this.spatialZ = result.matrix[0][2]

}