package com.example.habittracker.ui.mood

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.habittracker.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class MoodStatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood_stats, container, false)

        val moodChart = view.findViewById<LineChart>(R.id.moodChart)

        // Load moods
        val prefs = requireContext().getSharedPreferences("mood_prefs", android.content.Context.MODE_PRIVATE)
        val json = prefs.getString("mood_list", null)
        val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
        val moodList: MutableList<MoodEntry> =
            if (json != null) Gson().fromJson(json, type) else mutableListOf()

        // Emoji -> score
        fun emojiToScore(emoji: String): Float = when (emoji) {
            "😢" -> 1f
            "😟" -> 2f
            "😐" -> 3f
            "😊" -> 4f
            "😍" -> 5f
            else -> 3f
        }

        // Last 7 days
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        val today = Calendar.getInstance()
        val last7Days = (6 downTo 0).map { i ->
            (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -i) }
        }

        // Day -> score (neutral 3 if none)
        val moodByDay = mutableMapOf<String, Float>()
        moodList.forEach { mood ->
            val key = sdf.format(Date(mood.timestamp.toLong()))
            moodByDay[key] = emojiToScore(mood.emoji)
        }

        val entries = last7Days.mapIndexed { idx, cal ->
            val key = sdf.format(cal.time)
            val score = moodByDay[key] ?: 3f
            Entry(idx.toFloat(), score)
        }

        // Dataset styling
        val dataSet = LineDataSet(entries, "Mood Trend").apply {
            color = Color.MAGENTA
            setCircleColor(Color.MAGENTA)
            circleRadius = 4.5f
            lineWidth = 2f
            valueTextColor = Color.BLACK
            setDrawValues(false)       // hide point labels for a cleaner look
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        }

        moodChart.data = LineData(dataSet)

        // X-axis as dates
        moodChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            labelRotationAngle = -45f
            setDrawGridLines(false)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val i = value.toInt()
                    return if (i in last7Days.indices) sdf.format(last7Days[i].time) else ""
                }
            }
        }

        // Y-axis as emojis (1..5)
        val emojiLabels = mapOf(
            1f to "😢",
            2f to "😟",
            3f to "😐",
            4f to "😊",
            5f to "😍"
        )
        moodChart.axisLeft.apply {
            axisMinimum = 1f
            axisMaximum = 5f
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String =
                    emojiLabels[value] ?: ""
            }
        }
        moodChart.axisRight.isEnabled = false

        moodChart.description.text = "Mood Scale: 1 (😢) → 5 (😍)"
        moodChart.legend.isEnabled = true
        moodChart.setPinchZoom(true)

        moodChart.invalidate()
        return view
    }
}
