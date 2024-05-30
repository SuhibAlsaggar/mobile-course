package com.mobilecourse.taskproject.datamodels

data class User(
    val id: String,
    val name: String,
    val role: String
) {
    override fun toString(): String {
        return name
    }
}
