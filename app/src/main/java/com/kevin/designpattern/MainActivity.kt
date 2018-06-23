package com.kevin.designpattern

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {
        var button = findViewById<Button>(R.id.btn)
        button.setOnClickListener(){
            var intent = Intent(this, TwoActivity::class.java)
            startActivity(intent)
        }
    }
}
