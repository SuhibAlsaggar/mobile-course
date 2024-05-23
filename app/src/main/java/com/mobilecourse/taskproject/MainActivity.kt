package com.mobilecourse.taskproject

import com.mobilecourse.taskproject.locationservice.LocationService
import com.mobilecourse.taskproject.locationservice.hasLocationPermission
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mobilecourse.taskproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            0
        )

        binding = ActivityMainBinding.inflate(layoutInflater)

        if (hasLocationPermission()) {
            Intent(applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                startService(this)
                binding.Text.text = "Location permissions granted"
            }
        } else {
            binding.Text.text = "Location permissions NOT granted"
        }

        setContentView(binding.root)
        val intent1 = Intent(this, TaskCreateActivity::class.java)
        startActivity(intent1)

        var intent = Intent(this, signup_login_page::class.java)
       auth = Firebase.auth
       val user = auth.currentUser
       if (user != null)
            intent = Intent(this, HomePage::class.java)

        startActivity(intent)
        finish()
    }

}