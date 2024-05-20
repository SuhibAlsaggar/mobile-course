package com.mobilecourse.taskproject

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import java.text.SimpleDateFormat

import java.util.Date


class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)
            val date  = Date()
            val format = SimpleDateFormat("dd/MM/yyyy")
            val formattedDate = format.format(date)
            val dateTextView = findViewById<TextView>(R.id.xml_date_time_text_view)
            dateTextView.text = formattedDate


        }
    }
