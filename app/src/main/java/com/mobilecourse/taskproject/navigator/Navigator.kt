package com.mobilecourse.taskproject.navigator

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mobilecourse.taskproject.AnalyticsActivity
import com.mobilecourse.taskproject.AuthActivity
import com.mobilecourse.taskproject.HomePageActivity
import com.mobilecourse.taskproject.MapActivity
import com.mobilecourse.taskproject.R
import com.mobilecourse.taskproject.TaskCreateActivity
import com.mobilecourse.taskproject.TaskDetailsActivity
import com.mobilecourse.taskproject.firebaseservice.UserAgent

class Navigator {
    companion object {
        fun toHomePage(context: Context) {
            if (context::class.java == HomePageActivity::class.java)
                return

            val intent = Intent(context, HomePageActivity::class.java)
            context.startActivity(intent)
        }

        fun toAnalytics(context: Context) {
            if (context::class.java == AnalyticsActivity::class.java)
                return

            val intent = Intent(context, AnalyticsActivity::class.java)
            context.startActivity(intent)
        }

        fun toMap(context: Context) {
            if (context::class.java == MapActivity::class.java)
                return

            val intent = Intent(context, MapActivity::class.java)
            context.startActivity(intent)
        }

        fun toAuth(context: Context) {
            if (context::class.java == AuthActivity::class.java)
                return

            val intent = Intent(context, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        fun toTaskCreate(context: Context) {
            if (context::class.java == TaskCreateActivity::class.java)
                return

            val intent = Intent(context, TaskCreateActivity::class.java)
            context.startActivity(intent)
        }

        fun toTaskDetails(context: Context, taskId: String) {
            if (context::class.java == TaskDetailsActivity::class.java)
                return

            val intent = Intent(context, TaskDetailsActivity::class.java)
            intent.putExtra("TASK_ID", taskId)
            context.startActivity(intent)
        }

        fun setNavigationBar(activity: Activity) {
            val bottomNavigationView =
                activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)

            // Determine the currently active activity and set the selected item accordingly
            val currentActivity = activity::class.java
            when (currentActivity) {
                MapActivity::class.java -> bottomNavigationView.selectedItemId = R.id.navigation_map
                HomePageActivity::class.java -> bottomNavigationView.selectedItemId =
                    R.id.navigation_home
                AnalyticsActivity::class.java -> bottomNavigationView.selectedItemId =
                    R.id.navigation_analytics
                else -> bottomNavigationView.selectedItemId = R.id.navigation_home
            }

            val bottomNavigationMenu = bottomNavigationView.menu

            bottomNavigationMenu.findItem(R.id.navigation_home).setOnMenuItemClickListener {
                toHomePage(activity)
                return@setOnMenuItemClickListener true
            }

            bottomNavigationMenu.findItem(R.id.navigation_analytics)
                .setOnMenuItemClickListener {
                    toAnalytics(activity)
                    return@setOnMenuItemClickListener true
                }

            UserAgent.getUserRole { role ->
                bottomNavigationMenu.findItem(R.id.navigation_map)
                    .setOnMenuItemClickListener {
                        toMap(activity)
                        return@setOnMenuItemClickListener true
                    }.setVisible(role != "admin")
            }
        }
    }
}