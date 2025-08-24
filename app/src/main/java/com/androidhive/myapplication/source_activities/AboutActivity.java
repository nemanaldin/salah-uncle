package com.androidhive.myapplication.source_activities;

import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.androidhive.myapplication.R;

public class AboutActivity extends AppCompatActivity {

  ImageView email ;
  ImageView telegram ;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);

    telegram = (ImageView)findViewById(R.id.telegram);
    email = (ImageView)findViewById(R.id.email);



    telegram.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent= new Intent(Intent.ACTION_EDIT);
        Intent i = new Intent(Intent.ACTION_VIEW , Uri.parse("https://t.me/Mono11"));
        startActivity(i);
      }
    });

    email.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        Intent intent = new Intent(Intent.ACTION_SEND);

        String[] strTo = { getString(R.string.to) };

        intent.putExtra(Intent.EXTRA_EMAIL, strTo);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject));

        Uri attachments = Uri.parse("moeinarazi@gmail.com");
        intent.putExtra(Intent.EXTRA_STREAM, attachments);

        intent.setType("message/rfc822");

        intent.setPackage("com.google.android.gm");

        startActivity(intent);
      }
    });


  }
}
