package com.androidhive.myapplication.source_activities;

import android.content.Intent;
import android.media.MediaPlayer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidhive.myapplication.R;

public class VuzuActivity extends AppCompatActivity {

  TextView  vuzu1, vuzu2,vuzu3,vuzu4, vuzu5, vuzu6,vuzu7,vuzu8,vuzu9,vuzu10,vuzu11,vuzu_text1,vuzu_text2,vuzu_text3 ;
  MediaPlayer mp1 ,mp2 ,mp3, mp4 ,mp5, mp6 ,mp7, mp8 ,mp9 ,mp10 ,mp11 ;
  ImageView img_btn2,img_btn3,img_btn4,img_btn5,img_btn6,img_btn7,img_btn8,img_btn9,img_btn10,img_btn11,img_btn12 ;
   Button btnvuzu;
  FloatingActionButton fab ;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_vuzu);

    btnvuzu = findViewById(R.id.btn_vuzu);

    img_btn2 = findViewById(R.id.img_btn2);
    img_btn3 = findViewById(R.id.img_btn3);
    img_btn4 = findViewById(R.id.img_btn4);
    img_btn5 = findViewById(R.id.img_btn5);
    img_btn6 = findViewById(R.id.img_btn6);
    img_btn7 = findViewById(R.id.img_btn7);
    img_btn8 = findViewById(R.id.img_btn8);
    img_btn9 = findViewById(R.id.img_btn9);
    img_btn10= findViewById(R.id.img_btn10);
    img_btn11= findViewById(R.id.img_btn11);
    img_btn12= findViewById(R.id.img_btn12);

    vuzu1 = findViewById(R.id.txt_vuzu1 );
    vuzu2 = findViewById(R.id.txt_vuzu2 );
    vuzu3 = findViewById(R.id.txt_vuzu3 );
    vuzu4 = findViewById(R.id.txt_vuzu4 );
    vuzu5 = findViewById(R.id.txt_vuzu5 );
    vuzu6 = findViewById(R.id.txt_vuzu6 );
    vuzu7 = findViewById(R.id.txt_vuzu7 );
    vuzu8 = findViewById(R.id.txt_vuzu8 );
    vuzu9 = findViewById(R.id.txt_vuzu9 );
    vuzu10= findViewById(R.id.txt_vuzu10);
    vuzu11= findViewById(R.id.txt_vuzu11);
    vuzu_text1= findViewById(R.id.vuzu_text1);
    vuzu_text2= findViewById(R.id.vuzu_text2);
    vuzu_text3= findViewById(R.id.vuzu_text3);



    mp1 = MediaPlayer.create(this, R.raw.auuzu_billah);
    mp2 = MediaPlayer.create(this, R.raw.vuzu_bismillahi);
    mp3 = MediaPlayer.create(this, R.raw.vuzu3);
    mp4 = MediaPlayer.create(this, R.raw.vuzu4);
    mp5 = MediaPlayer.create(this, R.raw.vuzu5);
    mp6 = MediaPlayer.create(this, R.raw.vuzu6);
    mp7 = MediaPlayer.create(this, R.raw.vuzu7);
    mp8 = MediaPlayer.create(this, R.raw.vuzu8);
    mp9 = MediaPlayer.create(this, R.raw.vuzu9);
    mp10= MediaPlayer.create(this, R.raw.vuzu10);
    mp11= MediaPlayer.create(this, R.raw.vuzu11);



    fab = findViewById(R.id.fab);


    img_btn2.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mp1.start();
      }
      });

    img_btn3.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mp2.start();
      }
      });

    img_btn4.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mp3.start();
      }
      });

    img_btn5.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mp4.start();
      }
      });

    img_btn6.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mp5.start();
      }
      });

    img_btn7.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mp6.start();
      }
      });

    img_btn8.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mp7.start();
      }
      });

    img_btn9.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mp8.start();
      }
      });

    img_btn10.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mp9.start();
      }
      });

    img_btn11.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mp10.start();
      }
      });

    img_btn12.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        mp11.start();
      }
      });

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
//TODO something when floating action menu first item clicked
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
          btnvuzu.getText().toString()+
            vuzu_text1.getText().toString()+
          vuzu1.getText().toString()+
          vuzu_text2.getText().toString()+
          vuzu2.getText().toString()+
          vuzu3.getText().toString()+
          vuzu4.getText().toString()+
          vuzu5.getText().toString()+
          vuzu6.getText().toString()+
          vuzu7.getText().toString()+
          vuzu8.getText().toString()+
          vuzu9.getText().toString()+
          vuzu10.getText().toString()+
          vuzu11.getText().toString()+
          vuzu_text3.getText().toString());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        startActivity(Intent.createChooser(shareIntent, "اشتراک ..."));
      }
    });

  }
}
