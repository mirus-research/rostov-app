package ru.mirus.rostovondon

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.fadeIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ServiceAdapter(
    private var services: List<Service>,
    private val context: Context
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.serviceIcon)
        val name: TextView = itemView.findViewById(R.id.serviceTitle) // фикс!
        val description: TextView = itemView.findViewById(R.id.serviceDescription)

        val app1: ConstraintLayout = itemView.findViewById(R.id.app1)
        val textapp1: TextView = itemView.findViewById<TextView>(R.id.textapp1)
        val app2: ConstraintLayout = itemView.findViewById(R.id.app2)
        val textapp2: TextView = itemView.findViewById<TextView>(R.id.textapp2)
        val app3: ConstraintLayout = itemView.findViewById(R.id.app3)
        val textapp3: TextView = itemView.findViewById<TextView>(R.id.textapp3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.service_item, parent, false)
        return ServiceViewHolder(view)
    }

    override fun getItemCount(): Int = services.size

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.icon.setImageResource(service.iconRes)
        holder.name.text = service.name
        holder.description.text = service.description

        // Скрываем кнопки перед настройкой
        holder.app1.visibility = View.GONE
        holder.app2.visibility = View.GONE
        holder.app3.visibility = View.GONE

        when (service.type) {
            ServiceType.ONE_APP, ServiceType.TWO_APPS, ServiceType.THREE_APPS -> {
                service.apps?.forEachIndexed { index, app ->
                    val button = when (index) {
                        0 -> holder.app1
                        1 -> holder.app2
                        2 -> holder.app3
                        else -> null
                    }
                    val textOF = when (index) {
                        0 -> holder.textapp1
                        1 -> holder.textapp2
                        2 -> holder.textapp3
                        else -> null
                    }

                    button?.apply {
                        visibility = View.VISIBLE
                        setOnClickListener { openAppOrLink(app) }
                    }
                    textOF!!.text = app.name
                }
            }

            ServiceType.ON_MAP -> {
                holder.app1.visibility = View.VISIBLE
                holder.textapp1.text = "Открыть на карте"
                holder.app1.setOnClickListener {
                    val args = Bundle().apply {
                        putString("service_name", service.name)
                        putBoolean("openFromAdditional", true)
                        putInt("icon_res", service.iconRes)
                    }

                    // сохраняем аргументы для MapFragment
                    MapFragment.pendingArgs = args

                    // переключаем на карту
                    val bottomNav = (context as AppCompatActivity)
                        .findViewById<BottomNavigationView>(R.id.bottom_navigation)
                    bottomNav.selectedItemId = R.id.nav_map
                }
            }
        }
        holder.itemView.alpha = 0f
        holder.itemView.translationY = 40f
        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setInterpolator(OvershootInterpolator(1.2f))
            .setStartDelay(position * 30L) // плавный каскадный эффект
            .start()
    }

    private fun openAppOrLink(app: AppItem) {
        // пробуем открыть приложение
        if (app.packageName != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                return
            }
        }

        // если приложения нет, пробуем открыть ссылку
        if (app.url != null) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(app.url))
            context.startActivity(browserIntent)
        } else {
            Toast.makeText(context, "Приложение недоступно", Toast.LENGTH_SHORT).show()
        }
    }
    fun updateData(newServices: List<Service>) {
        services = newServices
        notifyDataSetChanged()
    }
}
