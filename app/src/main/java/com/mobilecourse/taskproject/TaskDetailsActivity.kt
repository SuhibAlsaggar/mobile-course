package com.mobilecourse.taskproject

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobilecourse.taskproject.databinding.ActivityTaskDetailsBinding
import com.mobilecourse.taskproject.firebaseservice.TasksAgent

class TaskDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val taskId = intent.getStringExtra("TASK_ID")!!

        TasksAgent.getTaskById(taskId) {
            binding.taskTitle.text = it!!.title
            binding.taskDescription.text =
                it.subtasks.joinToString(", ") { subTask -> subTask.description }
            binding.taskAssignee.text = it.assigneeId
            binding.taskLocation.text = "Lat: ${it.latLng?.latitude}, Lon: ${it.latLng?.longitude}"

            binding.markCompleteButton.setOnClickListener {
                handleMarkTask(taskId, true)
            }
            binding.markIncompleteButton.setOnClickListener {
                handleMarkTask(taskId, false)
            }
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
