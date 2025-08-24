package com.androidhive.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.androidhive.myapplication.compass.CompassActivity;
import com.androidhive.myapplication.source_activities.AboutActivity;
import com.androidhive.myapplication.source_activities.GhoslActivity;
import com.androidhive.myapplication.source_activities.SejdeyyesahvActivity;
import com.androidhive.myapplication.source_activities.SourceActivity;
import com.androidhive.myapplication.source_activities.TasbihatActivity;
import com.androidhive.myapplication.source_activities.TayammomActivity;
import com.androidhive.myapplication.source_activities.VuzuActivity;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

  // CardView ها برای سهولت مدیریت
  private CardView btn_namaz;
  private CardView btn_vuzu;
  private CardView btn_ghosl;
  private CardView btn_tasbihat;
  private CardView btn_tayammom;
  private CardView btn_sejdeyesahv;
  private CardView btn_compass;

  // متغیرهای لازم برای مدیریت خروج با دو ضربه
  private static final int TIME_INTERVAL = 2000;
  private long mBackPressed;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // تنظیمات Toolbar و DrawerLayout
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);


    // مقداردهی اولیه CardView ها و تنظیم یک OnClickListener مشترک برای همه
    btn_namaz = findViewById(R.id.btn_namaz);
    btn_vuzu = findViewById(R.id.btn_vuzu);
    btn_ghosl = findViewById(R.id.btn_ghosl);
    btn_tasbihat = findViewById(R.id.btn_tasbihat);
    btn_tayammom = findViewById(R.id.btn_tayammom);
    btn_sejdeyesahv = findViewById(R.id.btn_sejdeyesahv);
    btn_compass = findViewById(R.id.btn_compass);

    btn_namaz.setOnClickListener(this);
    btn_vuzu.setOnClickListener(this);
    btn_ghosl.setOnClickListener(this);
    btn_tasbihat.setOnClickListener(this);
    btn_tayammom.setOnClickListener(this);
    btn_sejdeyesahv.setOnClickListener(this);
    btn_compass.setOnClickListener(this);


    // پیاده‌سازی مدرن دکمه بازگشت با OnBackPressedCallback
    OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
      @Override
      public void handleOnBackPressed() {
        // بررسی وضعیت Drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
          drawerLayout.closeDrawer(GravityCompat.START);
          return; // اگر Drawer باز بود، فقط آن را ببند و خارج شو
        }

        // دسترسی به SharedPreferences
        SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
        boolean boolean_from_sp = shared.getBoolean("myBooleanName", true);

        if (boolean_from_sp) {
          mesg_box_exit();
        } else {
          // منطق دو ضربه برای خروج
          if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            finish();
            return;
          } else {
            Toast.makeText(getBaseContext(), "برای خروج از برنامه بروی دکمه بازگشت دوباره ضربه بزنید!", Toast.LENGTH_SHORT).show();
          }
          mBackPressed = System.currentTimeMillis();
        }
      }
    };
    // اضافه کردن callback به dispatcher
    getOnBackPressedDispatcher().addCallback(this, callback);
  }

  /**
   * یک متد مشترک برای مدیریت تمام کلیک‌ها روی CardView ها
   */
  @Override
  public void onClick(View v) {
    Intent intent = null;
    int id = v.getId();
    if (id == R.id.btn_namaz) {
      intent = new Intent(MainActivity.this, PrayerActivity.class);
    } else if (id == R.id.btn_vuzu) {
      intent = new Intent(MainActivity.this, VuzuActivity.class);
    } else if (id == R.id.btn_ghosl) {
      intent = new Intent(MainActivity.this, GhoslActivity.class);
    } else if (id == R.id.btn_tasbihat) {
      intent = new Intent(MainActivity.this, TasbihatActivity.class);
    } else if (id == R.id.btn_tayammom) {
      intent = new Intent(MainActivity.this, TayammomActivity.class);
    } else if (id == R.id.btn_sejdeyesahv) {
      intent = new Intent(MainActivity.this, SejdeyyesahvActivity.class);
    } else if (id == R.id.btn_compass) {
      intent = new Intent(MainActivity.this, CompassActivity.class);
    }

    if (intent != null) {
      startActivity(intent);
    }
  }


  /**
   * نمایش باکس پیام برای خروج
   */
  public void mesg_box_exit() {
    new AlertDialog.Builder(MainActivity.this)
            .setMessage("آیا با نظر خود در بازار از ما حمایت می کنید؟")
            .setPositiveButton("بله", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = shared.edit();
                editor.putBoolean("myBooleanName", false);
                editor.apply();

                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setData(Uri.parse("bazaar://details?id=" + getPackageName()));
                intent.setPackage("com.farsitel.bazaar");
                startActivity(intent);
                finish(); // بستن Activity پس از کلیک بر روی بله
              }
            })
            .setNegativeButton("خیر! خارج می شوم", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                finish();
              }
            })
            .show();
  }


  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // مدیریت کلیک‌های منوی ناوبری با یک switch
    int id = item.getItemId();
    Intent intent = null;

    if (id == R.id.nav_comment) {
      intent = new Intent(Intent.ACTION_EDIT);
      intent.setData(Uri.parse("bazaar://details?id=" + getPackageName()));
      intent.setPackage("com.farsitel.bazaar");
    } else if (id == R.id.nav_about) {
      intent = new Intent(MainActivity.this, AboutActivity.class);
    } else if (id == R.id.nav_sources) {
      intent = new Intent(MainActivity.this, SourceActivity.class);
    } else if (id == R.id.nav_share) {
      try {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "نماز (فقه حنفی)");
        String shareMessage = "\nاین برنامه آموزش نمازِ" +
                "\n  بهت پیشنهاد میکنم نصبش کنی \n\n";
        shareMessage = shareMessage + "https://cafebazaar.ir/app/com.androidhive.myapplication/?l=fa" + getPackageName() + "\n\n";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "یکی رو انتخاب کن"));
      } catch (Exception e) {
        // در صورت بروز خطا در اشتراک‌گذاری
        Toast.makeText(this, "امکان اشتراک‌گذاری وجود ندارد.", Toast.LENGTH_SHORT).show();
      }
    }

    if (intent != null) {
      startActivity(intent);
    }

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  // متدهای onCreateOptionsMenu و onOptionsItemSelected حذف شدند چون استفاده‌ای از آن‌ها نمی‌شود.
  // متد onActivityResult نیز حذف شد.
}
