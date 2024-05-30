package com.mobilecourse.taskproject.firebaseservice

import com.mobilecourse.taskproject.datamodels.User
import kotlinx.coroutines.tasks.await


public class UserAgent {
    companion object {
        public fun getUserRole(
            onComplete: (String) -> Unit
        ) {
            val userId = FirebaseHelper.getUserId()
            val db = FirebaseHelper.getDb()

            if (userId == null) {
                onComplete("Unknown")
            }

            db.collection("users")
                .whereEqualTo("userid", userId)
                .limit(1).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val data = task.result.documents.first()
                        if (data == null) {
                            onComplete("Unknown")
                        }
                        val role = data!!["role"] as? String ?: ""
                        onComplete(role)
                    }
                }
        }

        fun getCurrentUser(onComplete: (User) -> Unit) {
            val db = FirebaseHelper.getDb()
            val userId = FirebaseHelper.getUserId()
            var user = User("Unknown", "Unknown", "Unknown")

            db.collection("users")
                .whereEqualTo("userid", userId)
                .limit(1)
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documents = task.result.documents
                        if (documents.isNotEmpty()) {
                            val document = documents.first()
                            val data = document.data
                            if (data != null) {
                                val id = data["userid"] as? String ?: ""
                                val name = data["name"] as? String ?: ""
                                val role = data["role"] as? String ?: ""
                                user = User(id, name, role)
                            }
                        }
                    }
                    onComplete(user)
                }
        }

        suspend fun getUserById(userId: String): User {
            val db = FirebaseHelper.getDb()
            val querySnapshot = db.collection("users")
                .whereEqualTo("userid", userId)
                .limit(1)
                .get()
                .await()

            return if (querySnapshot.documents.isNotEmpty()) {
                val document = querySnapshot.documents.first()
                val data = document.data
                if (data != null) {
                    val id = data["userid"] as? String ?: ""
                    val name = data["name"] as? String ?: ""
                    val role = data["role"] as? String ?: ""
                    User(id, name, role)
                } else {
                    User("Unknown", "Unknown", "Unknown")
                }
            } else {
                User("Unknown", "Unknown", "Unknown")
            }
        }

        public fun getUsers(
            onComplete: (List<User>?) -> Unit
        ) {
            val db = FirebaseHelper.getDb()
            db.collection("users")
                .whereEqualTo("role", "user")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val users = task.result.documents.mapNotNull { document ->
                            val data = document.data
                            if (data != null) {
                                val id = data["userid"] as? String ?: ""
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

        public fun signIn(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
            val auth = FirebaseHelper.getAuth()
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, "Sign-in successful")
                } else {
                    onComplete(false, task.exception?.message ?: "Sign-in failed")
                }
            }
        }

        public fun signUp(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
            val auth = FirebaseHelper.getAuth()
            val db = FirebaseHelper.getDb()
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userId = user.uid
                        val name = email.substringBefore('@')
                        val userMap = hashMapOf(
                            "userid" to userId,
                            "role" to "user",
                            "name" to name
                        )

                        db.collection("users")
                            .document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                onComplete(true, "Sign-up successful")
                            }
                            .addOnFailureListener { e ->
                                onComplete(false, e.message ?: "Failed to add user to database")
                            }
                    } else {
                        onComplete(false, "Failed to retrieve user")
                    }
                } else {
                    onComplete(false, task.exception?.message ?: "Sign-up failed")
                }
            }
        }

        public fun signOut(onComplete: (Boolean, String) -> Unit) {
            val auth = FirebaseHelper.getAuth()
            try {
                auth.signOut()
                onComplete(true, "Sign-out successful")
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Sign-out failed")
            }
        }

    }
}