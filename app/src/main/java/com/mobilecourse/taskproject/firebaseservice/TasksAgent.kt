@file:OptIn(DelicateCoroutinesApi::class)

package com.mobilecourse.taskproject.firebaseservice

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.mobilecourse.taskproject.datamodels.SubTask
import com.mobilecourse.taskproject.datamodels.Task
import com.mobilecourse.taskproject.firebaseservice.UserAgent.Companion.getUserById
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

public class TasksAgent {

    companion object {
        public fun getUserTasksForDate(
            selectedDate: Date,
            onTasksRetrieved: (List<Task>) -> Unit
        ) {
            val userID = FirebaseHelper.getUserId()
            getTasksForDate(selectedDate, userID, false, onTasksRetrieved)
        }

        public fun getUserIncompleteTasksForDate(
            selectedDate: Date,
            onTasksRetrieved: (List<Task>) -> Unit
        ) {
            val userID = FirebaseHelper.getUserId()
            getTasksForDate(selectedDate, userID, true, onTasksRetrieved)
        }

        public fun getTasksForDate(
            selectedDate: Date,
            userId: String? = null,
            onlyIncomplete: Boolean = false,
            onTasksRetrieved: (List<Task>) -> Unit
        ) {
            GlobalScope.launch(Dispatchers.Main) {
                val db = FirebaseHelper.getDb()
                val calendar = Calendar.getInstance()

                calendar.time = selectedDate
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.time

                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val endOfDay = calendar.time

                var query = db.collection("tasks")
                    .whereGreaterThanOrEqualTo("date", startOfDay)
                    .whereLessThan("date", endOfDay)

                if (userId != null) {
                    query = query.whereEqualTo("assigneeId", userId)
                }

                if (onlyIncomplete) {
                    query = query.whereEqualTo("assigneeId", userId)
                }

                try {
                    val taskSnapshots = query.get().await()
                    val tasks = mutableListOf<Task>()

                    for (document in taskSnapshots) {
                        val data = document.data
                        val subtasks = (data["subtasks"] as List<HashMap<String, Any>>).map {
                            SubTask(
                                description = it["description"] as String,
                                completed = it["completed"] as Boolean
                            )
                        }

                        val assigneeId = data["assigneeId"] as? String ?: ""
                        val user = withContext(Dispatchers.IO) {
                            getUserById(assigneeId)
                        }

                        if (!onlyIncomplete || subtasks.any { task -> !task.completed!! }) {
                            tasks.add(
                                Task(
                                    id = document.id,
                                    assigneeId = user.name,
                                    title = data["title"] as? String ?: "",
                                    date = data["date"] as? Timestamp,
                                    latLng = data["latLng"] as? GeoPoint,
                                    subtasks = subtasks
                                )
                            )
                        }
                    }
                    onTasksRetrieved(tasks)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        public fun getTaskById(
            taskId: String,
            onTaskRetrieved: (Task?) -> Unit
        ) {
            GlobalScope.launch(Dispatchers.Main) {
                val db = FirebaseHelper.getDb()
                val docRef = db.collection("tasks").document(taskId)

                try {
                    val document = docRef.get().await()
                    if (document.exists()) {
                        val data = document.data ?: return@launch onTaskRetrieved(null)
                        val subtasks = (data["subtasks"] as List<HashMap<String, Any>>).map {
                            SubTask(
                                description = it["description"] as String,
                                completed = it["completed"] as Boolean
                            )
                        }

                        val assigneeId = data["assigneeId"] as? String ?: ""
                        val user = withContext(Dispatchers.IO) {
                            getUserById(assigneeId)
                        }

                        val retrievedTask = Task(
                            id = document.id,
                            assigneeId = user.name,
                            title = data["title"] as? String ?: "",
                            date = data["date"] as? Timestamp,
                            latLng = data["latLng"] as? GeoPoint,
                            subtasks = subtasks
                        )
                        onTaskRetrieved(retrievedTask)
                    } else {
                        onTaskRetrieved(null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onTaskRetrieved(null)
                }
            }
        }

        public fun createTask(
            title: String,
            latLng: GeoPoint,
            subtasks: List<SubTask> = emptyList(),
            assigneeId: String,
            onComplete: (Boolean) -> Unit
        ) {
            val db = FirebaseHelper.getDb()

            val data = hashMapOf(
                "title" to title,
                "assigneeId" to assigneeId,
                "date" to FieldValue.serverTimestamp(),
                "latLng" to latLng,
                "subtasks" to subtasks
            )

            db.collection("tasks").add(data)
                .addOnSuccessListener {
                    onComplete(true)
                }
        }

        fun markTask(taskId: String, complete: Boolean = true, onComplete: (Boolean) -> Unit) {
            val db = FirebaseHelper.getDb()
            val docRef = db.collection("tasks").document(taskId)

            docRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val subtasks =
                            (documentSnapshot.data!!["subtasks"] as List<HashMap<String, Any>>).map {
                                SubTask(
                                    description = it["description"] as String,
                                    completed = complete
                                )
                            }
                        docRef.update("subtasks", subtasks)
                            .addOnSuccessListener {
                                onComplete(true)
                            }
                    } else {
                        onComplete(false)
                    }
                }
        }
    }
}
