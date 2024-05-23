data class taskdata(
    val name: String,
    val date: String,
    var address: String = "", // Optional address field
    val latitude: Double = 0.0, // Optional latitude
    val longitude: Double = 0.0, // Optional longitude
    var isChecked: Boolean = false // Optional field for checkbox state
)
