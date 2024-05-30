package com.mobilecourse.taskproject.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobilecourse.taskproject.databinding.RecyclerAnalyticsCardBinding
import com.mobilecourse.taskproject.datamodels.Analytics

class AnalyticsAdapter(
    private val data: MutableList<Analytics>
) : RecyclerView.Adapter<AnalyticsAdapter.AnalyticsViewHolder>() {

    inner class AnalyticsViewHolder(private val binding: RecyclerAnalyticsCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(analytics: Analytics) {
            binding.textName.text = analytics.user
            binding.totalTasks.text =
                "Tasks completed: ${analytics.completedTasks} out of ${analytics.totalTasks}"
            binding.totalSubTasks.text =
                "Subtasks completed: ${analytics.completedSubTasks} out of ${analytics.totalSubTasks}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyticsViewHolder {
        val binding =
            RecyclerAnalyticsCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnalyticsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnalyticsViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}