package com.example.earthtagview.adapter

import android.graphics.*
import android.text.TextPaint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.earthtaglib.adapter.TagAdapter
import kotlin.math.roundToInt

/**
 * @author kun
 * @since 2021-Jun-04
 */
class TextBgTagAdapter(private val textList: MutableList<String>) : TagAdapter() {
    override fun getCount() = textList.size

    override fun getView(parent: ViewGroup, position: Int): View {
        val view = ImageView(parent.context)

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 35f
            color = Color.WHITE
            style = Paint.Style.FILL_AND_STROKE
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val boundsRect = Rect()
        textPaint.getTextBounds(textList[position], 0, textList[position].length, boundsRect)

        val fontMetrics = textPaint.fontMetrics
        val textHeight = -fontMetrics.ascent + fontMetrics.descent

        val bitmap = Bitmap.createBitmap(
            boundsRect.width(),
            textHeight.roundToInt(),
            Bitmap.Config.ARGB_8888
        ).apply {
            eraseColor(Color.TRANSPARENT)
        }
        val canvas = Canvas(bitmap)
        canvas.drawText(
            textList[position],
            (boundsRect.width() / 2).toFloat(),
            boundsRect.height().toFloat(),
            textPaint
        )
        view.setImageBitmap(bitmap)
        return view
    }

    override fun getPopularity(position: Int): Int {
        return 0
    }

    override fun onThemeColorChanged(view: View, themeColor: Int, alpha: Float) {
        view.alpha = alpha
    }
}

