package com.mobilecourse.taskproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobilecourse.taskproject.databinding.ActivityHomePageBinding
import com.mobilecourse.taskproject.datamodels.Task
import com.mobilecourse.taskproject.firebaseservice.TasksAgent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class HomePage : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding
    private var currentDate = Date()
    private val format = SimpleDateFormat("dd/MM/yyyy")

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var taskList: MutableList<Task>
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskList = mutableListOf()
        adapter = TaskAdapter(taskList) { item, position ->
            // Handle item click here
            println("Clicked task at position $position: ${item.title}")
        }

        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTasks.adapter = adapter

        updateDateTextView()
        fetchUserRole()

        binding.newTask.setOnClickListener {
            val intent = Intent(this, testactivity::class.java)
            startActivity(intent)
        }

        binding.buttonPreviousDay.setOnClickListener {
            updateDate(-1)
        }

        binding.buttonNextDay.setOnClickListener {
            updateDate(1)
        }

        loadTasksForDate(currentDate)
    }

    private fun fetchUserRole() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val role = document.getString("role")
                        if (role == "admin") {
                            binding.newTask.visibility = View.VISIBLE
                        } else {
                            binding.newTask.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }

    private fun updateDate(offset: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_YEAR, offset)
        currentDate = calendar.time
        updateDateTextView()
        loadTasksForDate(currentDate)
    }

    private fun updateDateTextView() {
        val formattedDate = format.format(currentDate)
        binding.xmlDateTimeTextView.text = formattedDate
    }

    private fun loadTasksForDate(date: Date) {
        TasksAgent.getTasksForDate(date, null) { tasks ->
            taskList.clear()
            taskList.addAll(tasks)
            adapter.notifyDataSetChanged()
            println("Loaded tasks: $tasks")
        }
    }
}
