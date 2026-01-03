package ru.mirus.rostovondon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val items: List<CategoryServiceItem>,
    private val onItemClick: (CategoryServiceItem) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CustomViewHolder>() {

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.itemTitle)
        val image: ImageView = itemView.findViewById(R.id.itemImage)
        val root: View = itemView // можно выделять весь item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.image.setImageResource(item.imageResId)

        val context = holder.itemView.context

        if (item.isSelected) {
            holder.root.setBackgroundResource(R.drawable.simple_container_selected)
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.blue_main))
        } else {
            holder.root.setBackgroundResource(R.drawable.simple_container)
            holder.title.setTextColor(android.graphics.Color.BLACK) // ← не black?
        }

        holder.itemView.setOnClickListener {
            items.forEach { it.isSelected = false }
            item.isSelected = true
            notifyDataSetChanged()

            val anim = AnimationUtils.loadAnimation(context, R.anim.select_animation)
            holder.root.startAnimation(anim)

            onItemClick(item)
        }

    }


    override fun getItemCount(): Int = items.size
}
