package com.mobilecourse.taskproject

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
import com.mobilecourse.taskproject.locationservice.hasNotificationsPermission

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.requestPermissionsButton.setOnClickListener {
            requestPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            0
        )
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (hasLocationPermission() && hasNotificationsPermission()) {
            var intent = Intent(this, AuthActivity::class.java)
            auth = Firebase.auth
            val user = auth.currentUser
            if (user != null)
                intent = Intent(this, HomePageActivity::class.java)

            startActivity(intent)
            finish()
        } else {
            updatePermissionsText()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun updatePermissionsText() {
        binding.locationPermissionsText.text = if (!hasLocationPermission())
            "Location permissions are NOT granted."
        else "Location permissions are granted."

        binding.notificationPermissionsText.text = if (!hasNotificationsPermission())
            "Notification permissions are NOT granted." else "Notification permissions are granted."

        setContentView(binding.root)
    }
}