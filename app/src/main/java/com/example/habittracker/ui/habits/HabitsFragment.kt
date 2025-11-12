package com.example.habittracker.ui.habits

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.databinding.FragmentHabitsBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.CalendarDay
import org.threeten.bp.DayOfWeek


class HabitsFragment : Fragment() {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HabitAdapter
    private var habits = mutableListOf<Habit>()
    private var selectedDate: String = today()

    private fun prefs() =
        requireContext().getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)

    /** Has the “all done” dialog already been shown for this date? */
    private fun hasShownCongrats(date: String): Boolean =
        prefs().getBoolean("congrats_shown_$date", false)

    /** Mark the “all done” dialog as shown for this date */
    private fun setShownCongrats(date: String) {
        prefs().edit().putBoolean("congrats_shown_$date", true).apply()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)

        // Load habits for today's date
        habits = loadFromPreferences(requireContext(), selectedDate)

        setupRecyclerView()
        setupFab()
        setupCalendar()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = HabitAdapter(
            habits,
            onUpdate = { updatedList ->
                saveToPreferences(requireContext(), selectedDate, updatedList)
                updateProgress(updatedList)
            },
            onEdit = { position ->
                showEditDialog(position)
            },
            onDelete = { position ->
                habits.removeAt(position)
                adapter.notifyItemRemoved(position)
                saveToPreferences(requireContext(), selectedDate, habits)
                updateProgress(habits)
            }
        )
        binding.recyclerHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHabits.adapter = adapter

        updateProgress(habits)
    }

    private fun setupFab() {
        binding.btnAddHabit.setOnClickListener {
            val editText = EditText(requireContext())
            AlertDialog.Builder(requireContext())
                .setTitle("Add Habit")
                .setView(editText)
                .setPositiveButton("Add") { _, _ ->
                    val habitName = editText.text.toString()
                    if (habitName.isNotEmpty()) {
                        val newHabit = Habit(habitName)
                        habits.add(newHabit)
                        adapter.notifyItemInserted(habits.size - 1)
                        saveToPreferences(requireContext(), selectedDate, habits)
                        updateProgress(habits)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupCalendar() {
        binding.calendarView.state().edit()
            .setFirstDayOfWeek(org.threeten.bp.DayOfWeek.MONDAY) // using ThreeTenBP
            .setMinimumDate(CalendarDay.from(2020, 1, 1))
            .setMaximumDate(CalendarDay.from(2030, 12, 31))
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()

        // Force calendar to always show 6 weeks
        binding.calendarView.setDynamicHeightEnabled(false) // 🔑 disables shrinking
        binding.calendarView.setTopbarVisible(true)         // keep month navigation
        binding.calendarView.invalidateDecorators()

        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            selectedDate = String.format(
                "%04d-%02d-%02d",
                date.year,
                date.month,  // already 1-based in CalendarDay
                date.day
            )
            habits = loadFromPreferences(requireContext(), selectedDate)
            adapter.updateData(habits)
            updateProgress(habits)
        }
    }



    private fun updateProgress(habits: List<Habit>) {
        val completed = habits.count { it.completed }
        val total = habits.size
        val percentage = if (total > 0) (completed * 100) / total else 0

        binding.progressText.text = "Progress: $completed / $total ($percentage%)"

        ObjectAnimator.ofInt(binding.progressBar, "progress", binding.progressBar.progress, percentage)
            .setDuration(600)
            .start()

        // 🎉 Show once per day when everything is completed
        if (total > 0 && completed == total && !hasShownCongrats(selectedDate)) {
            AlertDialog.Builder(requireContext())
                .setTitle("🎉 Well done!")
                .setMessage("You’ve completed all your habits for $selectedDate 👏")
                .setPositiveButton("OK") { d, _ -> d.dismiss() }
                .show()

            setShownCongrats(selectedDate)
        }
    }



    private fun saveToPreferences(context: Context, date: String, habits: List<Habit>) {
        val prefs = context.getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(habits)
        editor.putString(date, json)
        editor.apply()
    }

    private fun loadFromPreferences(context: Context, date: String): MutableList<Habit> {
        val prefs = context.getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(date, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Habit>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    private fun showEditDialog(position: Int) {
        val habit = habits[position]
        val editText = EditText(requireContext()).apply {
            setText(habit.name)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotEmpty()) {
                    habit.name = newName
                    adapter.notifyItemChanged(position)
                    saveToPreferences(requireContext(), selectedDate, habits)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}
