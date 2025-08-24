package com.androidhive.myapplication.source_activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidhive.myapplication.PrayerActivity;
import com.androidhive.myapplication.R;

public class MeyetActivity extends AppCompatActivity {

  TextView txt_meyyet ,txt_meyyet1 ,txt_meyyet2 ,txt_meyyet3 ,txt_meyyet4 ,txt_meyyet5 ,txt_meyyet6 ,txt_meyyet7 ,txt_meyyet8 ,txt_meyyet9 ,txt_meyyet10 ;
  ImageView imgmeyyet ,imgmeyyet1 ,imgmeyyet2 ,imgmeyyet3 ,imgmeyyet4 ;
  MediaPlayer mpmeyyet ,mpmeyyet1 ,mpmeyyet2 ,mpmeyyet3 ,mpmeyyet4 ;
  FloatingActionButton fab ;
  Button btnmeyyet;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_meyet);

    txt_meyyet  = findViewById(R.id.txt_meyyet);
    txt_meyyet1  = findViewById(R.id.txt_meyyet1 );
    txt_meyyet2  = findViewById(R.id.txt_meyyet2 );
    txt_meyyet3  = findViewById(R.id.txt_meyyet3 );
    txt_meyyet4  = findViewById(R.id.txt_meyyet4 );
    txt_meyyet5  = findViewById(R.id.txt_meyyet5 );
    txt_meyyet6  = findViewById(R.id.txt_meyyet6 );
    txt_meyyet7  = findViewById(R.id.txt_meyyet7 );
    txt_meyyet8  = findViewById(R.id.txt_meyyet8 );
    txt_meyyet9  = findViewById(R.id.txt_meyyet9 );
    txt_meyyet10 = findViewById(R.id.txt_meyyet10);


    imgmeyyet = findViewById(R.id.imgmeyyet);
    imgmeyyet1 = findViewById(R.id.imgmeyyet1);
    imgmeyyet2 = findViewById(R.id.imgmeyyet2);
    imgmeyyet3 = findViewById(R.id.imgmeyyet3);
    imgmeyyet4 = findViewById(R.id.imgmeyyet4);


    mpmeyyet = MediaPlayer.create(this, R.raw.subhanakallahumma);
    mpmeyyet1 = MediaPlayer.create(this, R.raw.salawats);
    mpmeyyet2 = MediaPlayer.create(this, R.raw.meyyet_mature);
    mpmeyyet3 = MediaPlayer.create(this, R.raw.meyyet_boy);
    mpmeyyet4 = MediaPlayer.create(this, R.raw.meyyet_girl);

    btnmeyyet= findViewById(R.id.btnmeyyet);


    imgmeyyet.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mpmeyyet.start();
      }
    });

    imgmeyyet1.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mpmeyyet1.start();
      }
    });

    imgmeyyet2.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mpmeyyet2.start();
      }
    });

    imgmeyyet3.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mpmeyyet3.start();
      }
    });

    imgmeyyet4.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mpmeyyet4.start();
      }
    });



    fab = findViewById(R.id.fab);

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
//TODO something when floating action menu first item clicked
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,btnmeyyet.getText().toString()+
          txt_meyyet  .getText().toString()+
          txt_meyyet1 .getText().toString()+
          txt_meyyet2 .getText().toString()+
          txt_meyyet3 .getText().toString()+
          txt_meyyet4 .getText().toString()+
          txt_meyyet5 .getText().toString()+
          txt_meyyet6 .getText().toString()+
          txt_meyyet7 .getText().toString()+
          txt_meyyet8 .getText().toString()+
          txt_meyyet9 .getText().toString()+
          txt_meyyet10.getText().toString()
        );
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        startActivity(Intent.createChooser(shareIntent, "اشتراک ..."));
      }
    });

  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    Intent intent = new Intent(MeyetActivity.this, PrayerActivity.class);
    MeyetActivity.this.startActivity(intent);
  }
}
