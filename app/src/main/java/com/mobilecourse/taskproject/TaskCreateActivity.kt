package com.mobilecourse.taskproject

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.mobilecourse.taskproject.datamodels.SubTask
import com.mobilecourse.taskproject.datamodels.Task
import com.mobilecourse.taskproject.databinding.ActivityTaskCreateBinding
import com.mobilecourse.taskproject.firebaseservice.TasksAgent
import java.util.Date

class TaskCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskCreateBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var subtaskContainer: LinearLayout
    private lateinit var assigneeSpinner: Spinner
    private lateinit var assigneesList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        subtaskContainer = findViewById(R.id.subtaskContainer)
        assigneeSpinner = findViewById(R.id.assigneeSpinner)

        binding.addSubtaskButton.setOnClickListener { addSubtaskField() }
        binding.createTaskButton.setOnClickListener { createTask() }

        // Fetch assignees from the database and populate the Spinner
        fetchAssignees()
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


    private fun fetchAssignees() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").get().addOnSuccessListener { documents ->
            assigneesList = documents.map { it.getString("name") ?: "" }
            println("test ${documents.documents}")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, assigneesList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            assigneeSpinner.adapter = adapter
        }.addOnFailureListener { exception ->
            Log.e("TaskCreateActivity", "Error fetching assignees", exception)
            Toast.makeText(this, "Failed to fetch assignees", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createTask() {
        val title = binding.taskTitle.text.toString()
        val description = binding.taskDescription.text.toString()
        val assignee = assigneeSpinner.selectedItem.toString()
        val date = Date()

        if (title.isEmpty() || description.isEmpty() || assignee.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latLng = GeoPoint(it.latitude, it.longitude)

                // Collect subtasks
                val subtasks = mutableListOf<SubTask>()
                for (i in 0 until subtaskContainer.childCount) {
                    val subtaskField = subtaskContainer.getChildAt(i) as EditText
                    val subtaskDescription = subtaskField.text.toString()
                    if (subtaskDescription.isNotEmpty()) {
                        subtasks.add(SubTask(description = subtaskDescription, completed = false))
                    }
                }

                // Use TasksAgent to create the task and upload to Firestore
                TasksAgent.createTask(
                    title = title,
                    latLng = latLng,
                    assigneeId = assignee,
                    subtasks = subtasks
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createTask()
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
