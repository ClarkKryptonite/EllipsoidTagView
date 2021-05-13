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
    var xRadius: Int = radius,
    var yRadius: Int = radius,
    var zRadius: Int = radius,
    var lightColor: FloatArray = DEFAULT_COLOR_LIGHT,
    var darkColor: FloatArray = DEFAULT_COLOR_DARK
) {

    companion object {
        private const val DEFAULT_RADIUS = 300

        /**
         * 旋转单位，默认为一度
         */
        private const val ROTATE_DEGREE_UNIT = Math.PI / 180
        private val DEFAULT_COLOR_LIGHT = floatArrayOf(0.886f, 0.725f, 0.188f, 1f)
        private val DEFAULT_COLOR_DARK = floatArrayOf(0.3f, 0.3f, 0.3f, 1f)
    }

    private var mSinX = 0f
    private var mCosX = 0f

    private var mSinY = 0f
    private var mCosY = 0f

    private var mSinZ = 0f
    private var mCosZ = 0f

    /**
     * x-y座标系下旋转角度，弧度制
     */
    private var rotateDegree = Math.PI / 4

    /**
     * 每次绕屏幕x轴旋转的角度
     */
    private var mInertiaX = 0f

    /**
     * 每次绕屏幕y轴旋转的角度
     */
    private var mInertiaY = 0f
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

    fun reset() {
        create()
    }

    fun update() {
        if (abs(mInertiaX) > 0.1f || abs(mInertiaY) > 0.1f) {
            recalculateAngle()
            updateAll()
        }
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
        newTag.spatialX = (xRadius * cos(phi) * sin(theta)).toFloat()
        newTag.spatialY = (yRadius * sin(phi) * sin(theta)).toFloat()
        newTag.spatialZ = (zRadius * cos(theta)).toFloat()
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

            tagList[i - 1].theta = theta
            tagList[i - 1].phi = phi
            tagList[i - 1].spatialX = (xRadius * cos(phi) * sin(theta)).toFloat()
            tagList[i - 1].spatialY = (yRadius * sin(phi) * sin(theta)).toFloat()
            tagList[i - 1].spatialZ = (zRadius * cos(theta)).toFloat()
        }
    }

    private var maxDelta = 0f
    private var minDelta = 0f

    private fun updateAll() {

        //update transparency/scale for all tags:
        for (tag in tagList) {
            val x: Float = tag.spatialX
            val y: Float = tag.spatialY
            val z: Float = tag.spatialZ

            //There exists two options for this part:
            // multiply positions by a x-rotation matrix
            val ry1 = y * mCosX - z * mSinX
            val rz1 = y * mSinX + z * mCosX
            // multiply new positions by a y-rotation matrix
            val rx2 = x * mCosY - rz1 * mSinY
            val rz2 = x * mSinY + rz1 * mCosY
            // multiply new positions by a z-rotation matrix
            val rx3 = rx2 * mCosZ - ry1 * mSinZ
            val ry3 = rx2 * mSinZ + ry1 * mCosZ
            // set arrays to new positions
            tag.spatialX = rx3
            tag.spatialY = ry3
            tag.spatialZ = rz2

            // 计算透明度
            val diameter = 2 * radius
            val per = diameter / 1.0f / (diameter + rz2)
            // let's set position, scale, alpha for the tag;
            tag.flatX = rx3 * per
            tag.flatY = ry3 * per
            tag.scale = per
            // calculate alpha value
            val delta = diameter + rz2
            maxDelta = max(maxDelta, delta)
            minDelta = min(minDelta, delta)
            val alpha = (delta - minDelta) / (maxDelta - minDelta)
            tag.alpha = 1 - alpha
        }
        // sortTagByScale()
    }

    private fun getColorFromGradient(percentage: Float): FloatArray {
        val rgba = FloatArray(4)
        rgba[0] = 1f
        rgba[1] = percentage * darkColor[0] + (1f - percentage) * lightColor[0]
        rgba[2] = percentage * darkColor[1] + (1f - percentage) * lightColor[1]
        rgba[3] = percentage * darkColor[2] + (1f - percentage) * lightColor[2]
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

    fun setTagColorLight(tagColor: FloatArray) {
        lightColor = tagColor
    }

    fun setTagColorDark(tagColorDark: FloatArray) {
        darkColor = tagColorDark
    }

    fun setInertia(x: Float, y: Float) {
        mInertiaX = 1f
        mInertiaY = 1f
        mInertiaZ = 0f
    }

    private fun sortTagByScale() {
        tagList.sort()
    }

    private fun calculatePopularity() {
        tagList.forEach {
            mMaxPopularity = max(mMaxPopularity, it.popularity)
            mMinPopularity = min(mMinPopularity, it.popularity)
        }
        tagList.forEach { initTagColor(it) }
    }


}