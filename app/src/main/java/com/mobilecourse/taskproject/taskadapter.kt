package com.mobilecourse.taskproject

import android.location.Geocoder
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobilecourse.taskproject.databinding.RecyclerviewdesignBinding
import com.mobilecourse.taskproject.datamodels.Task
import java.io.IOException
import java.util.Locale

class TaskAdapter(
    private val data: MutableList<Task>,
    private val onItemClickListener: (Task, Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var geocoder: Geocoder? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    inner class TaskViewHolder(private val binding: RecyclerviewdesignBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task, position: Int) {
            binding.textName.text = task.title
            binding.textDate.text = task.date!!.toDate().toString()

            // Reverse geocode on a separate thread
            Thread {
                try {
                    val addresses = geocoder?.getFromLocation(task.latLng!!.latitude, task.latLng.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0].getAddressLine(0)
                        // Update UI on main thread
                        mainHandler.post {
                            binding.textAddress.text = address
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()

            val subTasks = task.subtasks

            binding.imageCheckbox.setImageResource(
                if (subTasks.all { it.completed == true }) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
            )

            binding.root.setOnClickListener {
                onItemClickListener(task, position)
                println("item clicked")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = RecyclerviewdesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        geocoder = Geocoder(parent.context, Locale.getDefault())
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(data[position], position)
    }

    override fun getItemCount(): Int = data.size
}
