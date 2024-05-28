package com.mobilecourse.taskproject.firebaseservice

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.mobilecourse.taskproject.datamodels.SubTask
import com.mobilecourse.taskproject.datamodels.Task
import java.util.Calendar
import java.util.Date

public class TasksAgent {

    companion object {
        public fun getUserTasksForDate(
            selectedDate: Date,
            onTasksRetrieved: (List<Task>) -> Unit
        ) {
            val userID = FirebaseHelper.getUserId()
            getTasksForDate(selectedDate, userID, onTasksRetrieved)
        }

        public fun getTasksForDate(
            selectedDate: Date,
            userId: String? = null,
            onTasksRetrieved: (List<Task>) -> Unit
        ) {
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

            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val tasks = mutableListOf<Task>()
                    for (document in task.result!!) {
                        val data = document.data
                        val subtasks = (data["subtasks"] as List<HashMap<String, Any>>).map {
                            SubTask(
                                description = it["description"] as String,
                                completed = it["completed"] as Boolean
                            )
                        }
                        val taskItem = Task(
                            id = document.id,
                            assigneeId = data["assigneeId"] as? String ?: "",
                            title = data["title"] as? String ?: "",
                            date = data["date"] as? Timestamp,
                            latLng = data["latLng"] as? GeoPoint,
                            subtasks = subtasks
                        )
                        tasks.add(taskItem)
                    }
                    onTasksRetrieved(tasks)
                }
            }
        }

        public fun getTaskById(
            taskId: String,
            onTaskRetrieved: (Task?) -> Unit
        ) {
            val db = FirebaseHelper.getDb()
            val docRef = db.collection("tasks").document(taskId)
            docRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document?.exists() == true) {
                        val data = document.data ?: return@addOnCompleteListener
                        val subtasks = (data["subtasks"] as List<HashMap<String, Any>>).map {
                            SubTask(
                                description = it["description"] as String,
                                completed = it["completed"] as Boolean
                            )
                        }
                        val retrievedTask = Task(
                            id = document.id,
                            assigneeId = data["assigneeId"] as? String ?: "",
                            title = data["title"] as? String ?: "",
                            date = data["date"] as? Timestamp,
                            latLng = data["latLng"] as? GeoPoint,
                            subtasks = subtasks
                        )
                        onTaskRetrieved(retrievedTask)
                    } else {
                        onTaskRetrieved(null)
                    }
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

        public fun updateTaskSubtasks(
            taskId: String,
            updatedSubtasks: List<SubTask>,
            onComplete: (Boolean) -> Unit
        ) {
            val db = FirebaseHelper.getDb()

            val docRef = db.collection("tasks").document(taskId)
            docRef.update("subtasks", updatedSubtasks)
                .addOnSuccessListener {
                    onComplete(true)
                }
        }
    }


}