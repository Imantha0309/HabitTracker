package com.example.habittracker.ui.mood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import java.text.SimpleDateFormat
import java.util.*

class MoodAdapter(private val moods: List<MoodEntry>) :
    RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    // Keep track of expanded items
    private val expandedItems = mutableSetOf<Int>()

    class MoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvEmoji)
        val tvNote: TextView = view.findViewById(R.id.tvNote)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val entry = moods[position]
        holder.tvEmoji.text = entry.emoji
        holder.tvNote.text = entry.note

        // ✅ Format timestamp from Long → readable date
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.tvTime.text = sdf.format(Date(entry.timestamp))

        // Show note if expanded, otherwise hide
        holder.tvNote.visibility =
            if (expandedItems.contains(position)) View.VISIBLE else View.GONE

        // Toggle expand/collapse when clicked
        holder.itemView.setOnClickListener {
            if (expandedItems.contains(position)) {
                expandedItems.remove(position)
            } else {
                expandedItems.add(position)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = moods.size
}
