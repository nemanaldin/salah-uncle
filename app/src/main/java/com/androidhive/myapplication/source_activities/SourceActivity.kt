package com.androidhive.myapplication.source_activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

import com.androidhive.myapplication.R

class SourceActivity : AppCompatActivity() {

    private lateinit var txtSource: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source)

        txtSource = findViewById(R.id.txt_source)

        txtSource.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.erfanabad.org/fa/modules/news/article.php?storyid=41"))
            startActivity(intent)
        }
    }
}