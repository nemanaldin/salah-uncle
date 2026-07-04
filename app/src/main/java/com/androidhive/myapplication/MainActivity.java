package com.androidhive.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.androidhive.myapplication.compass.CompassActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

  private CardView btn_namaz, btn_vuzu, btn_ghosl, btn_tasbihat, btn_tayammom, btn_sejdeyesahv, btn_compass, btn_adhan_times;
  private View prayer_times_card;

  private TextView textNextPrayer, textTimeRemaining, textLocation;

  private final Handler countdownHandler = new Handler(Looper.getMainLooper());
  private Runnable countdownRunnable;

  private static final int TIME_INTERVAL = 2000;
  private long mBackPressed;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Toolbar + Drawer
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    // Buttons
    btn_namaz = findViewById(R.id.btn_namaz);
    btn_vuzu = findViewById(R.id.btn_vuzu);
    btn_ghosl = findViewById(R.id.btn_ghosl);
    btn_tasbihat = findViewById(R.id.btn_tasbihat);
    btn_tayammom = findViewById(R.id.btn_tayammom);
    btn_sejdeyesahv = findViewById(R.id.btn_sejdeyesahv);
    btn_compass = findViewById(R.id.btn_compass);
    btn_adhan_times = findViewById(R.id.btn_adhan_times);
    prayer_times_card = findViewById(R.id.prayer_times_card);

    btn_namaz.setOnClickListener(this);
    btn_vuzu.setOnClickListener(this);
    btn_ghosl.setOnClickListener(this);
    btn_tasbihat.setOnClickListener(this);
    btn_tayammom.setOnClickListener(this);
    btn_sejdeyesahv.setOnClickListener(this);
    btn_compass.setOnClickListener(this);
    btn_adhan_times.setOnClickListener(this);
    prayer_times_card.setOnClickListener(this);

    // Prayer UI on home
    textNextPrayer = findViewById(R.id.text_next_prayer);
    textTimeRemaining = findViewById(R.id.text_time_remaining);
    textLocation = findViewById(R.id.text_location);

    handleOnBackPressed();
  }

  @Override
  protected void onResume() {
    super.onResume();

    // اگر auto روشنه، همزمان schedule هم آپدیت بشه
    // (PrayerScheduler خودش چک می‌کنه auto روشن هست یا نه)
    PrayerScheduler.computeAndScheduleNext(this);

    renderFromPrefsAndStartCountdown();
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopCountdown();
  }

  private void renderFromPrefsAndStartCountdown() {
    // Location from single source
    double lat = AppPrefs.getLat(this);
    double lon = AppPrefs.getLon(this);
    String mode = AppPrefs.getLocationMode(this);

    if (lat == 0 && lon == 0 || "none".equals(mode)) {
      textLocation.setText("در انتظار انتخاب مکان");
    } else if ("manual".equals(mode)) {
      String city = AppPrefs.getManualCityName(this);
      if (city == null || city.trim().isEmpty()) city = "مکان دستی";
      textLocation.setText("موقعیت فعلی: " + city);
    } else {
      textLocation.setText(String.format(Locale.US, "موقعیت فعلی: %.2f,%.2f", lat, lon));
    }


    // Next prayer from single source
    String nextName = AppPrefs.getNextPrayerName(this);
    long nextMillis = AppPrefs.getNextPrayerTime(this); // ✅ این متد داخل AppPrefs هست

    if (nextMillis <= 0 || nextName == null || nextName.trim().isEmpty()) {

      String hhmm = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(nextMillis));
      textNextPrayer.setText("نماز بعدی: " + nextName + " — " + hhmm);
      textTimeRemaining.setText("--:--:--");
      stopCountdown();
      return;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    textNextPrayer.setText("نماز بعدی: " + nextName + " - " + sdf.format(new Date(nextMillis)));

    startCountdown(nextMillis);
  }

  private void startCountdown(long targetMillis) {
    stopCountdown();

    countdownRunnable = new Runnable() {
      @Override
      public void run() {
        long diff = targetMillis - System.currentTimeMillis();
        if (diff < 0) diff = 0;

        long h = TimeUnit.MILLISECONDS.toHours(diff);
        long m = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        long s = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;

        textTimeRemaining.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));

        if (diff > 0) {
          countdownHandler.postDelayed(this, 1000);
        } else {
          // رسید → دوباره از prefs بخون (ممکنه Scheduler نماز بعدی رو ست کرده باشه)
          countdownHandler.postDelayed(() -> renderFromPrefsAndStartCountdown(), 500);
        }
      }
    };

    countdownHandler.post(countdownRunnable);
  }

  private void stopCountdown() {
    if (countdownRunnable != null) {
      countdownHandler.removeCallbacks(countdownRunnable);
      countdownRunnable = null;
    }
  }

  private void handleOnBackPressed() {
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
          drawer.closeDrawer(GravityCompat.START);
          return;
        }

        if (mBackPressed + 2000 > System.currentTimeMillis()) {
          finish();
        } else {
          Snackbar.make(findViewById(android.R.id.content),
                  "برای خروج دوباره بازگشت را بزنید",
                  Snackbar.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();

      }
    };
    getOnBackPressedDispatcher().addCallback(this, callback);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();

    if (id == R.id.btn_namaz) {
      startActivity(new Intent(this, PrayerActivity.class));
    } else if (id == R.id.btn_vuzu) {
      startActivity(new Intent(this, com.androidhive.myapplication.source_activities.VuzuActivity.class));
    } else if (id == R.id.btn_ghosl) {
      startActivity(new Intent(this, com.androidhive.myapplication.source_activities.GhoslActivity.class));
    } else if (id == R.id.btn_tasbihat) {
      startActivity(new Intent(this, com.androidhive.myapplication.source_activities.TasbihatActivity.class));
    } else if (id == R.id.btn_tayammom) {
      startActivity(new Intent(this, com.androidhive.myapplication.source_activities.TayammomActivity.class));
    } else if (id == R.id.btn_sejdeyesahv) {
      startActivity(new Intent(this, com.androidhive.myapplication.source_activities.SejdeyyesahvActivity.class));
    } else if (id == R.id.btn_compass) {
      startActivity(new Intent(this, CompassActivity.class));
    } else if (id == R.id.prayer_times_card || id == R.id.btn_adhan_times) {
      startActivity(new Intent(this, PrayerTimesActivity.class));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.nav_comment) {
      // نظر دادن (باز کردن صفحه گوگل پلی)
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse("bazaar://details?id=" + "com.androidhive.myapplication"));
      startActivity(intent);

    } else if (id == R.id.nav_sources) {
      // منابع
      // اگر اکتیویتی منابع داری:
      startActivity(new Intent(this, com.androidhive.myapplication.source_activities.SourceActivity.class));

    } else if (id == R.id.nav_share) {
      // اشتراک گذاری
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");
      shareIntent.putExtra(Intent.EXTRA_SUBJECT, "برنامه اوقات شرعی");
      shareIntent.putExtra(Intent.EXTRA_TEXT,
              "برنامه نماز فقه حنفی\n" +
                      "دانلود:\n" +
                      "https://cafebazaar.ir/app/com.androidhive.myapplication" + getPackageName()
      );
      startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری با"));

    } else if (id == R.id.nav_about) {
      startActivity(new Intent(this, com.androidhive.myapplication.source_activities.AboutActivity.class));
    }

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }
}
