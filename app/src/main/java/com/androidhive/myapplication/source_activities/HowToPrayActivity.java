package com.androidhive.myapplication.source_activities;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.androidhive.myapplication.PrayerActivity;
import com.androidhive.myapplication.R;

public class HowToPrayActivity extends AppCompatActivity {

  TextView txtSubject1;
  TextView txtExplanation1;
  TextView txtExplanation2;
  TextView txtExplanation3;
  TextView txtExplanation4;
  TextView txtExplanation5;
  TextView txtExplanation6;

  FloatingActionButton fab ;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_howtopray);

    txtSubject1 = findViewById(R.id.txtSubject1);
    txtExplanation1 = findViewById(R.id.txtExplanation1);
    txtExplanation2 = findViewById(R.id.txtExplanation2);
    txtExplanation3 = findViewById(R.id.txtExplanation3);
    txtExplanation4 = findViewById(R.id.txtExplanation4);
    txtExplanation5 = findViewById(R.id.txtExplanation5);
    txtExplanation6 = findViewById(R.id.txtExplanation6);

    txtSubject1.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View arg0) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,txtExplanation1.getText().toString() + txtExplanation2.getText().toString()+txtExplanation3.getText().toString()+txtExplanation4.getText().toString()+txtExplanation5.getText().toString()+txtExplanation6.getText().toString());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "iran");
        startActivity(Intent.createChooser(shareIntent, "اشتراک ..."));

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
            txtSubject1 .getText().toString()+
          txtExplanation1 .getText().toString()+
          txtExplanation2 .getText().toString()+
          txtExplanation3 .getText().toString()+
          txtExplanation4 .getText().toString()+
          txtExplanation5 .getText().toString()+
          txtExplanation6 .getText().toString()
        );
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        startActivity(Intent.createChooser(shareIntent, "اشتراک ..."));
      }
    });

  }
    @Override
    public void onBackPressed() {
     super.onBackPressed();

     Intent intent = new Intent(HowToPrayActivity.this, PrayerActivity.class);
     HowToPrayActivity.this.startActivity(intent);
    }
}
