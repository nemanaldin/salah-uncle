package com.androidhive.myapplication.source_activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.androidhive.myapplication.R

class TayammomActivity : AppCompatActivity() {

    private lateinit var fab: FloatingActionButton
    private lateinit var txtTayammom: TextView
    private lateinit var txtTayammom1: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tayammom)

        txtTayammom = findViewById(R.id.txt_meyyet)
        txtTayammom1 = findViewById(R.id.txt_tayammom1)
        fab = findViewById(R.id.fab)

        fab.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, txtTayammom.text.toString() + txtTayammom1.text.toString())
                putExtra(Intent.EXTRA_SUBJECT, "")
            }
            startActivity(Intent.createChooser(shareIntent, "اشتراک ..."))
        }
    }
}