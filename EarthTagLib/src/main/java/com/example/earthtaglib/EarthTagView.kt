package com.example.earthtaglib

import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.view.*
import androidx.annotation.IntDef
import androidx.core.view.*
import com.example.earthtaglib.adapter.TagAdapter
import com.example.earthtaglib.bean.Tag
import kotlin.math.*

/**
 *
 * @author kun
 * @since 5/12/21
 */
class EarthTagView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ViewGroup(context, attrs, defStyleAttr), Runnable, TagAdapter.OnDataSetChangeListener {

    /**
     * Mode
     *
     */
    @IntDef(MODE_DISABLE, MODE_DECELERATE, MODE_UNIFORM)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Mode


    //region 私有变量
    private var mSpeed = 2f
    private var mTagCloud: TagCloud = TagCloud()
    private var mInertiaX = 0.5f
    private var mInertiaY = 0.5f
    private var mCenterX = 0f
    private var mCenterY = 0f
    private var mRadius = 100f
    private var mDarkColor = DEFAULT_COLOR_DARK //rgba
    private var mLightColor = DEFAULT_COLOR_LIGHT //rgba

    private var manualScroll = false

    private val mLayoutParams: MarginLayoutParams by lazy {
        layoutParams as MarginLayoutParams
    }
    private var viewInitialWidth = 0
    private var viewInitialHeight = 0
    private var mIsOnTouch = false
    private lateinit var mAdapter: TagAdapter
    private val dataHandleThread = HandlerThread("View")
    private val mHandler by lazy {
        dataHandleThread.start()
        Handler(dataHandleThread.looper)
    }

    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (setPause) return
            event?.values?.let {
                var axisX = if (abs(it[0]) < 0.03f) 0f else it[0]
                var axisY = if (abs(it[1]) < 0.03f) 0f else it[1]

                val velocity = sqrt(axisX * axisX + axisY * axisY)
                if (velocity < minVelocity) {
                    when {
                        axisX == 0f && axisY == 0f -> {
                            axisX = sqrt(2f) * minVelocity / 2
                            axisY = axisX
                        }
                        axisX == 0f -> axisY = minVelocity
                        axisY == 0f -> axisX = minVelocity
                        else -> {
                            val k = axisY / axisX
                            val absAxisX = sqrt(minVelocity * minVelocity / (k * k + 1))
                            axisX = if (axisX > 0) absAxisX else -absAxisX
                            axisY = k * axisX
                        }
                    }
                } else if (velocity > maxVelocity) {
                    when {
                        axisX == 0f -> axisY = maxVelocity
                        axisY == 0f -> axisX = maxVelocity
                        else -> {
                            val k = axisY / axisX
                            val absAxisX = sqrt(maxVelocity * maxVelocity / (k * k + 1))
                            axisX = if (axisX > 0) absAxisX else -absAxisX
                            axisY = k * axisX
                        }
                    }
                }

                // Sensor的Y座标和View的Y座标相反
                // mInertialX代表的是绕X轴旋转的角度，所以要用axisY赋值，mInertiaY同理
                mInertiaX = axisY
                mInertiaY = -axisX

            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }
    //endregion

    //region 属性变量
    @Mode
    var autoScrollMode = MODE_UNIFORM
    var mOnTagClickListener: OnTagClickListener? = null
    var radiusPercent = 0.9f
        set(value) {
            require(!(value > 1f || value < 0f)) { "percent value not in range 0 to 1" }
            mRadius = min(mCenterX * value, mCenterY * 2 * value)
            mTagCloud.radius = mRadius.roundToInt()
            field = value
        }
    var deltaScale = 1f
        set(value) {
            require(1f <= value) { "scale must equal or greater than 1.0" }
            mTagCloud.scale = value
            field = value
        }
    var minAlpha = 0.1f
        set(value) {
            require(value in 0f..1f) { "alpha must between 0f and 1f " }
            mTagCloud.minAlpha = value
            field = value
        }
    var maxAlpha = 0.1f
        set(value) {
            require(value in 0f..1f) { "alpha must between 0f and 1f " }
            mTagCloud.maxAlpha = value
            field = value
        }

    @Volatile
    var setPause: Boolean = false
        set(value) {
            field = value
            if (!value) {
                mHandler.post(this)
            }
        }

    @Volatile
    var minVelocity = DEFAULT_MIN_VELOCITY
        set(value) {
            require(value > 0f) { "min velocity must greater than 0" }
            field = value
        }

    @Volatile
    var maxVelocity = DEFAULT_MAX_VELOCITY
        set(value) {
            require(value > minVelocity) { "max velocity must greater than minVelocity:$minVelocity" }
            field = value
        }
    //endregion

    init {
        isFocusableInTouchMode = true
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.EarthTagView)
            autoScrollMode =
                typedArray.getInteger(R.styleable.EarthTagView_autoScrollMode, MODE_UNIFORM)
            val light = typedArray.getColor(R.styleable.EarthTagView_lightColor, Color.WHITE)
            setLightColor(light)
            val dark = typedArray.getColor(R.styleable.EarthTagView_darkColor, Color.BLACK)
            setDarkColor(dark)
            radiusPercent =
                typedArray.getFloat(R.styleable.EarthTagView_radiusPercent, DEFAULT_PERCENT)
            deltaScale = typedArray.getFloat(R.styleable.EarthTagView_scale, DEFAULT_DELTA_SCALE)
            minAlpha = typedArray.getFloat(R.styleable.EarthTagView_minAlpha, DEFAULT_MIN_ALPHA)
            maxAlpha = typedArray.getFloat(R.styleable.EarthTagView_maxAlpha, DEFAULT_MAX_ALPHA)
            minVelocity =
                typedArray.getFloat(R.styleable.EarthTagView_minVelocity, DEFAULT_MIN_VELOCITY)
            maxVelocity =
                typedArray.getFloat(R.styleable.EarthTagView_maxVelocity, DEFAULT_MAX_VELOCITY)

            typedArray.recycle()
        }
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getSize(point)
        val screenWidth = point.x
        val screenHeight = point.y
        if (screenWidth < screenHeight) {
            viewInitialWidth = screenWidth
            viewInitialHeight = screenWidth
        } else {
            viewInitialWidth = screenHeight
            viewInitialHeight = screenHeight
        }
        initFromAdapter()
    }

    fun setAdapter(adapter: TagAdapter) {
        mAdapter = adapter
        mAdapter.onDataSetChangeListener = this
    }

    fun setLightColor(color: Int) {
        val argb = FloatArray(4)
        argb[3] = Color.alpha(color) / 1.0f / 0xff
        argb[0] = Color.red(color) / 1.0f / 0xff
        argb[1] = Color.green(color) / 1.0f / 0xff
        argb[2] = Color.blue(color) / 1.0f / 0xff
        mLightColor = argb.clone()
        mTagCloud.lightColor = mLightColor //higher color
    }

    fun setDarkColor(color: Int) {
        val argb = FloatArray(4)
        argb[3] = Color.alpha(color) / 1.0f / 0xff
        argb[0] = Color.red(color) / 1.0f / 0xff
        argb[1] = Color.green(color) / 1.0f / 0xff
        argb[2] = Color.blue(color) / 1.0f / 0xff
        mDarkColor = argb.clone()
        mTagCloud.darkColor = mDarkColor //lower color
    }

    private fun initFromAdapter() {
        if (this::mAdapter.isInitialized) {
            throw NoSuchElementException("No adapter attached")
        }
        post {
            mCenterX = ((right - left) / 2).toFloat()
            mCenterY = bottom.toFloat()
            mRadius = min(mCenterX * radiusPercent, mCenterY * 2 * radiusPercent)
            mTagCloud.radius = mRadius.roundToInt()
            mTagCloud.clear()
            removeAllViews()
            for (i in 0 until mAdapter.getCount()) {
                //binding view to each tag
                val view: View = mAdapter.getView(this, i)
                val tag = Tag(popularity = mAdapter.getPopularity(i), position = i)
                mTagCloud.add(tag)
                addListener(view, i)
                addView(view)
            }
            mTagCloud.setInertia(mInertiaX, mInertiaY)
            mTagCloud.create()
            resetChildren()
        }
    }

    private fun addListener(view: View, position: Int) {
        if (!view.hasOnClickListeners()) {
            view.setOnClickListener { v ->
                val pos = v.tag as? Int
                if (pos != null && mTagCloud[pos].scale >= deltaScale) {
                    mOnTagClickListener?.onItemClick(
                        this@EarthTagView,
                        v,
                        position
                    )
                }
            }
        }
    }

    private fun resetChildren() {
        post { setChildrenPosition() }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val contentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val contentHeight = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val dimensionX =
            if (widthMode == MeasureSpec.EXACTLY) contentWidth
            else viewInitialWidth - mLayoutParams.leftMargin - mLayoutParams.rightMargin
        val dimensionY =
            if (heightMode == MeasureSpec.EXACTLY) contentHeight
            else viewInitialHeight - mLayoutParams.topMargin - mLayoutParams.bottomMargin
        setMeasuredDimension(dimensionX, dimensionY)
        measureChildren(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        setChildrenPosition()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        sensorManager.registerListener(
            sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        mHandler.post(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        sensorManager.unregisterListener(sensorEventListener)
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun setChildrenPosition() {
        val iterator = mTagCloud.tagList.iterator()
        while (iterator.hasNext()) {
            val tag = iterator.next()
            val child = getChildAt(tag.position)
            mAdapter.onThemeColorChanged(child, tag.getColor(), tag.opacity)
            child.scaleX = tag.scale
            child.scaleY = tag.scale
            child.z = tag.scale
            val left = (mCenterX + tag.flatX).roundToInt() - child.measuredWidth / 2
            val top = (mCenterY + tag.flatY).roundToInt() - child.measuredHeight / 2
            child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (manualScroll) {
            handleTouchEvent(ev)
        }
        return false
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (manualScroll) {
            handleTouchEvent(e)
        }
        return super.onTouchEvent(e)
    }

    private var downX = 0f
    private var downY = 0f
    private fun handleTouchEvent(e: MotionEvent) {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = e.x
                downY = e.y
                mIsOnTouch = true
                //rotate elements depending on how far the selection point is from center of cloud
                val dx = e.x - downX
                val dy = e.y - downY
                if (isValidMove(dx, dy)) {
                    mInertiaX = dy / mRadius * mSpeed * TOUCH_SCALE_FACTOR
                    mInertiaY = -dx / mRadius * mSpeed * TOUCH_SCALE_FACTOR
                    processTouch()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = e.x - downX
                val dy = e.y - downY
                if (isValidMove(dx, dy)) {
                    mInertiaX = dy / mRadius * mSpeed * TOUCH_SCALE_FACTOR
                    mInertiaY = -dx / mRadius * mSpeed * TOUCH_SCALE_FACTOR
                    processTouch()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> mIsOnTouch = false
        }
    }

    private fun isValidMove(dx: Float, dy: Float): Boolean {
        val minDistance = ViewConfiguration.get(context).scaledTouchSlop
        return abs(dx) > minDistance || abs(dy) > minDistance
    }

    private fun processTouch() {
        mTagCloud.setInertia(mInertiaX, mInertiaY)
        mTagCloud.update()
        resetChildren()
    }

    override fun onChange() {
        initFromAdapter()
    }

    override fun run() {
        if (!mIsOnTouch && autoScrollMode != MODE_DISABLE) {
            if (autoScrollMode == MODE_DECELERATE) {
                if (mInertiaX > 0.04f) {
                    mInertiaX -= 0.02f
                }
                if (mInertiaY > 0.04f) {
                    mInertiaY -= 0.02f
                }
                if (mInertiaX < -0.04f) {
                    mInertiaX += 0.02f
                }
                if (mInertiaY < -0.04f) {
                    mInertiaY += 0.02f
                }
            }
            processTouch()
        }
        if (!setPause) {
            mHandler.postDelayed(this, REFRESH_INTERVAL)
        }
    }

    interface OnTagClickListener {
        fun onItemClick(parent: ViewGroup?, view: View?, position: Int)
    }

    companion object {
        private const val TOUCH_SCALE_FACTOR = 0.8f
        private const val MODE_DISABLE = 0
        private const val MODE_DECELERATE = 1
        private const val MODE_UNIFORM = 2
        private const val REFRESH_INTERVAL = 10L
    }
}