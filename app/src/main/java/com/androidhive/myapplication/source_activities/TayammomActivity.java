package com.androidhive.myapplication.source_activities;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.androidhive.myapplication.R;

public class TayammomActivity extends AppCompatActivity {


  FloatingActionButton fab ;
  TextView txt_tayammom;
  TextView txt_tayammom1;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tayammom);


    txt_tayammom = findViewById(R.id.txt_meyyet);
    txt_tayammom1 = findViewById(R.id.txt_tayammom1);


    fab = findViewById(R.id.fab);

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
//TODO something when floating action menu first item clicked
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,txt_tayammom.getText().toString()+ txt_tayammom1.getText().toString());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        startActivity(Intent.createChooser(shareIntent, "اشتراک ..."));
      }
    });
  }
}
