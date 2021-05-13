package com.example.earthtagview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.earthtaglib.adapter.TextTagAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var num = 15
        val textList = arrayListOf<String>()
        for (i in 1..num) {
            textList.add("No.$i")
        }
        val adapter = TextTagAdapter(textList)
        earth_tag.apply {
            setAdapter(adapter)
        }
        earth_add_tag.setOnClickListener {
            textList.add("No.${++num}")
            adapter.notifyDataSetChanged()
        }
    }
}