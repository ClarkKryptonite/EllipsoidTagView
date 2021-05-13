package com.example.earthtagview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.earthtaglib.adapter.TextTagAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textList = arrayListOf<String>()
        for (i in 1..20) {
            textList.add("No.$i")
        }
        earth_tag.apply {
            setAdapter(TextTagAdapter(textList))
        }
    }
}