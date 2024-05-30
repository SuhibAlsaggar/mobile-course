package com.mobilecourse.taskproject.adapters

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobilecourse.taskproject.R
import com.mobilecourse.taskproject.databinding.RecyclerHomeTaskCardBinding
import com.mobilecourse.taskproject.datamodels.Task
import com.mobilecourse.taskproject.navigator.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class TaskAdapter(
    private val data: MutableList<Task>, private val context: Context
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var geocoder: Geocoder? = null

    inner class TaskViewHolder(private val binding: RecyclerHomeTaskCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.textName.text = task.title
            binding.textDate.text = task.date!!.toDate().toString()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val addresses =
                        geocoder?.getFromLocation(task.latLng!!.latitude, task.latLng.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0].getAddressLine(0)
                        withContext(Dispatchers.Main) {
                            binding.textAddress.text = address
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            val subTasks = task.subtasks
            binding.imageCheckbox.setImageResource(
                if (subTasks.all { it.completed == true }) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
            )

            binding.linearLayoutCardview.setOnClickListener {
                Navigator.toTaskDetails(context, task.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding =
            RecyclerHomeTaskCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        geocoder = Geocoder(parent.context, Locale.getDefault())
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}
