package com.mobilecourse.taskproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.Marker
import com.mobilecourse.taskproject.datamodels.Task
import com.mobilecourse.taskproject.firebaseservice.TasksAgent
import java.util.Calendar

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private val markerTaskMap = mutableMapOf<Marker, Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnInfoWindowClickListener(this)

        val calendar = Calendar.getInstance()

        TasksAgent.getUserTasksForDate(calendar.time) { tasks ->
            tasks.forEach{ task ->
                val latLng = LatLng(task.latLng!!.latitude, task.latLng.longitude)
                val title = task.title
                val marker = googleMap.addMarker(MarkerOptions().position(latLng).title(title))
                markerTaskMap[marker!!] = task
            }
        }
        val mapCenter = LatLng(31.951275, 35.915839)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 11f))
    }

    override fun onInfoWindowClick(marker: Marker) {
        val task = markerTaskMap[marker]
        val taskIntent = Intent(this, TaskDetailsActivity::class.java)
        taskIntent.putExtra("TASK_ID", task!!.id)
        startActivity(taskIntent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


}