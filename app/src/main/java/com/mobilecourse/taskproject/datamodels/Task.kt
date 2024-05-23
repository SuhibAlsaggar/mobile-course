package com.mobilecourse.taskproject.datamodels

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

public data class Task(
    val id: String,
    val assigneeId: String,
    val title: String,
    val date: Timestamp?,
    val latLng: GeoPoint?,
    val subtasks: List<SubTask>
)
