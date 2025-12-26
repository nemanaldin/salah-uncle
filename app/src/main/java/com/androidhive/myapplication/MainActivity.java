package com.androidhive.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.androidhive.myapplication.PrayerTimesActivity;
import com.batoulapps.adhan.CalculationParameters;
import com.google.android.material.navigation.NavigationView;
import com.androidhive.myapplication.compass.CompassActivity;
import com.androidhive.myapplication.source_activities.AboutActivity;
import com.androidhive.myapplication.source_activities.GhoslActivity;
import com.androidhive.myapplication.source_activities.SejdeyyesahvActivity;
import com.androidhive.myapplication.source_activities.SourceActivity;
import com.androidhive.myapplication.source_activities.TasbihatActivity;
import com.androidhive.myapplication.source_activities.TayammomActivity;
import com.androidhive.myapplication.source_activities.VuzuActivity;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Prayer;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


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
  private View prayerTimesCard;
  private CardView btn_adhan_times;


  // متغیرهای UI برای اوقات شرعی
  private TextView textNextPrayer;
  private TextView textTimeRemaining;
  private TextView textLocation;

  // متغیرهای لازم برای مدیریت خروج با دو ضربه
  private static final int TIME_INTERVAL = 2000;
  private long mBackPressed;
  private static final int PERMISSION_REQUEST_CODE = 100;

  // متغیرهای مربوط به مکان و اوقات شرعی
  private FusedLocationProviderClient fusedLocationClient;
  private Handler handler;
  private Runnable runnable;

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
    prayerTimesCard = findViewById(R.id.prayer_times_card);
    btn_adhan_times = findViewById(R.id.btn_adhan_times);


    btn_namaz.setOnClickListener(this);
    btn_vuzu.setOnClickListener(this);
    btn_ghosl.setOnClickListener(this);
    btn_tasbihat.setOnClickListener(this);
    btn_tayammom.setOnClickListener(this);
    btn_sejdeyesahv.setOnClickListener(this);
    btn_compass.setOnClickListener(this);
    prayerTimesCard.setOnClickListener(this);
    btn_adhan_times.setOnClickListener(this);// کلیک بر روی پنل اوقات شرعی


    // مقداردهی اولیه برای اوقات شرعی
    textNextPrayer = findViewById(R.id.text_next_prayer);
    textTimeRemaining = findViewById(R.id.text_time_remaining);
    textLocation = findViewById(R.id.text_location);
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    // درخواست مجوز دسترسی به موقعیت مکانی
    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
    } else {
      getLocationAndSetPrayerTimes();
    }

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

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PERMISSION_REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // مجوز داده شد، موقعیت مکانی را دریافت کن
        getLocationAndSetPrayerTimes();
      } else {
        Toast.makeText(this, "دسترسی به موقعیت مکانی برای نمایش اوقات شرعی الزامی است.", Toast.LENGTH_LONG).show();
      }
    }
  }


  private void getLocationAndSetPrayerTimes() {
    try {
      fusedLocationClient.getLastLocation()
              .addOnSuccessListener(this, location -> {
                if (location != null) {
                  textLocation.setText("موقعیت فعلی: " + String.format(Locale.US, "%.2f, %.2f", location.getLatitude(), location.getLongitude()));
                  updatePrayerTimesUI(location.getLatitude(), location.getLongitude());
                  // شروع به‌روزرسانی زمان باقیمانده
                  handler = new Handler();
                  runnable = new Runnable() {
                    @Override
                    public void run() {
                      updateTimeRemaining(location.getLatitude(), location.getLongitude());
                      handler.postDelayed(this, 1000); // هر یک ثانیه به‌روزرسانی شود
                    }
                  };
                  handler.post(runnable);
                } else {
                  textLocation.setText("در حال دریافت موقعیت...");
                }
              });
    } catch (SecurityException e) {
      textLocation.setText("لطفا مجوز مکان را فعال کنید.");
    }
  }



  private void updatePrayerTimesUI(double latitude, double longitude) {
    DateComponents date = DateComponents.from(new Date());
    Coordinates coordinates = new Coordinates(latitude, longitude);

    // Create CalculationParameters using the desired CalculationMethod
    CalculationParameters params = CalculationMethod.KARACHI.getParameters();

    // Pass the CalculationParameters object to the PrayerTimes constructor
    PrayerTimes prayerTimes = new PrayerTimes(coordinates, date, params);

    // اینجا می‌توانید اوقات شرعی را در متغیرها ذخیره کنید
    // برای مثال: prayerTimes.fajr, prayerTimes.sunrise, ...
  }

  // ... inside your MainActivity class

  private void updateTimeRemaining(double latitude, double longitude) {
    DateComponents date = DateComponents.from(new Date());
    Coordinates coordinates = new Coordinates(latitude, longitude);

    CalculationParameters params = CalculationMethod.KARACHI.getParameters();
    PrayerTimes prayerTimes = new PrayerTimes(coordinates, date, params);

    Prayer nextPrayer = prayerTimes.nextPrayer();
    Date nextPrayerTime;
    String nextPrayerName;

    if (nextPrayer != Prayer.NONE) {
      nextPrayerTime = prayerTimes.timeForPrayer(nextPrayer);
      nextPrayerName = mapPrayerName(nextPrayer);
    } else {
      // بعد از عشاء: فجرِ فردا را به عنوان نماز بعدی نشان بده
      java.util.Calendar cal = java.util.Calendar.getInstance();
      cal.setTime(new Date());
      cal.add(java.util.Calendar.DATE, 1);
      DateComponents tomorrow = DateComponents.from(cal.getTime());
      PrayerTimes tomorrowTimes = new PrayerTimes(coordinates, tomorrow, params);

      nextPrayer = Prayer.FAJR;
      nextPrayerTime = tomorrowTimes.fajr;
      nextPrayerName = "فجر";
    }

    long remainingMillis = nextPrayerTime.getTime() - System.currentTimeMillis();
    if (remainingMillis < 0) remainingMillis = 0;

    long hours = TimeUnit.MILLISECONDS.toHours(remainingMillis);
    long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60;
    long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60;

    String timeString = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);

    textNextPrayer.setText("نماز بعدی: " + nextPrayerName);
    textTimeRemaining.setText(timeString);

    // ذخیره برای نوتیفیکیشن/سرویس
    SharedPreferences.Editor editor = getSharedPreferences("PrayerAppPrefs", MODE_PRIVATE).edit();
    editor.putLong("next_prayer_time_millis", nextPrayerTime.getTime());
    editor.putString("next_prayer_name_fa", nextPrayerName);
    editor.apply();
}

  private String mapPrayerName(Prayer prayer) {
    switch (prayer) {
      case FAJR: return "فجر";
      case SUNRISE: return "طلوع آفتاب";
      case DHUHR: return "ظهر";
      case ASR: return "عصر";
      case MAGHRIB: return "مغرب";
      case ISHA: return "عشا";
      default: return "نامعلوم";
    }
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
    } else if (id == R.id.prayer_times_card) {
      intent = new Intent(MainActivity.this, PrayerTimesActivity.class);
    } else if (id == R.id.btn_adhan_times) {
      intent = new Intent(MainActivity.this, PrayerTimesActivity.class);
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

                try {
                  Intent intent = new Intent(Intent.ACTION_EDIT);
                  intent.setData(Uri.parse("bazaar://details?id=" + getPackageName()));
                  intent.setPackage("com.farsitel.bazaar");
                  startActivity(intent);
                  finish(); // بستن Activity پس از کلیک بر روی بله
                } catch (Exception e) {
                  Toast.makeText(MainActivity.this, "نرم‌افزار بازار نصب نیست.", Toast.LENGTH_LONG).show();
                  finish();
                }
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
      try {
        intent = new Intent(Intent.ACTION_EDIT);
        intent.setData(Uri.parse("bazaar://details?id=" + getPackageName()));
        intent.setPackage("com.farsitel.bazaar");
        startActivity(intent);
      } catch (Exception e) {
        Toast.makeText(this, "نرم‌افزار بازار نصب نیست.", Toast.LENGTH_LONG).show();
      }
    } else if (id == R.id.nav_about) {
      intent = new Intent(MainActivity.this, AboutActivity.class);
      startActivity(intent);
    } else if (id == R.id.nav_sources) {
      intent = new Intent(MainActivity.this, SourceActivity.class);
      startActivity(intent);
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

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }
}
