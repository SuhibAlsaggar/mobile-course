package com.mobilecourse.taskproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TaskDetailsActivity : AppCompatActivity() {

    private lateinit var taskTitle: TextView
    private lateinit var taskDescription: TextView
    private lateinit var taskAssignee: TextView
    private lateinit var taskLocation: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        taskTitle = findViewById(R.id.taskTitle)
        taskDescription = findViewById(R.id.taskDescription)
        taskAssignee = findViewById(R.id.taskAssignee)
        taskLocation = findViewById(R.id.taskLocation)

        val task = intent.getParcelableExtra<TaskCreateActivity.Task>("TASK")

        task?.let {
            taskTitle.text = it.title
            taskDescription.text = it.description
            taskAssignee.text = it.assignee
            taskLocation.text = "Lat: ${it.latitude}, Lon: ${it.longitude}"
        }
    }
}
