package com.androidhive.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity; // از کلاس استاندارد استفاده شده است

public class Prefs extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // روش جدید برای دسترسی به SharedPreferences
    // از getSharedPreferences() به جای getDefaultSharedPreferences() استفاده شده است
    SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);

    Boolean check = prefs.getBoolean("myBooleanName", true);
  }
}
