package com.example.earthtagview.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.earthtaglib.adapter.TagAdapter

/**
 * @author kun
 * @since 2021-Jun-02
 */
class ImageAdapter(val resIdList: MutableList<Int>) : TagAdapter() {
    override fun getCount(): Int {
        return resIdList.size
    }

    override fun getView(parent: ViewGroup, position: Int): View {
        val view = ImageView(parent.context)
        view.setImageResource(resIdList[position])
        return view
    }

    override fun getPopularity(position: Int): Int {
        return 0
    }

    override fun onThemeColorChanged(view: View, themeColor: Int, alpha: Float) {

    }
}