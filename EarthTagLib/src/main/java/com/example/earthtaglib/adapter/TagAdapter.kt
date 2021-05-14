package com.example.earthtaglib.adapter

import android.view.View
import android.view.ViewGroup

/**
 * EarthTagView的适配器，具体自定义的tag实现需要继承该类实现
 * @author kun
 * @since 5/12/21
 */
abstract class TagAdapter {
    open var onDataSetChangeListener: OnDataSetChangeListener? = null
    abstract fun getCount(): Int
    abstract fun getView(parent: ViewGroup, position: Int): View
    abstract fun getPopularity(position: Int): Int
    abstract fun onThemeColorChanged(view: View, themeColor: Int, alpha: Float)

    fun notifyDataSetChanged() {
        onDataSetChangeListener?.onChange()
    }

    interface OnDataSetChangeListener {
        fun onChange()
    }
}