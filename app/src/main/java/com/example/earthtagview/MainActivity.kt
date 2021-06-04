package com.example.earthtagview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.text.buildSpannedString
import com.example.earthtaglib.EarthTagView
import com.example.earthtagview.adapter.ColorAdapter
import com.example.earthtagview.adapter.ImageAdapter
import com.example.earthtagview.adapter.TextBgTagAdapter
import com.example.earthtagview.adapter.WhiteTextTagAdapter
import com.example.earthtagview.span.FakeBoldSpan
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var setPause = false
    private var enableScale = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val num = 30

        // text adapter
        val textList = arrayListOf<String>()
        for (i in 1..num) {
            textList.add("关键词$i")
        }
        val textAdapter = WhiteTextTagAdapter(textList)

        // image adapter
        val resIdList = arrayListOf<Int>().apply {
            for (i in 1..10) {
                add(R.drawable.text_img_android)
                add(R.drawable.text_img_java_base)
                add(R.drawable.text_img_tools)
            }
        }
        val imageAdapter = ImageAdapter(resIdList)

        // view adapter
        val colorList = arrayListOf<Any>().apply {
            for (i in 1..num) {
                add(Any())
            }
        }
        val colorAdapter = ColorAdapter(colorList)

        // text drawable adapter
        val textDrawableAdapter = TextBgTagAdapter(textList)

        earth_tag.apply {
            setAdapter(textDrawableAdapter)
        }

        earth_tag.mOnTagClickListener = object : EarthTagView.OnTagClickListener {
            override fun onItemClick(parent: ViewGroup?, view: View?, position: Int) {
                Toast.makeText(this@MainActivity, textList[position], Toast.LENGTH_SHORT).show()
            }
        }

        val title = arrayListOf<String>().apply {
            add("1.5亿")
            add("全球专利数据 覆盖")
            add("116个")
            add("国家/地区")
            add("创新热词 - 专利查询，技术分析，免费看")
        }
        tag_title.text = buildSpannedString {
            append(SpannableString(title[0]).apply {
                setSpan(
                    RelativeSizeSpan(1.6f),
                    0,
                    title[0].length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(Color.parseColor("#ff9900")),
                    0,
                    title[0].length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
                setSpan(FakeBoldSpan(), 0, title[0].length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                setSpan(StyleSpan(Typeface.ITALIC), 0, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            })
            append(SpannableString(title[1]).apply {
                setSpan(
                    RelativeSizeSpan(1.2f),
                    0,
                    title[1].length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(Color.parseColor("#ffffff")),
                    0,
                    title[1].length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
            })
            append(SpannableString(title[2]).apply {
                setSpan(
                    RelativeSizeSpan(1.6f),
                    0,
                    title[2].length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(Color.parseColor("#ff9900")),
                    0,
                    title[2].length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
            })
            append(SpannableString(title[3]).apply {
                setSpan(
                    RelativeSizeSpan(1.2f),
                    0,
                    title[3].length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(Color.parseColor("#ffffff")),
                    0,
                    title[3].length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
            })
            append("\n")
            append(SpannableString(title[4]).apply {
                setSpan(
                    ForegroundColorSpan(Color.parseColor("#ff9900")),
                    0,
                    title[4].length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
            })
        }

        toggle_button.setOnClickListener {
            setPause = !setPause
            earth_tag.setPause = setPause
        }

        id_text.setOnClickListener {
            val scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 1f, 2f, 1f)
            val scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", 1f, 2f, 1f)
            val translationXHolder = PropertyValuesHolder.ofFloat("translationX", 0f, 100f, 0f)
            val translationYHolder = PropertyValuesHolder.ofFloat("translationY", 0f, 100f, 0f)
            val animator = ObjectAnimator.ofPropertyValuesHolder(
                scaleXHolder,
                scaleYHolder,
                translationXHolder,
                translationYHolder
            )
            animator.duration = 1000L
            animator.start()
        }
    }

}