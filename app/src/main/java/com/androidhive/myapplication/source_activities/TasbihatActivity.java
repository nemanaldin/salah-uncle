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

import com.androidhive.myapplication.R;

public class TasbihatActivity extends AppCompatActivity {


  TextView txt_tas,txt_tas1,txt_tas2,txt_tas3,txt_tas4,txt_tas5,txt_tas6,txt_tas7,txt_tas8,txt_tas9,
    txt_tas10,txt_tas11,txt_tas12,txt_tas13,txt_tas14,txt_tas15,txt_tas16,txt_tas17,txt_tas18,txt_tas19,txt_tas20,txt_tas21,txt_tas22,txt_tas23;

  ImageView imgtas1,imgtas2,imgtas3,imgtas4,imgtas5,imgtas6,imgtas7,imgtas8,imgtas9,
    imgtas10,imgtas11,imgtas12,imgtas13,imgtas14,imgtas15,imgtas16,imgtas17,imgtas18,imgtas19,imgtas20,imgtas21,imgtas22;

  MediaPlayer mptas1 ,mptas2 ,mptas3 ,mptas4 ,mptas5 ,mptas6 ,mptas7 ,mptas8 ,mptas9 ,mptas10 ,
  mptas11 ,mptas12 ,mptas13 ,mptas14 ,mptas15 ,mptas16 ,mptas17 ,mptas18 ,mptas19 ,mptas20 ,mptas21 ,mptas22  ;

  FloatingActionButton fab ;
Button btntasbihat;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tasbihat);

    txt_tas  = findViewById(R.id.txt_tas);
    txt_tas1 = findViewById(R.id.txt_tas1);
    txt_tas2 = findViewById(R.id.txt_tas2);
    txt_tas3 = findViewById(R.id.txt_tas3);
    txt_tas4 = findViewById(R.id.txt_tas4);
    txt_tas5 = findViewById(R.id.txt_meyyet1);
    txt_tas6 = findViewById(R.id.txt_tas6);
    txt_tas7 = findViewById(R.id.txt_tas7);
    txt_tas8 = findViewById(R.id.txt_tas8);
    txt_tas9 = findViewById(R.id.txt_tas9);
    txt_tas10= findViewById(R.id.txt_tas10);
    txt_tas11= findViewById(R.id.txt_tas11);
    txt_tas12= findViewById(R.id.txt_tas12);
    txt_tas13= findViewById(R.id.txt_tas13);
    txt_tas14= findViewById(R.id.txt_tas14);
    txt_tas15= findViewById(R.id.txt_tas15);
    txt_tas16= findViewById(R.id.txt_tas16);
    txt_tas17= findViewById(R.id.txt_tas17);
    txt_tas18= findViewById(R.id.txt_tas18);
    txt_tas19= findViewById(R.id.txt_tas19);
    txt_tas20= findViewById(R.id.txt_tas20);
    txt_tas21= findViewById(R.id.txt_tas21);
    txt_tas22= findViewById(R.id.txt_tas22);
    txt_tas23= findViewById(R.id.txt_tas23);

    imgtas1  = findViewById(R.id.imgtas1);
    imgtas2  = findViewById(R.id.imgtas2 );
    imgtas3  = findViewById(R.id.imgtas3 );
    imgtas4  = findViewById(R.id.imgtas4 );
    imgtas5  = findViewById(R.id.imgtas5 );
    imgtas6  = findViewById(R.id.imgtas6 );
    imgtas7  = findViewById(R.id.imgtas7 );
    imgtas8  = findViewById(R.id.imgtas8 );
    imgtas9  = findViewById(R.id.imgtas9 );
    imgtas10 = findViewById(R.id.imgtas10);
    imgtas11 = findViewById(R.id.imgtas11);
    imgtas12 = findViewById(R.id.imgtas12);
    imgtas13 = findViewById(R.id.imgtas13);
    imgtas14 = findViewById(R.id.imgtas14);
    imgtas15 = findViewById(R.id.imgtas15);
    imgtas16 = findViewById(R.id.imgtas16);
    imgtas17 = findViewById(R.id.imgtas17);
    imgtas18 = findViewById(R.id.imgtas18);
    imgtas19 = findViewById(R.id.imgtas19);
    imgtas20 = findViewById(R.id.imgtas20);
    imgtas21 = findViewById(R.id.imgtas21);
    imgtas22 = findViewById(R.id.imgtas22);

    mptas1 = MediaPlayer.create(this, R.raw.subhanallah);
    mptas2  = MediaPlayer.create(this, R.raw.alhamdolillah);
    mptas3  = MediaPlayer.create(this, R.raw.allaho_akbar);
    mptas4  = MediaPlayer.create(this, R.raw.la_ilaha_vahdahu);
    mptas5  = MediaPlayer.create(this, R.raw.ajirna_minannar);
    mptas6  = MediaPlayer.create(this, R.raw.adkhilnaljannah);
    mptas7  = MediaPlayer.create(this, R.raw.la_ilah);
    mptas8  = MediaPlayer.create(this, R.raw.mohammad);
    mptas9  = MediaPlayer.create(this, R.raw.salawat);
    mptas10 = MediaPlayer.create(this, R.raw.astaghfir);
    mptas11 = MediaPlayer.create(this, R.raw.yahadi);
    mptas12 = MediaPlayer.create(this, R.raw.salawat);
    mptas13 = MediaPlayer.create(this, R.raw.ruku);
    mptas14 = MediaPlayer.create(this, R.raw.sejde);
    mptas15 = MediaPlayer.create(this, R.raw.samiallaho);
    mptas16 = MediaPlayer.create(this, R.raw.rabbanalakalhamd);
    mptas17 = MediaPlayer.create(this, R.raw.subhanakallahumma);
    mptas18 = MediaPlayer.create(this, R.raw.attahiyat);
    mptas19 = MediaPlayer.create(this, R.raw.salawats);
    mptas20 = MediaPlayer.create(this, R.raw.dua);
    mptas21 = MediaPlayer.create(this, R.raw.inna_nastaeinoka);
    mptas22 = MediaPlayer.create(this, R.raw.farz);

    btntasbihat = findViewById(R.id.btntasbihat);

    imgtas1.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas1.start();
      }
    });

    imgtas2.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas2.start();
      }
    });

    imgtas3.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas3.start();
      }
    });

    imgtas4.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas4.start();
      }
    });

    imgtas5.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas5.start();
      }
    });

    imgtas6.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas6.start();
      }
    });

    imgtas7.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas7.start();
      }
    });

    imgtas8.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas8.start();
      }
    });

    imgtas9.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas9.start();
      }
    });

    imgtas10.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas10.start();
      }
    });

    imgtas11.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas11.start();
      }
    });

    imgtas12.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas12.start();
      }
    });

    imgtas13.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas13.start();
      }
    });

    imgtas14.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas14.start();
      }
    });

    imgtas15.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas15.start();
      }
    });

    imgtas16.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas16.start();
      }
    });

    imgtas17.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas17.start();
      }
    });

    imgtas18.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas18.start();
      }
    });

    imgtas19.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas19.start();
      }
    });

    imgtas20.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas20.start();
      }
    });

    imgtas21.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas21.start();
      }
    });

    imgtas22.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mptas22.start();
      }
    });





    fab = findViewById(R.id.fab);

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
//TODO something when floating action menu first item clicked
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
          btntasbihat.getText().toString()+
            txt_tas.getText().toString()+
          txt_tas1 .getText().toString()+
          txt_tas2 .getText().toString()+
          txt_tas3 .getText().toString()+
          txt_tas4 .getText().toString()+
          txt_tas5 .getText().toString()+
          txt_tas6 .getText().toString()+
          txt_tas7 .getText().toString()+
          txt_tas8 .getText().toString()+
          txt_tas9 .getText().toString()+
          txt_tas10.getText().toString()+
          txt_tas11.getText().toString()+
          txt_tas12.getText().toString()+
          txt_tas13.getText().toString()+
          txt_tas14.getText().toString()+
          txt_tas15.getText().toString()+
          txt_tas16.getText().toString()+
          txt_tas17.getText().toString()+
          txt_tas18.getText().toString()+
          txt_tas19.getText().toString()+
          txt_tas20.getText().toString()+
          txt_tas21.getText().toString()+
          txt_tas22.getText().toString()+
          txt_tas23.getText().toString());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        startActivity(Intent.createChooser(shareIntent, "اشتراک ..."));
      }
    });


  }
}
