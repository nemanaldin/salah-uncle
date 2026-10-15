package com.androidhive.myapplication.source_activities

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.androidhive.myapplication.PrayerActivity
import com.androidhive.myapplication.R

class MeyetActivity : AppCompatActivity() {

    private lateinit var txt_meyyet: TextView
    private lateinit var txt_meyyet1: TextView
    private lateinit var txt_meyyet2: TextView
    private lateinit var txt_meyyet3: TextView
    private lateinit var txt_meyyet4: TextView
    private lateinit var txt_meyyet5: TextView
    private lateinit var txt_meyyet6: TextView
    private lateinit var txt_meyyet7: TextView
    private lateinit var txt_meyyet8: TextView
    private lateinit var txt_meyyet9: TextView
    private lateinit var txt_meyyet10: TextView
    private lateinit var imgmeyyet: ImageView
    private lateinit var imgmeyyet1: ImageView
    private lateinit var imgmeyyet2: ImageView
    private lateinit var imgmeyyet3: ImageView
    private lateinit var imgmeyyet4: ImageView
    private lateinit var mpmeyyet: MediaPlayer
    private lateinit var mpmeyyet1: MediaPlayer
    private lateinit var mpmeyyet2: MediaPlayer
    private lateinit var mpmeyyet3: MediaPlayer
    private lateinit var mpmeyyet4: MediaPlayer
    private lateinit var fab: FloatingActionButton
    private lateinit var btnmeyyet: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meyet)

        txt_meyyet = findViewById(R.id.txt_meyyet)
        txt_meyyet1 = findViewById(R.id.txt_meyyet1)
        txt_meyyet2 = findViewById(R.id.txt_meyyet2)
        txt_meyyet3 = findViewById(R.id.txt_meyyet3)
        txt_meyyet4 = findViewById(R.id.txt_meyyet4)
        txt_meyyet5 = findViewById(R.id.txt_meyyet5)
        txt_meyyet6 = findViewById(R.id.txt_meyyet6)
        txt_meyyet7 = findViewById(R.id.txt_meyyet7)
        txt_meyyet8 = findViewById(R.id.txt_meyyet8)
        txt_meyyet9 = findViewById(R.id.txt_meyyet9)
        txt_meyyet10 = findViewById(R.id.txt_meyyet10)

        imgmeyyet = findViewById(R.id.imgmeyyet)
        imgmeyyet1 = findViewById(R.id.imgmeyyet1)
        imgmeyyet2 = findViewById(R.id.imgmeyyet2)
        imgmeyyet3 = findViewById(R.id.imgmeyyet3)
        imgmeyyet4 = findViewById(R.id.imgmeyyet4)

        mpmeyyet = MediaPlayer.create(this, R.raw.subhanakallahumma)
        mpmeyyet1 = MediaPlayer.create(this, R.raw.salawats)
        mpmeyyet2 = MediaPlayer.create(this, R.raw.meyyet_mature)
        mpmeyyet3 = MediaPlayer.create(this, R.raw.meyyet_boy)
        mpmeyyet4 = MediaPlayer.create(this, R.raw.meyyet_girl)

        btnmeyyet = findViewById(R.id.btnmeyyet)

        imgmeyyet.setOnClickListener {
            mpmeyyet.start()
        }

        imgmeyyet1.setOnClickListener {
            mpmeyyet1.start()
        }

        imgmeyyet2.setOnClickListener {
            mpmeyyet2.start()
        }

        imgmeyyet3.setOnClickListener {
            mpmeyyet3.start()
        }

        imgmeyyet4.setOnClickListener {
            mpmeyyet4.start()
        }

        fab = findViewById(R.id.fab)

        fab.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,
                    btnmeyyet.text.toString() +
                    txt_meyyet.text.toString() +
                    txt_meyyet1.text.toString() +
                    txt_meyyet2.text.toString() +
                    txt_meyyet3.text.toString() +
                    txt_meyyet4.text.toString() +
                    txt_meyyet5.text.toString() +
                    txt_meyyet6.text.toString() +
                    txt_meyyet7.text.toString() +
                    txt_meyyet8.text.toString() +
                    txt_meyyet9.text.toString() +
                    txt_meyyet10.text.toString()
                )
                putExtra(Intent.EXTRA_SUBJECT, "")
            }
            startActivity(Intent.createChooser(shareIntent, "اشتراک ..."))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listOf(mpmeyyet, mpmeyyet1, mpmeyyet2, mpmeyyet3, mpmeyyet4).forEach { mp ->
            try {
                if (mp.isPlaying) mp.stop()
                mp.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, PrayerActivity::class.java))
    }
}