package com.androidhive.myapplication;

import android.app.Activity;
import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.androidhive.myapplication.source_activities.HowToPrayActivity;
import com.androidhive.myapplication.source_activities.MeyetActivity;


public class TextActivity extends AppCompatActivity {

  FloatingActionButton fab;
  TextView txtExplanation;
  TextView txtSubject;
  final Activity activity = this;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_text);


    fab = findViewById(R.id.fab);

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
//TODO something when floating action menu first item clicked
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, txtExplanation.getText().toString());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "nothing");
        startActivity(Intent.createChooser(shareIntent, "اشتراک ..."));
      }
    });

    txtExplanation = (TextView) findViewById(R.id.txtExplanation);
    txtSubject = (TextView) findViewById(R.id.txtSubject);


    if (getIntent() != null) {
      String info = getIntent().getStringExtra("info");
      Log.i("Test", "index is " + info);

      if (info.equals("index " + 0)) {
        txtSubject.setText(R.string.dailypray_title);
        txtExplanation.setText(R.string.dailypray);

      }  else if (info.equals("index " + 1)) {
        txtSubject.setText(R.string.jomee_title);
        txtExplanation.setText(R.string.jomee);

      } else if (info.equals("index " + 2)) {
        txtSubject.setText(R.string.jamaat_title);
        txtExplanation.setText(R.string.jamaat);

      } else if (info.equals("index " + 3)) {
        txtSubject.setText(R.string.ghaza_title);
        txtExplanation.setText(R.string.ghaza);

      } else if (info.equals("index " + 4)) {
        txtSubject.setText(R.string.eyd_title);
        txtExplanation.setText(R.string.eyd);

      }  else if (info.equals("index " + 5)) {
        txtSubject.setText(R.string.mosafer_title);
        txtExplanation.setText(R.string.mosafer);

      } else if (info.equals("index " + 6)) {
        txtSubject.setText(R.string.ayat_title);
        txtExplanation.setText(R.string.ayat);

      } else if (info.equals("index " + 7)) {
        txtSubject.setText(R.string.tahajod_title);
        txtExplanation.setText(R.string.tahajod);

      } else if (info.equals("index " + 8)) {
        txtSubject.setText(R.string.ghadr_title);
        txtExplanation.setText(R.string.ghadr);

      } else if (info.equals("index " + 9)) {
        txtSubject.setText(R.string.eshragh_title);
        txtExplanation.setText(R.string.eshragh);

      } else if (info.equals("index " + 10)) {
        txtSubject.setText(R.string.awabin_title);
        txtExplanation.setText(R.string.awabin);

      } else if (info.equals("index " + 11)) {
        txtSubject.setText(R.string.tasabih_title);
        txtExplanation.setText(R.string.tasabih);

      } else if (info.equals("index " + 12)) {
        txtSubject.setText(R.string.taravih_title);
        txtExplanation.setText(R.string.taravih);

      } else if (info.equals("index " + 13)) {
        txtSubject.setText(R.string.tahiyatmasjid_title);
        txtExplanation.setText(R.string.tahiyatmasjid);

      }


    }

  }


}
