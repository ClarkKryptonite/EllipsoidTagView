package com.example.earthtagview.adapter

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.example.earthtaglib.adapter.TagAdapter

/**
 * @author kun
 * @since 5/14/21
 */
class WhiteTextTagAdapter(val textList:MutableList<String>) : TagAdapter() {
    override fun getCount() = textList.size

    override fun getView(parent: ViewGroup, position: Int): View {
        val tv = AppCompatTextView(parent.context)
        tv.paint.isFakeBoldText = true
        tv.includeFontPadding = false
        tv.setLineSpacing(0f,0f)
        tv.firstBaselineToTopHeight = 0
        tv.text = textList[position]
        tv.gravity = Gravity.CENTER
        tv.textSize = 12f
        return tv
    }

    override fun getPopularity(position: Int): Int {
        return 0
    }

    override fun onThemeColorChanged(view: View, themeColor: Int, alpha: Float) {
        (view as? TextView)?.setTextColor(themeColor)
    }
}