package com.androidhive.myapplication.source_activities

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.androidhive.myapplication.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TasbihatActivity : AppCompatActivity() {

    private lateinit var txt_tas: TextView
    private lateinit var txt_tas1: TextView
    private lateinit var txt_tas2: TextView
    private lateinit var txt_tas3: TextView
    private lateinit var txt_tas4: TextView
    private lateinit var txt_tas5: TextView
    private lateinit var txt_tas6: TextView
    private lateinit var txt_tas7: TextView
    private lateinit var txt_tas8: TextView
    private lateinit var txt_tas9: TextView
    private lateinit var txt_tas10: TextView
    private lateinit var txt_tas11: TextView
    private lateinit var txt_tas12: TextView
    private lateinit var txt_tas13: TextView
    private lateinit var txt_tas14: TextView
    private lateinit var txt_tas15: TextView
    private lateinit var txt_tas16: TextView
    private lateinit var txt_tas17: TextView
    private lateinit var txt_tas18: TextView
    private lateinit var txt_tas19: TextView
    private lateinit var txt_tas20: TextView
    private lateinit var txt_tas21: TextView
    private lateinit var txt_tas22: TextView
    private lateinit var txt_tas23: TextView

    private lateinit var imgtas1: ImageView
    private lateinit var imgtas2: ImageView
    private lateinit var imgtas3: ImageView
    private lateinit var imgtas4: ImageView
    private lateinit var imgtas5: ImageView
    private lateinit var imgtas6: ImageView
    private lateinit var imgtas7: ImageView
    private lateinit var imgtas8: ImageView
    private lateinit var imgtas9: ImageView
    private lateinit var imgtas10: ImageView
    private lateinit var imgtas11: ImageView
    private lateinit var imgtas12: ImageView
    private lateinit var imgtas13: ImageView
    private lateinit var imgtas14: ImageView
    private lateinit var imgtas15: ImageView
    private lateinit var imgtas16: ImageView
    private lateinit var imgtas17: ImageView
    private lateinit var imgtas18: ImageView
    private lateinit var imgtas19: ImageView
    private lateinit var imgtas20: ImageView
    private lateinit var imgtas21: ImageView
    private lateinit var imgtas22: ImageView

    private lateinit var mptas1: MediaPlayer
    private lateinit var mptas2: MediaPlayer
    private lateinit var mptas3: MediaPlayer
    private lateinit var mptas4: MediaPlayer
    private lateinit var mptas5: MediaPlayer
    private lateinit var mptas6: MediaPlayer
    private lateinit var mptas7: MediaPlayer
    private lateinit var mptas8: MediaPlayer
    private lateinit var mptas9: MediaPlayer
    private lateinit var mptas10: MediaPlayer
    private lateinit var mptas11: MediaPlayer
    private lateinit var mptas12: MediaPlayer
    private lateinit var mptas13: MediaPlayer
    private lateinit var mptas14: MediaPlayer
    private lateinit var mptas15: MediaPlayer
    private lateinit var mptas16: MediaPlayer
    private lateinit var mptas17: MediaPlayer
    private lateinit var mptas18: MediaPlayer
    private lateinit var mptas19: MediaPlayer
    private lateinit var mptas20: MediaPlayer
    private lateinit var mptas21: MediaPlayer
    private lateinit var mptas22: MediaPlayer

    private lateinit var btntasbihat: Button

    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasbihat)

        txt_tas = findViewById(R.id.txt_tas)
        txt_tas1 = findViewById(R.id.txt_tas1)
        txt_tas2 = findViewById(R.id.txt_tas2)
        txt_tas3 = findViewById(R.id.txt_tas3)
        txt_tas4 = findViewById(R.id.txt_tas4)
        txt_tas5 = findViewById(R.id.txt_meyyet1)
        txt_tas6 = findViewById(R.id.txt_tas6)
        txt_tas7 = findViewById(R.id.txt_tas7)
        txt_tas8 = findViewById(R.id.txt_tas8)
        txt_tas9 = findViewById(R.id.txt_tas9)
        txt_tas10 = findViewById(R.id.txt_tas10)
        txt_tas11 = findViewById(R.id.txt_tas11)
        txt_tas12 = findViewById(R.id.txt_tas12)
        txt_tas13 = findViewById(R.id.txt_tas13)
        txt_tas14 = findViewById(R.id.txt_tas14)
        txt_tas15 = findViewById(R.id.txt_tas15)
        txt_tas16 = findViewById(R.id.txt_tas16)
        txt_tas17 = findViewById(R.id.txt_tas17)
        txt_tas18 = findViewById(R.id.txt_tas18)
        txt_tas19 = findViewById(R.id.txt_tas19)
        txt_tas20 = findViewById(R.id.txt_tas20)
        txt_tas21 = findViewById(R.id.txt_tas21)
        txt_tas22 = findViewById(R.id.txt_tas22)
        txt_tas23 = findViewById(R.id.txt_tas23)

        imgtas1 = findViewById(R.id.imgtas1)
        imgtas2 = findViewById(R.id.imgtas2)
        imgtas3 = findViewById(R.id.imgtas3)
        imgtas4 = findViewById(R.id.imgtas4)
        imgtas5 = findViewById(R.id.imgtas5)
        imgtas6 = findViewById(R.id.imgtas6)
        imgtas7 = findViewById(R.id.imgtas7)
        imgtas8 = findViewById(R.id.imgtas8)
        imgtas9 = findViewById(R.id.imgtas9)
        imgtas10 = findViewById(R.id.imgtas10)
        imgtas11 = findViewById(R.id.imgtas11)
        imgtas12 = findViewById(R.id.imgtas12)
        imgtas13 = findViewById(R.id.imgtas13)
        imgtas14 = findViewById(R.id.imgtas14)
        imgtas15 = findViewById(R.id.imgtas15)
        imgtas16 = findViewById(R.id.imgtas16)
        imgtas17 = findViewById(R.id.imgtas17)
        imgtas18 = findViewById(R.id.imgtas18)
        imgtas19 = findViewById(R.id.imgtas19)
        imgtas20 = findViewById(R.id.imgtas20)
        imgtas21 = findViewById(R.id.imgtas21)
        imgtas22 = findViewById(R.id.imgtas22)

        btntasbihat = findViewById(R.id.btntasbihat)

        fab = findViewById(R.id.fab)

        mptas1 = MediaPlayer.create(this, R.raw.subhanallah)
        mptas2 = MediaPlayer.create(this, R.raw.alhamdolillah)
        mptas3 = MediaPlayer.create(this, R.raw.allaho_akbar)
        mptas4 = MediaPlayer.create(this, R.raw.la_ilaha_vahdahu)
        mptas5 = MediaPlayer.create(this, R.raw.ajirna_minannar)
        mptas6 = MediaPlayer.create(this, R.raw.adkhilnaljannah)
        mptas7 = MediaPlayer.create(this, R.raw.la_ilah)
        mptas8 = MediaPlayer.create(this, R.raw.mohammad)
        mptas9 = MediaPlayer.create(this, R.raw.salawat)
        mptas10 = MediaPlayer.create(this, R.raw.astaghfir)
        mptas11 = MediaPlayer.create(this, R.raw.yahadi)
        mptas12 = MediaPlayer.create(this, R.raw.salawat)
        mptas13 = MediaPlayer.create(this, R.raw.ruku)
        mptas14 = MediaPlayer.create(this, R.raw.sejde)
        mptas15 = MediaPlayer.create(this, R.raw.samiallaho)
        mptas16 = MediaPlayer.create(this, R.raw.rabbanalakalhamd)
        mptas17 = MediaPlayer.create(this, R.raw.subhanakallahumma)
        mptas18 = MediaPlayer.create(this, R.raw.attahiyat)
        mptas19 = MediaPlayer.create(this, R.raw.salawats)
        mptas20 = MediaPlayer.create(this, R.raw.dua)
        mptas21 = MediaPlayer.create(this, R.raw.inna_nastaeinoka)
        mptas22 = MediaPlayer.create(this, R.raw.farz)

        imgtas1.setOnClickListener { mptas1.start() }
        imgtas2.setOnClickListener { mptas2.start() }
        imgtas3.setOnClickListener { mptas3.start() }
        imgtas4.setOnClickListener { mptas4.start() }
        imgtas5.setOnClickListener { mptas5.start() }
        imgtas6.setOnClickListener { mptas6.start() }
        imgtas7.setOnClickListener { mptas7.start() }
        imgtas8.setOnClickListener { mptas8.start() }
        imgtas9.setOnClickListener { mptas9.start() }
        imgtas10.setOnClickListener { mptas10.start() }
        imgtas11.setOnClickListener { mptas11.start() }
        imgtas12.setOnClickListener { mptas12.start() }
        imgtas13.setOnClickListener { mptas13.start() }
        imgtas14.setOnClickListener { mptas14.start() }
        imgtas15.setOnClickListener { mptas15.start() }
        imgtas16.setOnClickListener { mptas16.start() }
        imgtas17.setOnClickListener { mptas17.start() }
        imgtas18.setOnClickListener { mptas18.start() }
        imgtas19.setOnClickListener { mptas19.start() }
        imgtas20.setOnClickListener { mptas20.start() }
        imgtas21.setOnClickListener { mptas21.start() }
        imgtas22.setOnClickListener { mptas22.start() }

        fab.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                btntasbihat.text.toString() +
                txt_tas.text.toString() +
                txt_tas1.text.toString() +
                txt_tas2.text.toString() +
                txt_tas3.text.toString() +
                txt_tas4.text.toString() +
                txt_tas5.text.toString() +
                txt_tas6.text.toString() +
                txt_tas7.text.toString() +
                txt_tas8.text.toString() +
                txt_tas9.text.toString() +
                txt_tas10.text.toString() +
                txt_tas11.text.toString() +
                txt_tas12.text.toString() +
                txt_tas13.text.toString() +
                txt_tas14.text.toString() +
                txt_tas15.text.toString() +
                txt_tas16.text.toString() +
                txt_tas17.text.toString() +
                txt_tas18.text.toString() +
                txt_tas19.text.toString() +
                txt_tas20.text.toString() +
                txt_tas21.text.toString() +
                txt_tas22.text.toString() +
                txt_tas23.text.toString())
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "")
            startActivity(Intent.createChooser(shareIntent, "اشتراک ..."))
        }
    }
}