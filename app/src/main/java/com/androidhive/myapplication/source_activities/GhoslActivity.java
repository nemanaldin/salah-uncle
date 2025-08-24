package com.androidhive.myapplication.source_activities;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.androidhive.myapplication.R;


public class GhoslActivity extends AppCompatActivity {

  FloatingActionButton fab ;

  TextView txt_ghosl;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ghosl);


    txt_ghosl = findViewById(R.id.txt_meyyet);


    fab = findViewById(R.id.fab);

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
//TODO something when floating action menu first item clicked
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,txt_ghosl.getText().toString());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "iran");
        startActivity(Intent.createChooser(shareIntent, "اشتراک ..."));
      }
    });



  }
}
