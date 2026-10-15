package com.androidhive.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.androidhive.myapplication.compass.CompassActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private lateinit var btn_namaz: CardView
    private lateinit var btn_vuzu: CardView
    private lateinit var btn_ghosl: CardView
    private lateinit var btn_tasbihat: CardView
    private lateinit var btn_tayammom: CardView
    private lateinit var btn_sejdeyesahv: CardView
    private lateinit var btn_compass: CardView
    private lateinit var btn_adhan_times: CardView
    private lateinit var prayer_times_card: View

    private lateinit var textNextPrayer: TextView
    private lateinit var textTimeRemaining: TextView
    private lateinit var textLocation: TextView

    private lateinit var viewModel: MainViewModel

    private var mBackPressed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this, MainViewModelFactory(AppPrefs)).get(MainViewModel::class.java)

        // Toolbar + Drawer
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // Buttons
        btn_namaz = findViewById(R.id.btn_namaz)
        btn_vuzu = findViewById(R.id.btn_vuzu)
        btn_ghosl = findViewById(R.id.btn_ghosl)
        btn_tasbihat = findViewById(R.id.btn_tasbihat)
        btn_tayammom = findViewById(R.id.btn_tayammom)
        btn_sejdeyesahv = findViewById(R.id.btn_sejdeyesahv)
        btn_compass = findViewById(R.id.btn_compass)
        btn_adhan_times = findViewById(R.id.btn_adhan_times)
        prayer_times_card = findViewById(R.id.prayer_times_card)

        btn_namaz.setOnClickListener(this)
        btn_vuzu.setOnClickListener(this)
        btn_ghosl.setOnClickListener(this)
        btn_tasbihat.setOnClickListener(this)
        btn_tayammom.setOnClickListener(this)
        btn_sejdeyesahv.setOnClickListener(this)
        btn_compass.setOnClickListener(this)
        btn_adhan_times.setOnClickListener(this)
        prayer_times_card.setOnClickListener(this)

        // Prayer UI on home
        textNextPrayer = findViewById(R.id.text_next_prayer)
        textTimeRemaining = findViewById(R.id.text_time_remaining)
        textLocation = findViewById(R.id.text_location)

        // Observe ViewModel state with repeatOnLifecycle
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    textNextPrayer.text = "نماز بعدی: ${state.nextPrayerName} - ${state.nextPrayerTime}"
                    textTimeRemaining.text = state.remainingTime
                    textLocation.text = state.currentLocationLabel
                }
            }
        }

        handleOnBackPressed()
    }

    override fun onStart() {
        super.onStart()
        viewModel.setOnCountdownFinished {
            viewModel.loadFromPrefs(this@MainActivity)
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.clearOnCountdownFinished()
    }

    override fun onResume() {
        super.onResume()

        // اگر auto روشنه، همزمان schedule هم آپدیت بشه
        // (PrayerScheduler خودش چک می‌کنه auto روشن هست یا نه)
        PrayerScheduler.computeAndScheduleNext(this)

        viewModel.loadFromPrefs(this)
    }

    override fun onPause() {
        super.onPause()
        // Countdown is automatically cancelled in ViewModel.onCleared()
        // or when Activity is destroyed. No manual stop needed here.
    }

    private fun handleOnBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                    return
                }

                if (mBackPressed + 2000 > System.currentTimeMillis()) {
                    finish()
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "برای خروج دوباره بازگشت را بزنید",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                mBackPressed = System.currentTimeMillis()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onClick(v: View) {
        val id = v.id

        when (id) {
            R.id.btn_namaz -> startActivity(Intent(this, PrayerActivity::class.java))
            R.id.btn_vuzu -> startActivity(Intent(this, com.androidhive.myapplication.source_activities.VuzuActivity::class.java))
            R.id.btn_ghosl -> startActivity(Intent(this, com.androidhive.myapplication.source_activities.GhoslActivity::class.java))
            R.id.btn_tasbihat -> startActivity(Intent(this, com.androidhive.myapplication.source_activities.TasbihatActivity::class.java))
            R.id.btn_tayammom -> startActivity(Intent(this, com.androidhive.myapplication.source_activities.TayammomActivity::class.java))
            R.id.btn_sejdeyesahv -> startActivity(Intent(this, com.androidhive.myapplication.source_activities.SejdeyyesahvActivity::class.java))
            R.id.btn_compass -> startActivity(Intent(this, CompassActivity::class.java))
            R.id.prayer_times_card, R.id.btn_adhan_times ->
                startActivity(Intent(this, PrayerTimesActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.nav_comment) {
            // نظر دادن (باز کردن صفحه گوگل پلی)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("bazaar://details?id=" + "com.androidhive.myapplication")
            startActivity(intent)
        } else if (id == R.id.nav_sources) {
            // منابع
            startActivity(Intent(this, com.androidhive.myapplication.source_activities.SourceActivity::class.java))
        } else if (id == R.id.nav_share) {
            // اشتراک گذاری
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "برنامه اوقات شرعی")
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "برنامه نماز فقه حنفی\n" +
                    "دانلود:\n" +
                    "https://cafebazaar.ir/app/com.androidhive.myapplication" + packageName
            )
            startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری با"))
        } else if (id == R.id.nav_about) {
            startActivity(Intent(this, com.androidhive.myapplication.source_activities.AboutActivity::class.java))
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}