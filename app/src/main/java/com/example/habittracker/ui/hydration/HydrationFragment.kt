package com.example.habittracker.ui.hydration

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.habittracker.R

class HydrationFragment : Fragment() {

    private var goal = 2500  // default
    private var consumed = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_hydration, container, false)

        val tvCurrent = view.findViewById<TextView>(R.id.tvCurrentHydration)
        val tvRemaining = view.findViewById<TextView>(R.id.tvRemaining)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarHydration)

        val etGoal = view.findViewById<EditText>(R.id.etGoal)
        val btnSetGoal = view.findViewById<Button>(R.id.btnSetGoal)

        val btnAdd180 = view.findViewById<Button>(R.id.btnAdd180)
        val btnAdd250 = view.findViewById<Button>(R.id.btnAdd250)
        val btnAdd500 = view.findViewById<Button>(R.id.btnAdd500)

        val etManualAmount = view.findViewById<EditText>(R.id.etManualAmount)
        val btnAddManual = view.findViewById<Button>(R.id.btnAddManual)

        val spinnerInterval = view.findViewById<Spinner>(R.id.spinnerInterval)
        val btnStartReminder = view.findViewById<Button>(R.id.btnStartReminder)
        val btnStopReminder = view.findViewById<Button>(R.id.btnStopReminder)

        // 🔹 Disable Stop button initially
        btnStopReminder.isEnabled = false

        // SharedPreferences
        val prefs = requireContext().getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        goal = prefs.getInt("goal", 2500)
        consumed = prefs.getInt("consumed", 0)

        fun updateUI() {
            tvCurrent.text = "$consumed ml"
            val remaining = (goal - consumed).coerceAtLeast(0)
            tvRemaining.text = "Remaining: $remaining ml"
            progressBar.max = goal
            progressBar.progress = consumed.coerceAtMost(goal)
        }

        // Set goal
        btnSetGoal.setOnClickListener {
            val input = etGoal.text.toString()
            if (input.isNotEmpty()) {
                goal = input.toInt()
                consumed = 0
                prefs.edit().putInt("goal", goal).putInt("consumed", consumed).apply()
                updateUI()
            }
        }

        // Quick add
        btnAdd180.setOnClickListener { consumed += 180; prefs.edit().putInt("consumed", consumed).apply(); updateUI() }
        btnAdd250.setOnClickListener { consumed += 250; prefs.edit().putInt("consumed", consumed).apply(); updateUI() }
        btnAdd500.setOnClickListener { consumed += 500; prefs.edit().putInt("consumed", consumed).apply(); updateUI() }

        // Manual add
        btnAddManual.setOnClickListener {
            val input = etManualAmount.text.toString()
            if (input.isNotEmpty()) {
                consumed += input.toInt()
                prefs.edit().putInt("consumed", consumed).apply()
                updateUI()
                etManualAmount.text.clear()
            }
        }

        // Reminder Intervals
        val intervals = arrayOf("10 sec", "1 min", "30 min", "1 hour", "2 hours", "3 hours")
        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervals)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerInterval.adapter = adapterSpinner

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminderIntent = Intent(requireContext(), HydrationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Start reminder
        btnStartReminder.setOnClickListener {
            val interval = when (spinnerInterval.selectedItem.toString()) {
                "10 sec" -> 10 * 1000L
                "1 min" -> 60 * 1000L
                "30 min" -> 30 * 60 * 1000L
                "1 hour" -> 60 * 60 * 1000L
                "2 hours" -> 2 * 60 * 60 * 1000L
                "3 hours" -> 3 * 60 * 60 * 1000L
                else -> 60 * 60 * 1000L
            }

            // Check exact alarm permission for Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${requireContext().packageName}")
                    }
                    startActivity(intent)
                    Toast.makeText(requireContext(), "Enable Exact Alarms in settings", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            // Pass interval to receiver
            reminderIntent.putExtra("interval", interval)

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + interval,
                pendingIntent
            )

            Toast.makeText(requireContext(), "Reminder set every ${spinnerInterval.selectedItem}", Toast.LENGTH_SHORT).show()

            // Enable Stop button
            btnStopReminder.isEnabled = true
        }

        // Stop reminder
        btnStopReminder.setOnClickListener {
            alarmManager.cancel(pendingIntent)
            Toast.makeText(requireContext(), "Reminders stopped", Toast.LENGTH_SHORT).show()

            // Disable stop button again
            btnStopReminder.isEnabled = false
        }

        updateUI()
        return view
    }
}
