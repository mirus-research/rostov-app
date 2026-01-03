package ru.mirus.rostovondon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DistrictAdapter(
    private val districts: List<District>
) : RecyclerView.Adapter<DistrictAdapter.DistrictViewHolder>() {

    class DistrictViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textDistrictName)
        val description: TextView = view.findViewById(R.id.textDescription)
        val image: ImageView = view.findViewById(R.id.imageDistrict)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DistrictViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_district_card, parent, false)
        return DistrictViewHolder(view)
    }

    override fun onBindViewHolder(holder: DistrictViewHolder, position: Int) {
        val district = districts[position]
        holder.name.text = district.name
        holder.description.text = district.description
        holder.image.setImageResource(district.imageResId)
    }

    override fun getItemCount(): Int = districts.size
}
