package com.androidhive.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import com.androidhive.myapplication.source_activities.HowToPrayActivity
import com.androidhive.myapplication.source_activities.MeyetActivity

class PrayerActivity : AppCompatActivity() {

    private lateinit var btn_dailypray: CardView
    private lateinit var btn_jomee: CardView
    private lateinit var btn_jamaat: CardView
    private lateinit var btn_howtopray: CardView
    private lateinit var btn_ghaza: CardView
    private lateinit var btn_meyet: CardView
    private lateinit var btn_ghadr: CardView
    private lateinit var btn_eshragh: CardView
    private lateinit var btn_ayat: CardView
    private lateinit var btn_mosafer: CardView
    private lateinit var btn_eyd: CardView
    private lateinit var btn_awabin: CardView
    private lateinit var btn_taravih: CardView
    private lateinit var btn_tahiyatmasjid: CardView
    private lateinit var btn_tahajod: CardView
    private lateinit var btn_tasabih: CardView

    companion object {
        @JvmStatic
        var fa: Activity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge for API 30+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)

        btn_dailypray = findViewById(R.id.btn_dailypray)
        btn_jomee = findViewById(R.id.btn_jomee)
        btn_jamaat = findViewById(R.id.btn_jamaat)
        btn_howtopray = findViewById(R.id.btn_howtopray)
        btn_ghaza = findViewById(R.id.btn_ghaza)
        btn_meyet = findViewById(R.id.btn_meyet)
        btn_ghadr = findViewById(R.id.btn_ghadr)
        btn_eshragh = findViewById(R.id.btn_eshragh)
        btn_ayat = findViewById(R.id.btn_ayat)
        btn_mosafer = findViewById(R.id.btn_mosafer)
        btn_eyd = findViewById(R.id.btn_eyd)
        btn_awabin = findViewById(R.id.btn_awabin)
        btn_taravih = findViewById(R.id.btn_taravih)
        btn_tahiyatmasjid = findViewById(R.id.btn_tahiyatmasjid)
        btn_tahajod = findViewById(R.id.btn_tahajod)
        btn_tasabih = findViewById(R.id.btn_tasabih)

        btn_dailypray.setOnClickListener(listener)
        btn_jomee.setOnClickListener(listener)
        btn_jamaat.setOnClickListener(listener)
        btn_howtopray.setOnClickListener(listener)
        btn_ghaza.setOnClickListener(listener)
        btn_meyet.setOnClickListener(listener)
        btn_ghadr.setOnClickListener(listener)
        btn_eshragh.setOnClickListener(listener)
        btn_ayat.setOnClickListener(listener)
        btn_mosafer.setOnClickListener(listener)
        btn_eyd.setOnClickListener(listener)
        btn_awabin.setOnClickListener(listener)
        btn_taravih.setOnClickListener(listener)
        btn_tahiyatmasjid.setOnClickListener(listener)
        btn_tahajod.setOnClickListener(listener)
        btn_tasabih.setOnClickListener(listener)
    }

    private val listener = View.OnClickListener { v ->
        when (v.id) {
            R.id.btn_dailypray -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 0")
                startActivity(intent)
            }
            R.id.btn_howtopray -> {
                val intent = Intent(this@PrayerActivity, HowToPrayActivity::class.java)
                startActivity(intent)
                fa?.finish()
            }
            R.id.btn_jomee -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 1")
                startActivity(intent)
            }
            R.id.btn_jamaat -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 2")
                startActivity(intent)
            }
            R.id.btn_ghaza -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 3")
                startActivity(intent)
            }
            R.id.btn_meyet -> {
                val intent = Intent(this@PrayerActivity, MeyetActivity::class.java)
                startActivity(intent)
                fa?.finish()
            }
            R.id.btn_eyd -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 4")
                startActivity(intent)
            }
            R.id.btn_mosafer -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 5")
                startActivity(intent)
            }
            R.id.btn_ayat -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 6")
                startActivity(intent)
            }
            R.id.btn_tahajod -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 7")
                startActivity(intent)
            }
            R.id.btn_ghadr -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 8")
                startActivity(intent)
            }
            R.id.btn_eshragh -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 9")
                startActivity(intent)
            }
            R.id.btn_awabin -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 10")
                startActivity(intent)
            }
            R.id.btn_tasabih -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 11")
                startActivity(intent)
            }
            R.id.btn_taravih -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 12")
                startActivity(intent)
            }
            R.id.btn_tahiyatmasjid -> {
                val intent = Intent(this@PrayerActivity, TextActivity::class.java)
                intent.putExtra("info", "index 13")
                startActivity(intent)
            }
        }
    }
}