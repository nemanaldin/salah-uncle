package com.androidhive.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.androidhive.myapplication.R

class TextActivity : AppCompatActivity() {

    private lateinit var fab: FloatingActionButton
    private lateinit var txtExplanation: TextView
    private lateinit var txtSubject: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)

        fab = findViewById(R.id.fab)
        txtExplanation = findViewById(R.id.txtExplanation)
        txtSubject = findViewById(R.id.txtSubject)

        fab.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, txtExplanation.text.toString())
                putExtra(Intent.EXTRA_SUBJECT, "nothing")
            }
            startActivity(Intent.createChooser(shareIntent, "اشتراک ..."))
        }

        val info = intent.getStringExtra("info")
        if (info != null) {
            Log.i("Test", "index is $info")
            when (info) {
                "index 0" -> {
                    txtSubject.setText(R.string.dailypray_title)
                    txtExplanation.setText(R.string.dailypray)
                }
                "index 1" -> {
                    txtSubject.setText(R.string.jomee_title)
                    txtExplanation.setText(R.string.jomee)
                }
                "index 2" -> {
                    txtSubject.setText(R.string.jamaat_title)
                    txtExplanation.setText(R.string.jamaat)
                }
                "index 3" -> {
                    txtSubject.setText(R.string.ghaza_title)
                    txtExplanation.setText(R.string.ghaza)
                }
                "index 4" -> {
                    txtSubject.setText(R.string.eyd_title)
                    txtExplanation.setText(R.string.eyd)
                }
                "index 5" -> {
                    txtSubject.setText(R.string.mosafer_title)
                    txtExplanation.setText(R.string.mosafer)
                }
                "index 6" -> {
                    txtSubject.setText(R.string.ayat_title)
                    txtExplanation.setText(R.string.ayat)
                }
                "index 7" -> {
                    txtSubject.setText(R.string.tahajod_title)
                    txtExplanation.setText(R.string.tahajod)
                }
                "index 8" -> {
                    txtSubject.setText(R.string.ghadr_title)
                    txtExplanation.setText(R.string.ghadr)
                }
                "index 9" -> {
                    txtSubject.setText(R.string.eshragh_title)
                    txtExplanation.setText(R.string.eshragh)
                }
                "index 10" -> {
                    txtSubject.setText(R.string.awabin_title)
                    txtExplanation.setText(R.string.awabin)
                }
                "index 11" -> {
                    txtSubject.setText(R.string.tasabih_title)
                    txtExplanation.setText(R.string.tasabih)
                }
                "index 12" -> {
                    txtSubject.setText(R.string.taravih_title)
                    txtExplanation.setText(R.string.taravih)
                }
                "index 13" -> {
                    txtSubject.setText(R.string.tahiyatmasjid_title)
                    txtExplanation.setText(R.string.tahiyatmasjid)
                }
            }
        }
    }
}