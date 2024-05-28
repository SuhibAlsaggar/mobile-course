package com.mobilecourse.taskproject.firebaseservice

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.mobilecourse.taskproject.datamodels.SubTask
import com.mobilecourse.taskproject.datamodels.Task
import com.mobilecourse.taskproject.datamodels.User
import java.util.Date

public class UserAgent {

    companion object{
        public fun getAnalytics(
            onAnalyticsRetrieved: (List<String>) -> Unit
        ) {
        }

        public fun getUserAnalytics(
            onAnalyticsRetrieved: (String) -> Unit
        ) {
        }

        public fun getUserRole(
            onComplete: (String) -> Unit
        ) {
            val userId = FirebaseHelper.getUserId()
            val db = FirebaseHelper.getDb()

            if (userId == null) {
                onComplete("Unknown")
            }

            db.collection("users")
                .whereEqualTo("userid",userId)
                .limit(1).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val data = task.result.documents[0]
                        if (data == null)
                        {
                            onComplete("Unknown")
                        }
                        val role = data!!["role"] as? String ?: ""
                        onComplete(role)
                    }
            }
        }

        public fun getUsers(
            onComplete: (List<User>?) -> Unit
        ) {
            val db = FirebaseHelper.getDb()
            db.collection("users")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val users = task.result.documents.mapNotNull { document ->
                            val data = document.data
                            if (data != null) {
                                val id = document.id
                                val name = data["name"] as? String ?: ""
                                val role = data["role"] as? String ?: ""
                                User(id, name, role)
                            } else {
                                null
                            }
                        }
                        onComplete(users)
                    } else {
                        onComplete(null)
                    }
                }
        }
    }
}