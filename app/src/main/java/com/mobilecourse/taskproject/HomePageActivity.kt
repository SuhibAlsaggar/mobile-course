package com.mobilecourse.taskproject

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobilecourse.taskproject.adapters.TaskAdapter
import com.mobilecourse.taskproject.databinding.ActivityHomePageBinding
import com.mobilecourse.taskproject.datamodels.Task
import com.mobilecourse.taskproject.firebaseservice.TasksAgent
import com.mobilecourse.taskproject.firebaseservice.UserAgent
import com.mobilecourse.taskproject.locationservice.LocationService
import com.mobilecourse.taskproject.locationservice.hasLocationPermission
import com.mobilecourse.taskproject.navigator.Navigator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class HomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding
    private var currentDate = Date()
    private val format = SimpleDateFormat("dd/MM/yyyy")

    private lateinit var taskList: MutableList<Task>
    private lateinit var adapter: TaskAdapter
    private lateinit var role: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasLocationPermission()) {
            UserAgent.getUserRole { _role ->
                role = _role
                updateUiForRole()
                loadTasksForDate(currentDate)
                if (role == "user") {
                    if (!isLocationServiceRunning()) {
                        Intent(applicationContext, LocationService::class.java).apply {
                            action = LocationService.ACTION_START
                            startService(this)
                        }
                    }
                }
            }
        } else {
            handleSignOut()
            return
        }

        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskList = mutableListOf()
        adapter = TaskAdapter(taskList, this)

        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTasks.adapter = adapter

        binding.newTask.setOnClickListener {
            Navigator.toTaskCreate(this)
        }
        binding.newTask.bringToFront()

        UserAgent.getCurrentUser { user ->
            binding.userNameText.text = user.name

        }
        binding.buttonPreviousDay.setOnClickListener {
            updateDate(-1)
        }

        binding.buttonNextDay.setOnClickListener {
            updateDate(1)
        }

        binding.buttonSignOut.setOnClickListener {
            handleSignOut()
        }

        updateDateTextView()
        Navigator.setNavigationBar(this)

    }

    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            loadTasksForDate(currentDate)
        }
    }

    private fun updateUiForRole() {
        if (role == "admin") {
            binding.newTask.visibility = View.VISIBLE
        } else {
            binding.newTask.visibility = View.GONE
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
        if (role == "admin") {
            TasksAgent.getTasksForDate(date, null) { tasks ->
                taskList.clear()
                taskList.addAll(tasks)
                adapter.notifyDataSetChanged()
            }
        } else {
            TasksAgent.getUserTasksForDate(date) { tasks ->
                taskList.clear()
                taskList.addAll(tasks)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun handleSignOut() {
        UserAgent.signOut { success, message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (success) {
                Intent(applicationContext, LocationService::class.java).apply {
                    action = LocationService.ACTION_STOP
                    startService(this)
                }

                Navigator.toAuth(this)
                finish()
            }
        }
    }

    private fun isLocationServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
