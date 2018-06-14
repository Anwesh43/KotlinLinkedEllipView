package com.anwesh.uiprojects.kotlinlinkedellipview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.linkedellipview.LinkedEllipView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LinkedEllipView.create(this)
    }
}
