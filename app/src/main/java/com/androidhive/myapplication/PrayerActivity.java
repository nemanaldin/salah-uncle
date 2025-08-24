package com.androidhive.myapplication;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import com.androidhive.myapplication.source_activities.HowToPrayActivity;
import com.androidhive.myapplication.source_activities.MeyetActivity;



public class PrayerActivity extends AppCompatActivity {

  CardView btn_dailypray ;
  CardView btn_jomee      ;
  CardView btn_jamaat    ;
  CardView btn_howtopray ;
  CardView btn_ghaza     ;
  CardView btn_meyet     ;
  CardView btn_ghadr     ;
  CardView btn_eshragh   ;
  CardView btn_ayat      ;
  CardView btn_mosafer   ;
  CardView btn_eyd       ;
  CardView btn_awabin    ;
  CardView btn_taravih   ;
  CardView btn_tahiyatmasjid ;
  CardView btn_tahajod  ;
  CardView btn_tasabih    ;

  public static Activity fa;
  Context context;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.test);


    btn_dailypray = findViewById(R.id.btn_dailypray);
    btn_jomee     = findViewById(R.id.btn_jomee    );
    btn_jamaat    = findViewById(R.id.btn_jamaat   );
    btn_howtopray = findViewById(R.id.btn_howtopray);
    btn_ghaza     = findViewById(R.id.btn_ghaza    );
    btn_meyet     = findViewById(R.id.btn_meyet    );
    btn_ghadr     = findViewById(R.id.btn_ghadr    );
    btn_eshragh   = findViewById(R.id.btn_eshragh  );
    btn_ayat      = findViewById(R.id.btn_ayat     );
    btn_mosafer   = findViewById(R.id.btn_mosafer  );
    btn_eyd       = findViewById(R.id.btn_eyd      );
    btn_awabin    = findViewById(R.id.btn_awabin   );
    btn_taravih   = findViewById(R.id.btn_taravih  );
    btn_tahiyatmasjid = findViewById(R.id.btn_tahiyatmasjid);
    btn_tahajod   = findViewById(R.id.btn_tahajod);
    btn_tasabih   = findViewById(R.id.btn_tasabih );


    btn_dailypray.setOnClickListener(listener);
    btn_jomee.setOnClickListener(listener);
    btn_jamaat.setOnClickListener(listener);
    btn_howtopray.setOnClickListener(listener);
    btn_ghaza.setOnClickListener(listener);
    btn_meyet.setOnClickListener(listener);
    btn_ghadr.setOnClickListener(listener);
    btn_eshragh.setOnClickListener(listener);
    btn_ayat.setOnClickListener(listener);
    btn_mosafer.setOnClickListener(listener);
    btn_eyd.setOnClickListener(listener);
    btn_awabin.setOnClickListener(listener);
    btn_taravih.setOnClickListener(listener);
    btn_tahiyatmasjid.setOnClickListener(listener);
    btn_tahajod.setOnClickListener(listener);
    btn_tasabih.setOnClickListener(listener);

    fa = this;



  }


  View.OnClickListener listener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Intent intent;
      int id = v.getId();

      if (id == R.id.btn_dailypray) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 0);
        startActivity(intent);
      } else if (id == R.id.btn_howtopray) {
        intent = new Intent(PrayerActivity.this, HowToPrayActivity.class);
        startActivity(intent);
        PrayerActivity.fa.finish();
      } else if (id == R.id.btn_jomee) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 1);
        startActivity(intent);
      } else if (id == R.id.btn_jamaat) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 2);
        startActivity(intent);
      } else if (id == R.id.btn_ghaza) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 3);
        startActivity(intent);
      } else if (id == R.id.btn_meyet) {
        intent = new Intent(PrayerActivity.this, MeyetActivity.class);
        startActivity(intent);
        PrayerActivity.fa.finish();
      } else if (id == R.id.btn_eyd) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 4);
        startActivity(intent);
      } else if (id == R.id.btn_mosafer) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 5);
        startActivity(intent);
      } else if (id == R.id.btn_ayat) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 6);
        startActivity(intent);
      } else if (id == R.id.btn_tahajod) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 7);
        startActivity(intent);
      } else if (id == R.id.btn_ghadr) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 8);
        startActivity(intent);
      } else if (id == R.id.btn_eshragh) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 9);
        startActivity(intent);
      } else if (id == R.id.btn_awabin) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 10);
        startActivity(intent);
      } else if (id == R.id.btn_tasabih) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 11);
        startActivity(intent);
      } else if (id == R.id.btn_taravih) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 12);
        startActivity(intent);
      } else if (id == R.id.btn_tahiyatmasjid) {
        intent = new Intent(PrayerActivity.this, TextActivity.class);
        intent.putExtra("info", "index " + 13);
        startActivity(intent);
      }
    }
  };






}