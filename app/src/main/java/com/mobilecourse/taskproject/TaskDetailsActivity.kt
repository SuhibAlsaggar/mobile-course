package com.mobilecourse.taskproject

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobilecourse.taskproject.databinding.ActivityTaskDetailsBinding
import com.mobilecourse.taskproject.firebaseservice.TasksAgent

class TaskDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val taskId = intent.getStringExtra("TASK_ID")!!

        TasksAgent.getTaskById(taskId) { task ->
            binding.taskTitle.text = task!!.title
            binding.taskDescription.text =
                task.subtasks.joinToString(", ") { subTask -> subTask.description }
            binding.taskAssignee.text = task.assigneeId
            binding.taskLocation.text =
                "Lat: ${task.latLng!!.latitude}, Lon: ${task.latLng.longitude}"
            
            binding.taskLocation.setOnClickListener {
                launchGoogleMaps(task.latLng.latitude, task.latLng.longitude)
            }

            binding.markCompleteButton.setOnClickListener {
                handleMarkTask(taskId, true)
            }
            binding.markIncompleteButton.setOnClickListener {
                handleMarkTask(taskId, false)
            }
        }
    }

    private fun launchGoogleMaps(lat: Double, lon: Double) {
        val uri = "http://maps.google.com/maps?daddr=$lat,$lon"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Google Maps app not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMarkTask(taskId: String, complete: Boolean = true) {
        val state = if (complete) "complete" else "incomplete"
        TasksAgent.markTask(taskId, complete) { status ->
            if (status)
                Toast.makeText(this, "Task marked as $state", Toast.LENGTH_LONG).show()
            else
                Toast.makeText(this, "Failed to mark as $state", Toast.LENGTH_LONG)
                    .show()
        }
    }
}
