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

class VuzuActivity : AppCompatActivity() {

    private lateinit var vuzu1: TextView
    private lateinit var vuzu2: TextView
    private lateinit var vuzu3: TextView
    private lateinit var vuzu4: TextView
    private lateinit var vuzu5: TextView
    private lateinit var vuzu6: TextView
    private lateinit var vuzu7: TextView
    private lateinit var vuzu8: TextView
    private lateinit var vuzu9: TextView
    private lateinit var vuzu10: TextView
    private lateinit var vuzu11: TextView
    private lateinit var vuzu_text1: TextView
    private lateinit var vuzu_text2: TextView
    private lateinit var vuzu_text3: TextView

    private lateinit var mp1: MediaPlayer
    private lateinit var mp2: MediaPlayer
    private lateinit var mp3: MediaPlayer
    private lateinit var mp4: MediaPlayer
    private lateinit var mp5: MediaPlayer
    private lateinit var mp6: MediaPlayer
    private lateinit var mp7: MediaPlayer
    private lateinit var mp8: MediaPlayer
    private lateinit var mp9: MediaPlayer
    private lateinit var mp10: MediaPlayer
    private lateinit var mp11: MediaPlayer

    private lateinit var img_btn2: ImageView
    private lateinit var img_btn3: ImageView
    private lateinit var img_btn4: ImageView
    private lateinit var img_btn5: ImageView
    private lateinit var img_btn6: ImageView
    private lateinit var img_btn7: ImageView
    private lateinit var img_btn8: ImageView
    private lateinit var img_btn9: ImageView
    private lateinit var img_btn10: ImageView
    private lateinit var img_btn11: ImageView
    private lateinit var img_btn12: ImageView

    private lateinit var btnvuzu: Button
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vuzu)

        btnvuzu = findViewById(R.id.btn_vuzu)

        img_btn2 = findViewById(R.id.img_btn2)
        img_btn3 = findViewById(R.id.img_btn3)
        img_btn4 = findViewById(R.id.img_btn4)
        img_btn5 = findViewById(R.id.img_btn5)
        img_btn6 = findViewById(R.id.img_btn6)
        img_btn7 = findViewById(R.id.img_btn7)
        img_btn8 = findViewById(R.id.img_btn8)
        img_btn9 = findViewById(R.id.img_btn9)
        img_btn10 = findViewById(R.id.img_btn10)
        img_btn11 = findViewById(R.id.img_btn11)
        img_btn12 = findViewById(R.id.img_btn12)

        vuzu1 = findViewById(R.id.txt_vuzu1)
        vuzu2 = findViewById(R.id.txt_vuzu2)
        vuzu3 = findViewById(R.id.txt_vuzu3)
        vuzu4 = findViewById(R.id.txt_vuzu4)
        vuzu5 = findViewById(R.id.txt_vuzu5)
        vuzu6 = findViewById(R.id.txt_vuzu6)
        vuzu7 = findViewById(R.id.txt_vuzu7)
        vuzu8 = findViewById(R.id.txt_vuzu8)
        vuzu9 = findViewById(R.id.txt_vuzu9)
        vuzu10 = findViewById(R.id.txt_vuzu10)
        vuzu11 = findViewById(R.id.txt_vuzu11)
        vuzu_text1 = findViewById(R.id.vuzu_text1)
        vuzu_text2 = findViewById(R.id.vuzu_text2)
        vuzu_text3 = findViewById(R.id.vuzu_text3)

        mp1 = MediaPlayer.create(this, R.raw.auuzu_billah)
        mp2 = MediaPlayer.create(this, R.raw.vuzu_bismillahi)
        mp3 = MediaPlayer.create(this, R.raw.vuzu3)
        mp4 = MediaPlayer.create(this, R.raw.vuzu4)
        mp5 = MediaPlayer.create(this, R.raw.vuzu5)
        mp6 = MediaPlayer.create(this, R.raw.vuzu6)
        mp7 = MediaPlayer.create(this, R.raw.vuzu7)
        mp8 = MediaPlayer.create(this, R.raw.vuzu8)
        mp9 = MediaPlayer.create(this, R.raw.vuzu9)
        mp10 = MediaPlayer.create(this, R.raw.vuzu10)
        mp11 = MediaPlayer.create(this, R.raw.vuzu11)

        fab = findViewById(R.id.fab)

        img_btn2.setOnClickListener { mp1.start() }
        img_btn3.setOnClickListener { mp2.start() }
        img_btn4.setOnClickListener { mp3.start() }
        img_btn5.setOnClickListener { mp4.start() }
        img_btn6.setOnClickListener { mp5.start() }
        img_btn7.setOnClickListener { mp6.start() }
        img_btn8.setOnClickListener { mp7.start() }
        img_btn9.setOnClickListener { mp8.start() }
        img_btn10.setOnClickListener { mp9.start() }
        img_btn11.setOnClickListener { mp10.start() }
        img_btn12.setOnClickListener { mp11.start() }

        fab.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                btnvuzu.text.toString() +
                vuzu_text1.text.toString() +
                vuzu1.text.toString() +
                vuzu_text2.text.toString() +
                vuzu2.text.toString() +
                vuzu3.text.toString() +
                vuzu4.text.toString() +
                vuzu5.text.toString() +
                vuzu6.text.toString() +
                vuzu7.text.toString() +
                vuzu8.text.toString() +
                vuzu9.text.toString() +
                vuzu10.text.toString() +
                vuzu11.text.toString() +
                vuzu_text3.text.toString())
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "")
            startActivity(Intent.createChooser(shareIntent, "اشتراک ..."))
        }
    }
}