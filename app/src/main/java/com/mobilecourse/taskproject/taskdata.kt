package com.mobilecourse.taskproject

data class taskdata(val name: String, var address: String, val date: String, var isChecked: Boolean = false, val latitude: Double,
                    val longitude: Double )
