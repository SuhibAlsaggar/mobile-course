package com.mobilecourse.taskproject.firebaseservice

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

public class FirebaseHelper {
    companion object {

        fun getDb(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        fun getAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        fun getFirebaseUser(): FirebaseUser? {
            return FirebaseAuth.getInstance().currentUser
        }

        fun getUserId(): String? {
            val user = getFirebaseUser()
            return user?.uid
        }
    }
}