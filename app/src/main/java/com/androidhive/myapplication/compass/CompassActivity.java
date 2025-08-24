package com.androidhive.myapplication.compass;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.androidhive.myapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.view.View.INVISIBLE;

public class CompassActivity extends AppCompatActivity {
    private static final String TAG = "CompassActivity";

    private Compass compass;
    private ImageView arrowViewQiblat;
    private ImageView imageDial;
    private TextView text_atas;
    private TextView text_bawah;
    private MenuItem gpsMenuItem;

    private float currentAzimuth;
    private SharedPreferences prefs;

    // Fused Location Provider Client برای دریافت مختصات دقیق‌تر و مدرن‌تر
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        // مقداردهی اولیه SharedPreferences
        prefs = getSharedPreferences("QiblaPrefs", MODE_PRIVATE);

        // مقداردهی اولیه FusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        arrowViewQiblat = findViewById(R.id.main_image_qiblat);
        imageDial = findViewById(R.id.main_image_dial);
        text_atas = findViewById(R.id.teks_atas);
        text_bawah = findViewById(R.id.teks_bawah);

        arrowViewQiblat.setVisibility(INVISIBLE);
        arrowViewQiblat.setVisibility(View.GONE);

        setupCompass();

        Toast.makeText(this, "برای دریافت مختصات، روی آیکون GPS بالا سمت راست ضربه بزنید.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "start compass");
        if (compass != null) {
            compass.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (compass != null) {
            compass.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (compass != null) {
            compass.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "stop compass");
        if (compass != null) {
            compass.stop();
        }
    }

    private void setupCompass() {
        boolean permissionGranted = GetBoolean("permission_granted");
        if (permissionGranted) {
            getBearing();
        } else {
            text_atas.setText(getResources().getString(R.string.msg_permission_not_granted_yet));
            text_bawah.setText(getResources().getString(R.string.msg_permission_not_granted_yet));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }
        }

        compass = new Compass(this);
        Compass.CompassListener cl = new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(float azimuth) {
                adjustGambarDial(azimuth);
                adjustArrowQiblat(azimuth);
            }
        };
        compass.setListener(cl);
    }

    public void adjustGambarDial(float azimuth) {
        Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = azimuth;
        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        imageDial.startAnimation(an);
    }

    public void adjustArrowQiblat(float azimuth) {
        float kiblat_derajat = GetFloat("kiblat_derajat");
        Animation an = new RotateAnimation(-(currentAzimuth) + kiblat_derajat, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        arrowViewQiblat.startAnimation(an);
        if (kiblat_derajat > 0) {
            arrowViewQiblat.setVisibility(View.VISIBLE);
        } else {
            arrowViewQiblat.setVisibility(INVISIBLE);
            arrowViewQiblat.setVisibility(View.GONE);
        }
    }

    public void getBearing() {
        // ابتدا بررسی می‌کنیم که آیا مختصات قبلی ذخیره شده است یا نه
        float kiblat_derajat = GetFloat("kiblat_derajat");
        if (kiblat_derajat > 0.0001) {
            text_bawah.setText(getResources().getString(R.string.your_location) + " " + getResources().getString(R.string.using_last_location));
            text_atas.setText(getResources().getString(R.string.qibla_direction) + " " + kiblat_derajat + " " + getResources().getString(R.string.degree_from_north));
            if (gpsMenuItem != null) {
                gpsMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.gps_on));
            }
            arrowViewQiblat.setVisibility(View.VISIBLE);
        } else {
            // اگر مختصات ذخیره شده نبود، مختصات جدید را دریافت می‌کنیم
            fetch_GPS_modern();
        }
    }

    @SuppressLint("MissingPermission")
    public void fetch_GPS_modern() {
        // بررسی مجوز دسترسی به مکان
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "مجوز دسترسی به مکان اعطا نشده است.", Toast.LENGTH_LONG).show();
            // درخواست مجوز مجدد
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            text_bawah.setText(getResources().getString(R.string.your_location) + "\nLat: " + latitude + " Long: " + longitude);

                            // محاسبه جهت قبله
                            double kaabaLat = Math.toRadians(21.422487);
                            double kaabaLon = Math.toRadians(39.826206);
                            double userLat = Math.toRadians(latitude);
                            double userLon = Math.toRadians(longitude);

                            double lonDifference = kaabaLon - userLon;
                            double y = Math.sin(lonDifference) * Math.cos(kaabaLat);
                            double x = Math.cos(userLat) * Math.sin(kaabaLat) - Math.sin(userLat) * Math.cos(kaabaLat) * Math.cos(lonDifference);
                            double result = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;

                            float result2 = (float) result;
                            SaveFloat("kiblat_derajat", result2);

                            text_atas.setText(getResources().getString(R.string.qibla_direction) + " " + result2 + " " + getResources().getString(R.string.degree_from_north));
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.qibla_direction) + " " + result2 + " " + getResources().getString(R.string.degree_from_north), Toast.LENGTH_LONG).show();
                            arrowViewQiblat.setVisibility(View.VISIBLE);

                            if (gpsMenuItem != null) {
                                gpsMenuItem.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gps_on));
                            }
                        } else {
                            // در صورت عدم دریافت مکان
                            text_atas.setText(getResources().getString(R.string.location_not_ready));
                            text_bawah.setText(getResources().getString(R.string.location_not_ready));
                            if (gpsMenuItem != null) {
                                gpsMenuItem.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gps_off));
                            }
                            Toast.makeText(getApplicationContext(), "Location not ready.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // مجوز اعطا شد، مختصات را دریافت کن
                SaveBoolean("permission_granted", true);
                text_atas.setText(getResources().getString(R.string.msg_permission_granted));
                text_bawah.setText(getResources().getString(R.string.msg_permission_granted));
                fetch_GPS_modern();
            } else {
                Toast.makeText(this, getResources().getString(R.string.toast_permission_required), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // region SharedPreferences helper methods
    public void SaveBoolean(String Judul, Boolean bbb) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(Judul, bbb);
        edit.apply();
    }

    public Boolean GetBoolean(String Judul) {
        return prefs.getBoolean(Judul, false);
    }

    public void SaveFloat(String Judul, Float bbb) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putFloat(Judul, bbb);
        edit.apply();
    }

    public Float GetFloat(String Judul) {
        return prefs.getFloat(Judul, 0);
    }
    // endregion

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gps, menu);
        gpsMenuItem = menu.findItem(R.id.gps);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.gps) {
            fetch_GPS_modern();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
