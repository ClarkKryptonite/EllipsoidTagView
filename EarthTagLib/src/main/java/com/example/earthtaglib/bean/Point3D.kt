package com.example.earthtaglib.bean

/**
 * 每个Tag的3D座标
 * @author kun
 * @since 5/12/21
 */
data class Point3D(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {
    constructor(point: Point3D) : this(point.x, point.y, point.z)

    fun negate() {
        x = -x
        y = -y
        z = -z
    }

    fun offset(dx: Float, dy: Float, dz: Float) {
        x += dx
        y += dy
        z += dz
    }
}
