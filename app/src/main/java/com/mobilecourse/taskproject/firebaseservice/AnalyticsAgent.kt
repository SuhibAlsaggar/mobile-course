package com.mobilecourse.taskproject.firebaseservice

import com.mobilecourse.taskproject.datamodels.Analytics
import java.util.Date

class AnalyticsAgent {
    companion object {
        public fun getAnalyticsForDate(
            selectedDate: Date,
            onAnalyticsRetrieved: (List<Analytics>) -> Unit
        ) {
            TasksAgent.getTasksForDate(selectedDate) { tasks ->
                val userAnalyticsMap = mutableMapOf<String, Analytics>()
                for (task in tasks) {
                    val user = task.assigneeId
                    val userAnalytics =
                        userAnalyticsMap.getOrPut(user) { Analytics(user, 0, 0, 0, 0) }
                    userAnalytics.totalTasks++
                    if (task.subtasks.all { it.completed!! }) {
                        userAnalytics.completedTasks++
                    }
                    userAnalytics.totalSubTasks += task.subtasks.size
                    userAnalytics.completedSubTasks += task.subtasks.count { it.completed!! }
                }
                onAnalyticsRetrieved(userAnalyticsMap.values.toList())
            }
        }

        public fun getUserAnalyticsForDate(
            selectedDate: Date,
            onAnalyticsRetrieved: (Analytics) -> Unit
        ) {
            TasksAgent.getUserTasksForDate(selectedDate) { tasks ->

                val totalTasks = tasks.size
                val completedTasks = tasks.count { task ->
                    task.subtasks.all { it.completed!! }
                }
                val totalSubTasks = tasks.sumOf { it.subtasks.size }
                val completedSubTasks = tasks.flatMap { it.subtasks }.count { it.completed!! }

                val user = tasks.first().assigneeId
                val analytics =
                    Analytics(user, totalTasks, completedTasks, totalSubTasks, completedSubTasks)

                onAnalyticsRetrieved(analytics)
            }
        }
    }
}