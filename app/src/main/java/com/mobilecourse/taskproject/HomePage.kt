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
import com.mobilecourse.taskproject.databinding.ActivityHomePageBinding
import com.mobilecourse.taskproject.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date

class HomePage : AppCompatActivity() {
    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val date = Date()
        val format = SimpleDateFormat("dd/MM/yyyy")
        val formattedDate = format.format(date)
        val dateTextView = findViewById<TextView>(R.id.xml_date_time_text_view)
        binding.xmlDateTimeTextView.text = formattedDate

//        binding.buttonNext.setOnClickListener {
//            val intent = Intent(this, NextActivity::class.java) // Replace NextActivity with your actual activity
//            startActivity(intent)
//        }

        // Load the fragment
        replaceFragment(taskscroolfragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.framelayout, fragment)
        fragmentTransaction.commit()
    }
}