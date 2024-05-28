package com.mobilecourse.taskproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobilecourse.taskproject.databinding.ActivityTaskDetailsBinding
import com.mobilecourse.taskproject.datamodels.Task
import com.mobilecourse.taskproject.firebaseservice.TasksAgent

class TaskDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val taskId = intent.getStringExtra("TASK_ID")!!

        TasksAgent.getTaskById(taskId){
            binding.taskTitle.text = it!!.title
            binding.taskDescription.text = it.subtasks.joinToString(", ") { subTask -> subTask.description }

            binding.taskAssignee.text = it.assigneeId
            binding.taskLocation.text = "Lat: ${it.latLng?.latitude}, Lon: ${it.latLng?.longitude}"
        }
    }
}
