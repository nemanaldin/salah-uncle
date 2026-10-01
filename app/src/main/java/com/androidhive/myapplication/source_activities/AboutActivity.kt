package com.androidhive.myapplication.source_activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView

import com.androidhive.myapplication.R

class AboutActivity : AppCompatActivity() {

    private lateinit var email: ImageView
    private lateinit var telegram: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        telegram = findViewById(R.id.telegram)
        email = findViewById(R.id.email)

        telegram.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Mono11"))
            startActivity(intent)
        }

        email.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.to)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject))
                putExtra(Intent.EXTRA_STREAM, Uri.parse("moeinarazi@gmail.com"))
                type = "message/rfc822"
                setPackage("com.google.android.gm")
            }
            startActivity(intent)
        }
    }
}