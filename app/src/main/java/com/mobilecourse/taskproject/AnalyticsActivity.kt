package com.mobilecourse.taskproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobilecourse.taskproject.adapters.AnalyticsAdapter
import com.mobilecourse.taskproject.adapters.TaskAdapter
import com.mobilecourse.taskproject.databinding.ActivityAnalyticsBinding
import com.mobilecourse.taskproject.databinding.ActivityHomePageBinding
import com.mobilecourse.taskproject.datamodels.Analytics
import com.mobilecourse.taskproject.datamodels.Task
import com.mobilecourse.taskproject.firebaseservice.AnalyticsAgent
import com.mobilecourse.taskproject.firebaseservice.TasksAgent
import com.mobilecourse.taskproject.firebaseservice.UserAgent
import com.mobilecourse.taskproject.locationservice.LocationService
import com.mobilecourse.taskproject.navigator.Navigator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyticsBinding
    private var currentDate = Date()
    private val format = SimpleDateFormat("dd/MM/yyyy")
    private var role: String = "user"

    private lateinit var taskList: MutableList<Analytics>
    private lateinit var adapter: AnalyticsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UserAgent.getUserRole { _role ->
            role = _role
            loadAnalyticsDates(currentDate)
        }

        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskList = mutableListOf()
        adapter = AnalyticsAdapter(taskList)

        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTasks.adapter = adapter

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

    private fun loadAnalyticsDates(date: Date) {
        if (role == "admin") {
            AnalyticsAgent.getAnalyticsForDate(date) { tasks ->
                taskList.clear()
                taskList.addAll(tasks)
                adapter.notifyDataSetChanged()
            }
        } else {
            AnalyticsAgent.getUserAnalyticsForDate(date) { tasks ->
                taskList.clear()
                taskList.add(tasks)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun updateDate(offset: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_YEAR, offset)
        currentDate = calendar.time
        updateDateTextView()
        loadAnalyticsDates(currentDate)
    }

    private fun updateDateTextView() {
        val formattedDate = format.format(currentDate)
        binding.xmlDateTimeTextView.text = formattedDate
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
}