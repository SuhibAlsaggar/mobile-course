package com.mobilecourse.taskproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

class HomePage : AppCompatActivity() {
    private lateinit var buttonNext: Button

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        val date = Date()
        val format = SimpleDateFormat("dd/MM/yyyy")
        val formattedDate = format.format(date)
        val dateTextView = findViewById<TextView>(R.id.xml_date_time_text_view)
        dateTextView.text = formattedDate

        buttonNext = findViewById(R.id.button_next)
        fetchUserRole()

        buttonNext.setOnClickListener {
            val intent = Intent(this, NextActivity::class.java) // Replace NextActivity with your actual activity
            startActivity(intent)
        }

        // Load the fragment
        replaceFragment(taskscroolfragment())
    }

    private fun fetchUserRole() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val role = document.getString("role")
                        if (role == "some_role") {
                            buttonNext.visibility = View.VISIBLE
                        } else {
                            buttonNext.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.framelayout, fragment)
        fragmentTransaction.commit()
    }
}