package com.mobilecourse.taskproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var intent = Intent(this, AuthActivity::class.java)
        auth = Firebase.auth
        val user = auth.currentUser

        if (user != null)
            intent = Intent(this, HomeActivity::class.java)

        startActivity(intent)
        finish()
    }
}