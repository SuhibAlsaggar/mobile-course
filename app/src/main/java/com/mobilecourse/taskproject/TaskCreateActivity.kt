package com.mobilecourse.taskproject

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.mobilecourse.taskproject.datamodels.SubTask
import com.mobilecourse.taskproject.databinding.ActivityTaskCreateBinding
import com.mobilecourse.taskproject.datamodels.User
import com.mobilecourse.taskproject.firebaseservice.TasksAgent
import com.mobilecourse.taskproject.firebaseservice.UserAgent

class TaskCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskCreateBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var subtaskContainer: LinearLayout
    private lateinit var assigneeSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        subtaskContainer = findViewById(R.id.subtaskContainer)
        assigneeSpinner = findViewById(R.id.assigneeSpinner)

        binding.addSubtaskButton.setOnClickListener { addSubtaskField() }
        binding.createTaskButton.setOnClickListener { createTask() }

        fetchAssignees()
    }

    private fun fetchAssignees() {
        UserAgent.getUsers { users ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, users!!.toList())
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            assigneeSpinner.adapter = adapter
        }
    }

    private fun addSubtaskField() {
        if (subtaskContainer.childCount > 5) {
            return
        }

        val subtaskLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val subtaskField = EditText(this).apply {
            hint = "Subtask"
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val removeButton = Button(this).apply {
            text = "Remove"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                subtaskContainer.removeView(subtaskLayout)
            }
        }

        subtaskLayout.addView(subtaskField)
        subtaskLayout.addView(removeButton)
        subtaskContainer.addView(subtaskLayout)
    }

    @SuppressLint("MissingPermission")
    private fun createTask() {
        val title = binding.taskTitle.text.toString()
        val user = assigneeSpinner.selectedItem as User
        val assignee = user.id

        val subtasks = mutableListOf<SubTask>()
        for (i in 0 until subtaskContainer.childCount) {
            val subtaskLayout = subtaskContainer.getChildAt(i)
            if (subtaskLayout is LinearLayout) {
                for (j in 0 until subtaskLayout.childCount) {
                    val subtaskField = subtaskLayout.getChildAt(j)
                    if (subtaskField is EditText) {
                        val subtaskDescription = subtaskField.text.toString()
                        if (subtaskDescription.isNotEmpty()) {
                            subtasks.add(
                                SubTask(
                                    description = subtaskDescription,
                                    completed = false
                                )
                            )
                        }
                    }
                }
            }
        }

        if (title.isEmpty() || assignee.isEmpty() || subtasks.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                TasksAgent.createTask(
                    title = title,
                    latLng = GeoPoint(it.latitude, it.longitude),
                    assigneeId = assignee,
                    subtasks = subtasks.toList()
                ) { success ->
                    if (success) {
                        Log.d("TaskCreateActivity", "Task created successfully")
                        Toast.makeText(this, "Task created successfully", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity
                    } else {
                        Log.e("TaskCreateActivity", "Error creating task")
                        Toast.makeText(this, "Failed to create task", Toast.LENGTH_LONG).show()
                    }
                }
            } ?: run {
                Log.e("TaskCreateActivity", "Location is null")
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("TaskCreateActivity", "Failed to get location", exception)
            Toast.makeText(this, "Failed to get location: ${exception.message}", Toast.LENGTH_LONG)
                .show()
        }
    }
}
