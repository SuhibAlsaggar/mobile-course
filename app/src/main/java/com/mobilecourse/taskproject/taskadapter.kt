import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobilecourse.taskproject.R

import java.io.IOException
import java.util.Locale

class taskAdapter(
    private val data: List<taskdata>,
    private val onItemClickListener: (taskdata, Int) -> Unit
) : RecyclerView.Adapter<taskAdapter.MyViewHolder>() {

    private lateinit var geocoder: Geocoder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_taskscroolfragment, parent, false)
        geocoder = Geocoder(parent.context, Locale.getDefault())
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = data[position]
        holder.textName.text = item.name
        holder.textDate.text = item.date

        // Perform reverse geocoding only if address is empty
        if (item.address.isEmpty()) {
            reverseGeocode(item.latitude, item.longitude) { address ->
                item.address = address
                holder.textAddress.text = address
            }
        } else {
            holder.textAddress.text = item.address
        }

        holder.imageCheckbox.setImageResource(
            if (item.isChecked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        )

        holder.itemView.setOnClickListener {
            onItemClickListener(item, position)
        }

        holder.imageCheckbox.setOnClickListener {
            item.isChecked = !item.isChecked
            updateFirebase(item, position)
            holder.imageCheckbox.setImageResource(
                if (item.isChecked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
            )
        }
    }

    override fun getItemCount(): Int = data.size

    private fun updateFirebase(item: taskdata, position: Int) {
        // Implement Firebase update logic based on item.isChecked
    }

    private fun reverseGeocode(latitude: Double, longitude: Double, callback: (String) -> Unit) {
        // Perform geocoding in a background thread to avoid blocking the UI thread
        Thread {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0].getAddressLine(0)
                    callback(address)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.text_name)
        val textAddress: TextView = view.findViewById(R.id.text_address)
        val textDate: TextView = view.findViewById(R.id.text_date)
        val imageCheckbox: ImageView = view.findViewById(R.id.image_checkbox)
    }
}
