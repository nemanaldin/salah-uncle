package com.androidhive.myapplication.source_activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.androidhive.myapplication.R

class SejdeyyesahvActivity : AppCompatActivity() {

    private lateinit var fab: FloatingActionButton
    private lateinit var txtSejdesahv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sejdeyesahv)

        txtSejdesahv = findViewById(R.id.txt_meyyet)
        fab = findViewById(R.id.fab)

        fab.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, txtSejdesahv.text.toString())
                putExtra(Intent.EXTRA_SUBJECT, "")
            }
            startActivity(Intent.createChooser(shareIntent, "اشتراک ..."))
        }
    }
}