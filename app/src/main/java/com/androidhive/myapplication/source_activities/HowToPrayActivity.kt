package com.androidhive.myapplication.source_activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.androidhive.myapplication.PrayerActivity
import com.androidhive.myapplication.R

class HowToPrayActivity : AppCompatActivity() {

    private lateinit var txtSubject1: TextView
    private lateinit var txtExplanation1: TextView
    private lateinit var txtExplanation2: TextView
    private lateinit var txtExplanation3: TextView
    private lateinit var txtExplanation4: TextView
    private lateinit var txtExplanation5: TextView
    private lateinit var txtExplanation6: TextView
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_howtopray)

        txtSubject1 = findViewById(R.id.txtSubject1)
        txtExplanation1 = findViewById(R.id.txtExplanation1)
        txtExplanation2 = findViewById(R.id.txtExplanation2)
        txtExplanation3 = findViewById(R.id.txtExplanation3)
        txtExplanation4 = findViewById(R.id.txtExplanation4)
        txtExplanation5 = findViewById(R.id.txtExplanation5)
        txtExplanation6 = findViewById(R.id.txtExplanation6)

        txtSubject1.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,
                    txtExplanation1.text.toString() +
                    txtExplanation2.text.toString() +
                    txtExplanation3.text.toString() +
                    txtExplanation4.text.toString() +
                    txtExplanation5.text.toString() +
                    txtExplanation6.text.toString()
                )
                putExtra(Intent.EXTRA_SUBJECT, "iran")
            }
            startActivity(Intent.createChooser(shareIntent, "اشتراک ..."))
        }

        fab = findViewById(R.id.fab)
        fab.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,
                    txtSubject1.text.toString() +
                    txtExplanation1.text.toString() +
                    txtExplanation2.text.toString() +
                    txtExplanation3.text.toString() +
                    txtExplanation4.text.toString() +
                    txtExplanation5.text.toString() +
                    txtExplanation6.text.toString()
                )
                putExtra(Intent.EXTRA_SUBJECT, "")
            }
            startActivity(Intent.createChooser(shareIntent, "اشتراک ..."))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, PrayerActivity::class.java))
    }
}