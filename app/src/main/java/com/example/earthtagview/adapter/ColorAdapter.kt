package com.example.earthtagview.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.earthtaglib.adapter.TagAdapter
import com.example.earthtagview.util.dp

/**
 * @author kun
 * @since 2021-Jun-03
 */
class ColorAdapter(val list: List<Any>) : TagAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getView(parent: ViewGroup, position: Int): View {
        val view = View(parent.context)
        val param = ViewGroup.LayoutParams(50.dp, 20.dp)
        view.layoutParams = param
        view.setBackgroundColor(
            ContextCompat.getColor(
                parent.context, when (position % 3) {
                    0 -> android.R.color.holo_red_dark
                    1 -> android.R.color.holo_orange_dark
                    2 -> android.R.color.holo_green_dark
                    else -> android.R.color.holo_blue_dark
                }
            )
        )
        return view
    }

    override fun getPopularity(position: Int): Int {
        return 0
    }

    override fun onThemeColorChanged(view: View, themeColor: Int, alpha: Float) {

    }
}