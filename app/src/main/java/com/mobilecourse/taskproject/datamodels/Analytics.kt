package com.mobilecourse.taskproject.datamodels

data class Analytics(
    var user: String,
    var totalTasks: Int,
    var completedTasks: Int,
    var totalSubTasks: Int,
    var completedSubTasks: Int,
)
