package com.example.earthtagview.util

import android.content.res.Resources

/**
 * @author kun
 * @since 2021-Jun-03
 */
val Float.dp: Float
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_DIP, this + 0.5f, Resources.getSystem().displayMetrics
    )

val Int.dp: Int
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat() + 0.5f,
        Resources.getSystem().displayMetrics
    ).toInt()


val Float.sp: Float
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_SP, this + 0.5f, Resources.getSystem().displayMetrics
    )


val Int.sp: Int
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_SP,
        this.toFloat() + 0.5f,
        Resources.getSystem().displayMetrics
    ).toInt()