package com.mobilecourse.taskproject

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mobilecourse.taskproject.databinding.ActivityHomeBinding
import com.mobilecourse.taskproject.datamodels.Task
import com.mobilecourse.taskproject.firebaseservice.TasksAgent.Companion.getTaskById

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val email = auth.currentUser!!.email
        binding.WelcomeText.text = "Welcome: $email"

        var task: Task
        val taskId = intent.getStringExtra("TASK_ID")!!
        getTaskById(taskId) { retrievedTask ->
            task = retrievedTask!!
            task.let {
                binding.titleText.text = it.title
                binding.assigneeText.text = it.assigneeId
                binding.dateText.text = it.subtasks.toString()
                binding.latLngText.text = it.latLng.toString()
            }
        }
    }
}