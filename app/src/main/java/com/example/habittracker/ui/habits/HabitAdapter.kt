package com.example.habittracker.ui.habits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.ItemHabitBinding

class HabitAdapter(
    private var habits: MutableList<Habit>,
    private val onUpdate: (List<Habit>) -> Unit,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(val binding: ItemHabitBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.binding.cbHabit.apply {
            text = habit.name
            isChecked = habit.completed

            // ✅ Always keep text black (no fading)
            setTextColor(ContextCompat.getColor(context, android.R.color.black))

            setOnCheckedChangeListener { _, isChecked ->
                habit.completed = isChecked
                // Keep text black always
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                onUpdate(habits)
            }
        }

        // Hook edit and delete buttons if you have them in item layout
        holder.binding.btnEdit.setOnClickListener { onEdit(position) }
        holder.binding.btnDelete.setOnClickListener { onDelete(position) }
    }

    override fun getItemCount(): Int = habits.size

    fun updateData(newHabits: MutableList<Habit>) {
        habits = newHabits
        notifyDataSetChanged()
    }
}
