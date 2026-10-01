package com.androidhive.myapplication.compass

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidhive.myapplication.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener

class CompassActivity : AppCompatActivity() {

    private val TAG = "CompassActivity"

    private lateinit var compass: Compass
    private lateinit var arrowViewQiblat: ImageView
    private lateinit var imageDial: ImageView
    private lateinit var text_atas: TextView
    private lateinit var text_bawah: TextView
    private var gpsMenuItem: MenuItem? = null

    private var currentAzimuth: Float = 0f
    private lateinit var prefs: SharedPreferences

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        prefs = getSharedPreferences("QiblaPrefs", Context.MODE_PRIVATE)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        arrowViewQiblat = findViewById(R.id.main_image_qiblat)
        imageDial = findViewById(R.id.main_image_dial)
        text_atas = findViewById(R.id.teks_atas)
        text_bawah = findViewById(R.id.teks_bawah)

        arrowViewQiblat.visibility = View.INVISIBLE
        arrowViewQiblat.visibility = View.GONE

        setupCompass()

        Toast.makeText(this, "برای دریافت مختصات، روی آیکون GPS بالا سمت راست ضربه بزنید.", Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "start compass")
        compass.start()
    }

    override fun onPause() {
        super.onPause()
        compass.stop()
    }

    override fun onResume() {
        super.onResume()
        compass.start()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "stop compass")
        compass.stop()
    }

    private fun setupCompass() {
        val permissionGranted = GetBoolean("permission_granted")
        if (permissionGranted) {
            getBearing()
        } else {
            text_atas.text = getString(R.string.msg_permission_not_granted_yet)
            text_bawah.text = getString(R.string.msg_permission_not_granted_yet)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    1)
            }
        }

        compass = Compass(this)
        val cl = object : Compass.CompassListener {
            override fun onNewAzimuth(azimuth: Float) {
                adjustGambarDial(azimuth)
                adjustArrowQiblat(azimuth)
            }
        }
        compass.setListener(cl)
    }

    fun adjustGambarDial(azimuth: Float) {
        val an = RotateAnimation(-currentAzimuth, -azimuth,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f)
        currentAzimuth = azimuth
        an.duration = 500
        an.repeatCount = 0
        an.fillAfter = true
        imageDial.startAnimation(an)
    }

    fun adjustArrowQiblat(azimuth: Float) {
        val kiblat_derajat = GetFloat("kiblat_derajat")
        val an = RotateAnimation(-currentAzimuth + kiblat_derajat, -azimuth,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f)
        an.duration = 500
        an.repeatCount = 0
        an.fillAfter = true
        arrowViewQiblat.startAnimation(an)
        if (kiblat_derajat > 0) {
            arrowViewQiblat.visibility = View.VISIBLE
        } else {
            arrowViewQiblat.visibility = View.INVISIBLE
            arrowViewQiblat.visibility = View.GONE
        }
    }

    fun getBearing() {
        val kiblat_derajat = GetFloat("kiblat_derajat")
        if (kiblat_derajat > 0.0001f) {
            text_bawah.text = getString(R.string.your_location) + " " + getString(R.string.using_last_location)
            text_atas.text = getString(R.string.qibla_direction) + " $kiblat_derajat " + getString(R.string.degree_from_north)
            gpsMenuItem?.setIcon(ContextCompat.getDrawable(this, R.drawable.gps_on))
            arrowViewQiblat.visibility = View.VISIBLE
        } else {
            fetch_GPS_modern()
        }
    }

    @SuppressLint("MissingPermission")
    fun fetch_GPS_modern() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "مجوز دسترسی به مکان اعطا نشده است.", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener(this, OnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    text_bawah.text = getString(R.string.your_location) + "\nLat: $latitude Long: $longitude"

                    val kaabaLat = Math.toRadians(21.422487)
                    val kaabaLon = Math.toRadians(39.826206)
                    val userLat = Math.toRadians(latitude)
                    val userLon = Math.toRadians(longitude)

                    val lonDifference = kaabaLon - userLon
                    val y = Math.sin(lonDifference) * Math.cos(kaabaLat)
                    val x = Math.cos(userLat) * Math.sin(kaabaLat) - Math.sin(userLat) * Math.cos(kaabaLat) * Math.cos(lonDifference)
                    val result = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360

                    val result2 = result.toFloat()
                    SaveFloat("kiblat_derajat", result2)

                    text_atas.text = getString(R.string.qibla_direction) + " $result2 " + getString(R.string.degree_from_north)
                    Toast.makeText(applicationContext, getString(R.string.qibla_direction) + " $result2 " + getString(R.string.degree_from_north), Toast.LENGTH_LONG).show()
                    arrowViewQiblat.visibility = View.VISIBLE

                    gpsMenuItem?.setIcon(ContextCompat.getDrawable(applicationContext, R.drawable.gps_on))
                } else {
                    text_atas.text = getString(R.string.location_not_ready)
                    text_bawah.text = getString(R.string.location_not_ready)
                    gpsMenuItem?.setIcon(ContextCompat.getDrawable(applicationContext, R.drawable.gps_off))
                    Toast.makeText(applicationContext, "Location not ready.", Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<out String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SaveBoolean("permission_granted", true)
                text_atas.text = getString(R.string.msg_permission_granted)
                text_bawah.text = getString(R.string.msg_permission_granted)
                fetch_GPS_modern()
            } else {
                Toast.makeText(this, getString(R.string.toast_permission_required), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    fun SaveBoolean(Judul: String, bbb: Boolean) {
        val edit = prefs.edit()
        edit.putBoolean(Judul, bbb)
        edit.apply()
    }

    fun GetBoolean(Judul: String): Boolean {
        return prefs.getBoolean(Judul, false)
    }

    fun SaveFloat(Judul: String, bbb: Float) {
        val edit = prefs.edit()
        edit.putFloat(Judul, bbb)
        edit.apply()
    }

    fun GetFloat(Judul: String): Float {
        return prefs.getFloat(Judul, 0f)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.gps, menu)
        gpsMenuItem = menu.findItem(R.id.gps)
        return true
    }

    override fun onOptionsItemSelected(@NonNull item: MenuItem): Boolean {
        if (item.itemId == R.id.gps) {
            fetch_GPS_modern()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}