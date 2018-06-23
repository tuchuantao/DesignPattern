package com.kevin.designpattern

import android.app.Activity
import android.os.Bundle

class TwoActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }
}