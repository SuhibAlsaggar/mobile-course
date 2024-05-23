
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobilecourse.taskproject.R
import com.mobilecourse.taskproject.taskscroolfragment
import com.mobilecourse.taskproject.testactivity
import taskAdapter
import java.text.SimpleDateFormat
import java.util.*

class HomePage : AppCompatActivity(){

    private lateinit var buttonNext: Button
    private lateinit var buttonPreviousDay: ImageButton
    private lateinit var buttonNextDay: ImageButton
    private lateinit var dateTextView: TextView
    private var currentDate = Date()
    private val format = SimpleDateFormat("dd/MM/yyyy")

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var taskList: MutableList<taskdata>
    private lateinit var adapter: taskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)
        taskList = mutableListOf()
        adapter = taskAdapter(taskList) { item, position ->
            // Handle item click here if needed
        }

        dateTextView = findViewById(R.id.xml_date_time_text_view) // Assuming the ID is correct

        updateDateTextView()

        buttonNext = findViewById(R.id.button_next)
        fetchUserRole()

        buttonNext.setOnClickListener {
            val intent = Intent(
                this,
                testactivity::class.java
            )
            startActivity(intent)
        }

        buttonPreviousDay = findViewById(R.id.button_previous_day)
        buttonPreviousDay.setOnClickListener {
            updateDate(-1)
        }

        buttonNextDay = findViewById(R.id.button_next_day)
        buttonNextDay.setOnClickListener {
            updateDate(1)
        }

        replaceFragment(taskscroolfragment())
    }

    private fun fetchUserRole() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val role = document.getString("role")
                        if (role == "admin") {
                            buttonNext.visibility = View.VISIBLE
                        } else {
                            buttonNext.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }

    private fun updateDate(offset: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_YEAR, offset)
        currentDate = calendar.time
        updateDateTextView()
        loadTasksForDate(currentDate)
    }

    private fun updateDateTextView() {
        val formattedDate = format.format(currentDate)
        dateTextView.text = formattedDate
    }

    private fun loadTasksForDate(date: Date) {
        val formattedDate = format.format(date)
        firestore.collection("tasks")
            .whereEqualTo("date", formattedDate)
            .get()
            .addOnSuccessListener { documents ->
                taskList.clear()
                for (document in documents) {
                    val task = document.toObject(taskdata::class.java)
                    taskList.add(task)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.framelayout, fragment)
        fragmentTransaction.commit()
    }
}
