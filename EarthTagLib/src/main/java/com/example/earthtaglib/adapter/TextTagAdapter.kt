package com.example.earthtaglib.adapter

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * TagAdapter实现类，显示TextView
 * @author kun
 * @since 5/12/21
 */
class TextTagAdapter(private val dataSet: List<String>) : TagAdapter() {

    override fun getCount(): Int {
        return dataSet.size
    }

    override fun getView(parent: ViewGroup, position: Int): View {
        val tv = TextView(parent.context)
        tv.text = dataSet[position]
        tv.gravity = Gravity.CENTER
        return tv
    }

    override fun getItem(position: Int): Any {
        return dataSet[position]
    }

    override fun getPopularity(position: Int): Int {
        return position % 7
    }

    override fun onThemeColorChanged(view: View, themeColor: Int, alpha: Float) {
        view.setBackgroundColor(themeColor)
    }
}