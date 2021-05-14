package com.example.earthtaglib.bean

import android.graphics.Color
import android.graphics.PointF
import android.view.View

/**
 *
 * 每个Tag的信息，其中spatialX和spatialY对应View的座标系，z轴座标系往屏幕里是正值
 *
 * @param view 每个tag绑定的View
 * @param spatialX 3D空间中x座标
 * @param spatialY 3D空间中y座标
 * @param spatialZ 3D空间中z座标
 * @param scale 缩放比例
 * @param popularity 当前颜色的标记,便于[TagCloud][com.example.earthtaglib.TagCloud]标记颜色,有多少种popularity就有多少种颜色
 *
 * @author kun
 * @since 5/12/21
 */
class Tag(
    var view: View,
    var spatialX: Float = 0f,
    var spatialY: Float = 0f,
    var spatialZ: Float = 0f,
    var scale: Float = 1f,
    var popularity: Int = 5
) : Comparable<Tag> {

    /**
     * 对应手机平面的座标
     */
    var flatPosition = PointF(0f, 0f)
    var flatX = flatPosition.x
        set(value) {
            flatPosition.x = value
            field = value
        }
        get() = flatPosition.x
    var flatY = flatPosition.y
        set(value) {
            flatPosition.y = value
            field = value
        }
        get() = flatPosition.y

    /**
     * opacity,red,green,blue
     */
    private var color = floatArrayOf(1f, 0.5f, 0.5f, 0.5f)

    var opacity = color[0]
        set(value) {
            color[0] = value
            field = value
        }
        get() = color[0]

    fun setColorComponent(rgb: FloatArray) {
        System.arraycopy(rgb, 0, color, 0, rgb.size)
    }

    fun getColor(): Int {
        val result = IntArray(4)
        for (i in 0..3) {
            result[i] = (color[i] * 0xff).toInt()
        }
        return Color.argb(result[0], result[1], result[2], result[3])
    }

    override fun compareTo(other: Tag) = if (scale > other.scale) 1 else -1
}