package com.mobilecourse.taskproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Date


class TaskCreateActivity : AppCompatActivity() {

    private lateinit var taskTitle: EditText
    private lateinit var taskDescription: EditText
    private lateinit var taskAssignee: EditText
    private lateinit var createTaskButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_create)

        taskTitle = findViewById(R.id.taskTitle)
        taskDescription = findViewById(R.id.taskDescription)
        taskAssignee = findViewById(R.id.taskAssignee)
        createTaskButton = findViewById(R.id.createTaskButton)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        databaseReference = FirebaseDatabase.getInstance().getReference("tasks")

        createTaskButton.setOnClickListener { createTask() }
    }

    private fun createTask() {
        val title = taskTitle.text.toString()
        val description = taskDescription.text.toString()
        val assignee = taskAssignee.text.toString()
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
                val task = Task(title, description, date, assignee, it.latitude, it.longitude)


                Log.d("TaskCreateActivity", "Task created successfully: $task")
                val intent = Intent(this, TaskDetailsActivity::class.java).apply {
                    putExtra("TASK", task)
                }
                startActivity(intent)
                finish()
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

    data class Task(
        val title: String,
        val description: String,
        val date: Date,
        val assignee: String,
        val latitude: Double,
        val longitude: Double
    ) : Parcelable {
        constructor() : this("", "", Date(), "", 0.0, 0.0)

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<Task> {
                override fun createFromParcel(parcel: Parcel) = Task(parcel)
                override fun newArray(size: Int) = arrayOfNulls<Task>(size)
            }
        }

        private constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            Date(parcel.readLong()),
            parcel.readString() ?: "",
            parcel.readDouble(),
            parcel.readDouble()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(title)
            parcel.writeString(description)
            parcel.writeLong(date.time)
            parcel.writeString(assignee)
            parcel.writeDouble(latitude)
            parcel.writeDouble(longitude)
        }

        override fun describeContents() = 0
    }

}
