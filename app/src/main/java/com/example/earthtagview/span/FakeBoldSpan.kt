package com.example.earthtagview.span

import android.text.TextPaint
import android.text.style.CharacterStyle

/**
 * @author kun
 * @since 2021-May-28
 */
class FakeBoldSpan : CharacterStyle() {
    override fun updateDrawState(tp: TextPaint?) {
        tp?.apply {
            isFakeBoldText = true
        }
    }
}