package com.example.earthtaglib

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.annotation.IntDef
import androidx.core.view.isGone
import com.example.earthtaglib.adapter.TagAdapter
import com.example.earthtaglib.bean.Tag
import kotlin.math.abs
import kotlin.math.min

/**
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


    private var mSpeed = 2f
    private var mTagCloud: TagCloud = TagCloud()
    private var mInertiaX = 0.5f
    private var mInertiaY = 0.5f
    private var mCenterX = 0f
    private var mCenterY = 0f
    private var mRadius = 0f
    private var mRadiusPercent = 0.9f
    private var mDarkColor = floatArrayOf(1f, 0f, 0f, 1f) //rgba
    private var mLightColor = floatArrayOf(0.9412f, 0.7686f, 0.2f, 1f) //rgba

    private var manualScroll = false

    @Mode
    var autoScrollMode = MODE_UNIFORM
    private val mLayoutParams: MarginLayoutParams by lazy {
        layoutParams as MarginLayoutParams
    }
    private var mMinSize = 0
    private var mIsOnTouch = false
    private val mHandler = Handler(Looper.getMainLooper())
    private lateinit var mAdapter: TagAdapter
    var mOnTagClickListener: OnTagClickListener? = null

    init {
        isFocusableInTouchMode = true
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.EarthTagView)
            val m = typedArray.getString(R.styleable.EarthTagView_autoScrollMode) ?: "0"
            autoScrollMode = Integer.valueOf(m)
            setManualScroll(typedArray.getBoolean(R.styleable.EarthTagView_manualScroll, false))
            mInertiaX = typedArray.getFloat(R.styleable.EarthTagView_startAngleX, 0.5f)
            mInertiaY = typedArray.getFloat(R.styleable.EarthTagView_startAngleY, 0.5f)
            val light = typedArray.getColor(R.styleable.EarthTagView_lightColor, Color.WHITE)
            setLightColor(light)
            val dark = typedArray.getColor(R.styleable.EarthTagView_darkColor, Color.BLACK)
            setDarkColor(dark)
            val p = typedArray.getFloat(R.styleable.EarthTagView_radiusPercent, mRadiusPercent)
            setRadiusPercent(p)
            val s = typedArray.getFloat(R.styleable.EarthTagView_scrollSpeed, 2f)
            setScrollSpeed(s)
            typedArray.recycle()
        }
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getSize(point)
        Log.d("EarthTagView", "screenWidth - point : ${point.x}")
        val screenWidth = point.x
        val screenHeight = point.y
        mMinSize = if (screenHeight < screenWidth) screenHeight else screenWidth
    }

    fun setAdapter(adapter: TagAdapter) {
        mAdapter = adapter
        mAdapter.onDataSetChangeListener = this
        onChange()
    }

    fun setManualScroll(manualScroll: Boolean) {
        this.manualScroll = manualScroll
    }

    fun setLightColor(color: Int) {
        val argb = FloatArray(4)
        argb[3] = Color.alpha(color) / 1.0f / 0xff
        argb[0] = Color.red(color) / 1.0f / 0xff
        argb[1] = Color.green(color) / 1.0f / 0xff
        argb[2] = Color.blue(color) / 1.0f / 0xff
        mLightColor = argb.clone()
        onChange()
    }

    fun setDarkColor(color: Int) {
        val argb = FloatArray(4)
        argb[3] = Color.alpha(color) / 1.0f / 0xff
        argb[0] = Color.red(color) / 1.0f / 0xff
        argb[1] = Color.green(color) / 1.0f / 0xff
        argb[2] = Color.blue(color) / 1.0f / 0xff
        mDarkColor = argb.clone()
        onChange()
    }

    fun setRadiusPercent(percent: Float) {
        require(!(percent > 1f || percent < 0f)) { "percent value not in range 0 to 1" }
        mRadiusPercent = percent
        onChange()
    }

    private fun initFromAdapter() {
        postDelayed({
            mCenterX = ((right - left) / 2).toFloat()
            mCenterY = ((bottom - top) / 2).toFloat()
            mRadius = min(mCenterX * mRadiusPercent, mCenterY * mRadiusPercent)
            mTagCloud.radius = mRadius.toInt()
            mTagCloud.setTagColorLight(mLightColor) //higher color
            mTagCloud.setTagColorDark(mDarkColor) //lower color
            mTagCloud.clear()
            removeAllViews()
            for (i in 0 until mAdapter.getCount()) {
                //binding view to each tag
                val view: View = mAdapter.getView(this, i)
                val tag = Tag(view, popularity = mAdapter.getPopularity(i))
                mTagCloud.add(tag)
                addListener(view, i)
            }
            mTagCloud.setInertia(mInertiaX, mInertiaY)
            mTagCloud.create()
            resetChildren()
        }, 0)
    }

    private fun addListener(view: View, position: Int) {
        if (!view.hasOnClickListeners()) {
            view.setOnClickListener { v ->
                mOnTagClickListener?.onItemClick(
                    this@EarthTagView,
                    v,
                    position
                )
            }
        }
    }

    fun setScrollSpeed(scrollSpeed: Float) {
        mSpeed = scrollSpeed
    }

    private fun resetChildren() {
        removeAllViews()
        //必须保证getChildAt(i) == mTagCloud.getTagList().get(i)
        for (tag in mTagCloud.tagList) {
            addView(tag.view)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val contentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val contentHeight = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val dimensionX =
            if (widthMode == MeasureSpec.EXACTLY) contentWidth
            else mMinSize - mLayoutParams.leftMargin - mLayoutParams.rightMargin
        val dimensionY =
            if (heightMode == MeasureSpec.EXACTLY) contentHeight
            else mMinSize - mLayoutParams.leftMargin - mLayoutParams.rightMargin
        setMeasuredDimension(dimensionX, dimensionY)
        measureChildren(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mHandler.post(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val tag: Tag = mTagCloud[i]
            child?.let {
                if (child.isGone.not()) {
                    mAdapter.onThemeColorChanged(child, tag.getColor(), tag.alpha)
                    child.scaleX = tag.scale
                    child.scaleY = tag.scale
                    val left: Int = (mCenterX + tag.flatX).toInt() - child.measuredWidth / 2
                    val top: Int = (mCenterY + tag.flatY).toInt() - child.measuredHeight / 2
                    child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
                }
            }
        }
    }

    fun reset() {
        mTagCloud.reset()
        resetChildren()
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
        return true
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
        mHandler.postDelayed(this, 16)
    }

    interface OnTagClickListener {
        fun onItemClick(parent: ViewGroup?, view: View?, position: Int)
    }

    companion object {
        private const val TOUCH_SCALE_FACTOR = 0.8f
        const val MODE_DISABLE = 0
        const val MODE_DECELERATE = 1
        const val MODE_UNIFORM = 2
    }
}