package com.mobilecourse.taskproject.adapters

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobilecourse.taskproject.R
import com.mobilecourse.taskproject.databinding.RecyclerAnalyticsCardBinding
import com.mobilecourse.taskproject.databinding.RecyclerHomeTaskCardBinding
import com.mobilecourse.taskproject.datamodels.Analytics
import com.mobilecourse.taskproject.datamodels.Task
import com.mobilecourse.taskproject.navigator.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class AnalyticsAdapter (
    private val data: MutableList<Analytics>
) : RecyclerView.Adapter<AnalyticsAdapter.AnalyticsViewHolder>() {

    inner class AnalyticsViewHolder(private val binding: RecyclerAnalyticsCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(analytics: Analytics) {
            binding .textName.text = analytics.user
            binding .totalTasks.text = "${analytics.completedTasks} / ${analytics.totalTasks}"
            binding .totalSubTasks.text = "${analytics.completedSubTasks} / ${analytics.totalSubTasks}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyticsViewHolder {
        val binding = RecyclerAnalyticsCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnalyticsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnalyticsViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}