package com.mobilecourse.taskproject.locationservice

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.mobilecourse.taskproject.HomeActivity
import com.mobilecourse.taskproject.R
import com.mobilecourse.taskproject.firebaseservice.TasksAgent
import com.mobilecourse.taskproject.datamodels.Task
import com.mobilecourse.taskproject.locationservice.LocationHelper.Companion.haversine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    // -------------------------------------------------------------------------------------------

    private val TasksList = mutableListOf<Task>()
    private fun start() {
        val date = Date()
        TasksAgent.getUserTasksForDate(date) { tasks ->
            tasks.forEach { task ->
                TasksList.add(task)
            }
        }

        val notificationId = SimpleDateFormat("ddHHmmss", Locale.US).format(date).toInt()
        val notification = NotificationCompat.Builder(this, "locationService")
            .setContentTitle("Tracking location...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MIN)

        locationClient.getLocationUpdates(7000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude
                val lng = location.longitude

                TasksList.forEach { task ->
                    val distance =
                        haversine(lat, lng, task.latLng!!.latitude, task.latLng.longitude)
                    checkDistanceForNotificationReset(distance, task.id)
                    if (distance <= 200) {
                        showTaskNotification(distance, task.id)
                    }
                }
            }
            .launchIn(serviceScope)

        startForeground(notificationId, notification.build())
    }

    private val shownTaskIds = mutableListOf<String>()
    private fun showTaskNotification(distance: Double, taskId: String) {

        if (shownTaskIds.contains(taskId))
            return

        val taskIntent = Intent(this, HomeActivity::class.java)
        taskIntent.putExtra("TASK_ID", taskId)
        taskIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val taskPendingIntent =
            PendingIntent.getActivity(this, 1, taskIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationId = SimpleDateFormat("ddHHmmss", Locale.US).format(Date()).toInt()
        val notification = NotificationCompat.Builder(this, "taskService")
            .setContentTitle("Task nearby")
            .setContentTitle("You're ${distance.toInt()}m away from your task")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(taskPendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification.build())
        shownTaskIds.add(taskId)

    }

    private fun checkDistanceForNotificationReset(distance: Double, taskId: String) {
        if (distance > 200 && shownTaskIds.contains(taskId)) {
            shownTaskIds.remove(taskId)
        }
    }
}