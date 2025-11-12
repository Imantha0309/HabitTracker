package com.example.habittracker
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import android.widget.Toast
import com.example.habittracker.databinding.ActivityMainBinding
import com.example.habittracker.ui.habits.HabitsFragment
import com.example.habittracker.ui.hydration.HydrationFragment
import com.example.habittracker.ui.mood.MoodFragment
import com.example.habittracker.ui.settings.SettingsFragment


class MainActivity : AppCompatActivity() {
    private lateinit var vb: ActivityMainBinding

    // Launcher for runtime notification permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notifications disabled, reminders won’t appear.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        // Ask for POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HabitsFragment())
                .commit()
        }

        // Navigation
        vb.bottomNav.setOnItemSelectedListener { item ->
            val frag = when (item.itemId) {
                R.id.nav_habits -> HabitsFragment()
                R.id.nav_mood -> MoodFragment()
                R.id.nav_hydration -> HydrationFragment()


                else -> SettingsFragment()
            }
            supportFragmentManager.commit {
                replace(R.id.fragment_container, frag)
            }
            true
        }
    }
}
