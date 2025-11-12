package com.example.habittracker.ui.mood

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class MoodFragment : Fragment() {

    private lateinit var adapter: MoodAdapter
    private var moodList = mutableListOf<MoodEntry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        val spinnerEmoji = view.findViewById<Spinner>(R.id.spinnerEmoji)
        val etNote = view.findViewById<EditText>(R.id.etNote)
        val btnSaveMood = view.findViewById<Button>(R.id.btnSaveMood)
        val recyclerMoods = view.findViewById<RecyclerView>(R.id.recyclerMoods)
        val btnDeleteAllMoods = view.findViewById<Button>(R.id.btnDeleteAllMoods)
        val btnViewStats = view.findViewById<Button>(R.id.btnViewStats)

        // Emoji options
        val emojis = arrayOf("😊", "😢", "😡", "😴", "🤩", "😟")
        val emojiAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, emojis)
        emojiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEmoji.adapter = emojiAdapter

        // Load saved moods
        val prefs = requireContext().getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("mood_list", null)
        if (json != null) {
            // Try reading the current schema (timestamp: Long)
            val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
            try {
                moodList = Gson().fromJson(json, type)
            } catch (_: Exception) {
                // If you previously saved timestamp as String, migrate it
                data class OldMoodEntry(val emoji: String, val note: String, val timestamp: String)
                val oldType = object : TypeToken<MutableList<OldMoodEntry>>() {}.type
                val old = Gson().fromJson<MutableList<OldMoodEntry>>(json, oldType) ?: mutableListOf()
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                moodList = old.map {
                    val ts = it.timestamp.toLongOrNull()
                        ?: runCatching { sdf.parse(it.timestamp)?.time ?: System.currentTimeMillis() }.getOrElse {
                            System.currentTimeMillis()
                        }
                    MoodEntry(it.emoji, it.note, ts)
                }.toMutableList()
                // Persist migrated data
                prefs.edit().putString("mood_list", Gson().toJson(moodList)).apply()
            }
        }

        // Setup RecyclerView
        adapter = MoodAdapter(moodList)
        recyclerMoods.layoutManager = LinearLayoutManager(requireContext())
        recyclerMoods.adapter = adapter

        // Save button (one mood per day)
        btnSaveMood.setOnClickListener {
            val selectedEmoji = spinnerEmoji.selectedItem.toString()
            val note = etNote.text.toString().trim()

            // Check if there's already a mood for "today"
            val today = LocalDate.now()
            val alreadySavedToday = moodList.any { entry ->
                val entryDay = Instant.ofEpochMilli(entry.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                entryDay == today
            }

            if (alreadySavedToday) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Already Saved")
                    .setMessage("You’ve already saved a mood for today.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            // Save new mood (timestamp now)
            val entry = MoodEntry(
                emoji = selectedEmoji,
                note = note,
                timestamp = System.currentTimeMillis()
            )

            moodList.add(0, entry) // newest first
            prefs.edit().putString("mood_list", Gson().toJson(moodList)).apply()

            adapter.notifyItemInserted(0)
            recyclerMoods.scrollToPosition(0)

            etNote.text.clear()
            Toast.makeText(requireContext(), "Mood saved!", Toast.LENGTH_SHORT).show()
        }

        // View Stats Button → navigate to MoodStatsFragment
        btnViewStats.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MoodStatsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Delete all moods
        btnDeleteAllMoods.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete All Moods")
                .setMessage("Are you sure you want to delete all saved moods?")
                .setPositiveButton("Yes") { _, _ ->
                    prefs.edit().clear().apply()
                    moodList.clear()
                    adapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "All moods deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No", null)
                .show()
        }

        return view
    }
}
