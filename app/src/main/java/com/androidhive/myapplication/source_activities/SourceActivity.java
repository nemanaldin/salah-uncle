package com.androidhive.myapplication.source_activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.androidhive.myapplication.R;

public class SourceActivity extends AppCompatActivity {

  TextView txt_source ;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_source);

    txt_source = (TextView) findViewById(R.id.txt_source);

    txt_source.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent= new Intent(Intent.ACTION_EDIT);
        Intent i = new Intent(Intent.ACTION_VIEW , Uri.parse("http://www.erfanabad.org/fa/modules/news/article.php?storyid=41"));
        startActivity(i);
      }
    });


  }
}
