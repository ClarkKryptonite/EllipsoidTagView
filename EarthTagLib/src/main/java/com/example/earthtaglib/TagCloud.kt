package com.example.earthtaglib

import com.example.earthtaglib.bean.Tag
import kotlin.math.*

/**
 * 用于记录所有tag，并计算出每个tag所在位置
 * @author kun
 * @since 5/12/21
 */
class TagCloud(
    val tagList: MutableList<Tag> = arrayListOf(),
    var radius: Int = DEFAULT_RADIUS,
    var scale: Float = DEFAULT_DELTA_SCALE,
    var minAlpha: Float = DEFAULT_MIN_ALPHA,
    var maxAlpha: Float = DEFAULT_MAX_ALPHA,
    var lightColor: FloatArray = DEFAULT_COLOR_LIGHT,
    var darkColor: FloatArray = DEFAULT_COLOR_DARK
) {

    private var mSinX = 0f
    private var mCosX = 0f

    private var mSinY = 0f
    private var mCosY = 0f

    private var mSinZ = 0f
    private var mCosZ = 0f

    /**
     * 每次绕屏幕x轴旋转的角度
     */
    private var mInertiaX = 0f

    /**
     * 每次绕屏幕y轴旋转的角度
     */
    private var mInertiaY = 0f

    /**
     * 每次绕屏幕z轴旋转的角度
     */
    private var mInertiaZ = 0f

    private var mMinPopularity = 0
    private var mMaxPopularity = 0

    fun create() {
        positionAll()
        calculatePopularity()
        recalculateAngle()
        updateAll()
    }

    fun add(newTag: Tag) {
        initTagColor(newTag)
        position(newTag)
        tagList.add(newTag)
        updateAll()
    }

    fun clear() {
        tagList.clear()
    }

    operator fun get(position: Int): Tag = tagList[position]

    fun update() {
        recalculateAngle()
        updateAll()
    }

    /**
     * 初始化Tag颜色
     *
     * @param tag 需要设置的tag
     */
    private fun initTagColor(tag: Tag) {
        val percentage = getPercentage(tag)
        val argb = getColorFromGradient(percentage)
        tag.setColorComponent(argb)
    }

    private fun getPercentage(tag: Tag): Float {
        val p: Int = tag.popularity
        return if (mMinPopularity == mMaxPopularity) 1.0f
        else (p.toFloat() - mMinPopularity) / (mMaxPopularity - mMinPopularity)
    }

    /**
     * 计算当前Tag位置
     *
     * @param newTag 需要计算位置的tag
     */
    private fun position(newTag: Tag) {
        val theta = Math.random() * Math.PI
        val phi = Math.random() * (2 * Math.PI)
        newTag.spatialX = (radius * cos(phi) * sin(theta)).toFloat()
        newTag.spatialY = (radius * sin(phi) * sin(theta)).toFloat()
        newTag.spatialZ = (radius * cos(theta)).toFloat()
    }

    /**
     * 计算所有Tag位置,使得所有的Tag均匀分布
     */
    private fun positionAll() {
        // 该tag 与z轴的夹角
        var theta: Double
        // 该tag和z轴平面 与x轴的夹角
        var phi: Double
        val max = tagList.size
        for (i in 1..max) {
            theta = acos((2.0 * i - 1.0) / max - 1)
            phi = sqrt(max * Math.PI) * theta

            tagList[i - 1].spatialX = (radius * cos(phi) * sin(theta)).toFloat()
            tagList[i - 1].spatialY = (radius * sin(phi) * sin(theta)).toFloat()
            tagList[i - 1].spatialZ = (radius * cos(theta)).toFloat()
        }
    }

    private var maxDelta = 0f
    private var minDelta = 0f

    /**
     * EarthTagView的核心算法
     *
     * 计算旋转后座标，缩放倍数，透明度
     */
    private fun updateAll() {
        for (tag in tagList) {
            val x: Float = tag.spatialX
            val y: Float = tag.spatialY
            val z: Float = tag.spatialZ

            // 绕X-轴旋转
            val ry1 = y * mCosX - z * mSinX
            val rz1 = y * mSinX + z * mCosX
            // 绕Y-轴旋转
            val rx2 = x * mCosY - rz1 * mSinY
            val rz2 = x * mSinY + rz1 * mCosY
            // 绕Z-轴旋转
            val rx3 = rx2 * mCosZ - ry1 * mSinZ
            val ry3 = rx2 * mSinZ + ry1 * mCosZ
            // 设置新的3D座标
            tag.spatialX = rx3
            tag.spatialY = ry3
            tag.spatialZ = rz2
            // 计算缩放比
            val diameter = 2 * radius
            val per = diameter * scale / (diameter + rz2) / 2
            // 计算透明度
            val delta = diameter + rz2
            maxDelta = max(maxDelta, delta)
            minDelta = min(minDelta, delta)
            val alpha =
                minAlpha + (delta - minDelta) / (maxDelta - minDelta) * (maxAlpha - minAlpha)

            // 更新tag属性
            tag.flatX = rx3
            tag.flatY = ry3
            tag.scale = per
            tag.opacity = 1 - alpha
        }
    }

    private fun getColorFromGradient(percentage: Float): FloatArray {
        val rgba = FloatArray(4)
        rgba[0] = 1f
        rgba[1] = percentage * lightColor[0] + (1f - percentage) * darkColor[0]
        rgba[2] = percentage * lightColor[1] + (1f - percentage) * darkColor[1]
        rgba[3] = percentage * lightColor[2] + (1f - percentage) * darkColor[2]
        return rgba
    }

    /**
     * 计算旋转角度,该角度是手机平面角度，x-y座标系和View的座标系一致，z座标正向为朝屏幕内方向
     */
    private fun recalculateAngle() {
        mSinX = sin(mInertiaX * ROTATE_DEGREE_UNIT).toFloat()
        mCosX = cos(mInertiaX * ROTATE_DEGREE_UNIT).toFloat()
        mSinY = sin(mInertiaY * ROTATE_DEGREE_UNIT).toFloat()
        mCosY = cos(mInertiaY * ROTATE_DEGREE_UNIT).toFloat()
        mSinZ = sin(mInertiaZ * ROTATE_DEGREE_UNIT).toFloat()
        mCosZ = cos(mInertiaZ * ROTATE_DEGREE_UNIT).toFloat()
    }

    fun setInertia(x: Float, y: Float) {
        mInertiaX = x
        mInertiaY = y
    }

    private fun calculatePopularity() {
        tagList.forEach {
            mMaxPopularity = max(mMaxPopularity, it.popularity)
            mMinPopularity = min(mMinPopularity, it.popularity)
        }
        tagList.forEach { initTagColor(it) }
    }


}