package ru.mirus.rostovondon

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


data class OsmResponse(
    val elements: List<OsmElement>
)

data class OsmElement(
    val type: String,
    val id: Long,
    val lat: Double?,
    val lon: Double?,
    val center: Center?,
    val tags: Map<String, String>?
)

data class Center(
    val lat: Double,
    val lon: Double
)


class MapFragment : Fragment(R.layout.fragment_map) {

    companion object {
        // Временное хранилище аргументов
        var pendingArgs: Bundle? = null

        fun newInstance(args: Bundle? = null): MapFragment {
            val fragment = MapFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val queryByService = mapOf(
        "Рестораны" to ("node[\"amenity\"=\"restaurant\"](area.searchArea);" to R.drawable.services_food_3),
        "Больницы" to ("node[\"amenity\"=\"hospital\"](area.searchArea);node[\"amenity\"=\"pharmacy\"](area.searchArea);" to R.drawable.services_medicine_hospital),
        "Детские сады" to ("node[\"amenity\"=\"kindergarten\"](area.searchArea);" to R.drawable.services_childs_kindergarden),
        "Школы" to ("node[\"amenity\"=\"school\"](area.searchArea);" to R.drawable.services_childs_school),
        "Парки, набережные" to ("way[\"leisure\"=\"park\"](area.searchArea);relation[\"leisure\"=\"park\"](area.searchArea);" to R.drawable.services_dosug_parks),
        "Кино, театры, концерты" to ("node[\"amenity\"=\"theatre\"](area.searchArea);node[\"amenity\"=\"cinema\"](area.searchArea);node[\"amenity\"=\"arts_centre\"](area.searchArea);" to R.drawable.services_dosug_cinema),
        "Спорт" to ("node[\"leisure\"=\"fitness_centre\"](area.searchArea);node[\"sport\"](area.searchArea);" to R.drawable.services_dosug_sport),
        "МФЦ" to ("node[\"office\"=\"government\"](area.searchArea);" to R.drawable.services_gov_2),
        "Гос. учреждения" to ("node[\"amenity\"=\"townhall\"](area.searchArea);node[\"amenity\"=\"police\"](area.searchArea);" to R.drawable.services_gov_3),
        "Сервис" to ("node[\"shop\"=\"car_repair\"](area.searchArea);node[\"amenity\"=\"car_wash\"](area.searchArea);" to R.drawable.services_auto_1),
        "Заправки" to ("node[\"amenity\"=\"fuel\"](area.searchArea);" to R.drawable.services_auto_2),
        "Услуги" to ("node[\"office\"](area.searchArea);" to R.drawable.services_home_services)
    )


    private lateinit var mapView: MapView
    private var currentMapObjects: ClusterizedPlacemarkCollection? = null
    private val client = OkHttpClient()
    private var isExpanded = false

    private var fromAdditonal = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var selectedBlockId: Int? = null  // хранит id выделенного блока

        val openFromAdditional = arguments?.getBoolean("openFromAdditional")
        fromAdditonal = openFromAdditional == true

        // ✅ Инициализация карты
        mapView = view.findViewById(R.id.mapview)

        val point = Point(47.229404, 39.715828)
        mapView.map.move(
            com.yandex.mapkit.map.CameraPosition(
                point,
                12.0f,
                0.0f,
                0.0f
            )
        )

        val cityBlock = view.findViewById<ConstraintLayout>(R.id.cityBlock)
        val additionalsBlock = view.findViewById<ConstraintLayout>(R.id.additionalsBlock)
        val smartMapBlock = view.findViewById<ConstraintLayout>(R.id.smartMapBlock)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isExpanded) {
                        // Если блок открыт — просто сворачиваем его
                        additionalsBlock.performClick()
                    } else {
                        // Если блок закрыт — переключаемся на домашний фрагмент
                        val bottomNav =
                            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                        bottomNav.selectedItemId = R.id.nav_home
                    }
                }
            })

        val rostovLogo = view.findViewById<ImageView>(R.id.rostovLogo)
        val labelCityName = view.findViewById<TextView>(R.id.labelCityName)
        val imageBuilding = view.findViewById<ImageView>(R.id.additionalIcon)

        // Внутренние мини-блоки для анимации
        val innerBlocks = listOf(
            view.findViewById<ConstraintLayout>(R.id.transportBlock),
            view.findViewById<ConstraintLayout>(R.id.restaurantBlock),
            view.findViewById<ConstraintLayout>(R.id.fuelBlock),
            view.findViewById<ConstraintLayout>(R.id.parkBlock),
            view.findViewById<ConstraintLayout>(R.id.goodsBlock),
            view.findViewById<ConstraintLayout>(R.id.medicineBlock)
        )

        //Обработка нажатий
        val additionalIcon = additionalsBlock.findViewById<ImageView>(R.id.additionalIcon)

// Множество блоков с красным выделением
        val redBlocks = setOf(R.id.fuelBlock, R.id.parkBlock)

        // Сброс выделений
        fun resetSelections() {
            innerBlocks.forEach { block ->
                block.setBackgroundResource(R.drawable.simple_container)
            }
        }

        innerBlocks.forEach { block ->
            block.setOnClickListener {
                block.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(250)
                    .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                    .withEndAction {
                        block.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(250)
                            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                            .start()
                    }
                    .start()

                if (selectedBlockId == block.id) {
                    resetSelections()
                    additionalIcon?.setImageResource(R.drawable.services_home_buildings) // <-- сюда ставим картинку
                    additionalsBlock.setBackgroundResource(R.drawable.simple_container_selected)
                    selectedBlockId = null
                    currentMapObjects!!.clear()
                } else {
                    resetSelections()

                    if (redBlocks.contains(block.id)) {
                        block.setBackgroundResource(R.drawable.simple_container_selected_red)
                        additionalsBlock.setBackgroundResource(R.drawable.simple_container_selected_red)
                    } else {
                        block.setBackgroundResource(R.drawable.simple_container_selected)
                        additionalsBlock.setBackgroundResource(R.drawable.simple_container_selected)
                    }

                    // Проставляем иконку через when
                    val iconRes = when (block.id) {
                        R.id.transportBlock -> R.drawable.category_bus
                        R.id.restaurantBlock -> R.drawable.services_food_3
                        R.id.fuelBlock -> R.drawable.services_auto_2
                        R.id.parkBlock -> R.drawable.services_dosug_parks
                        R.id.goodsBlock -> R.drawable.services_dosug_cinema
                        R.id.medicineBlock -> R.drawable.services_medicine_hospital
                        else -> 0
                    }

                    if (iconRes != 0) {
                        additionalIcon?.setImageResource(iconRes)
                    } else {
                        additionalIcon?.setImageDrawable(null)
                    }
                    selectedBlockId = block.id

                    // Твои кастомные действия на блоки
                    when (block.id) {
                        R.id.transportBlock -> {
                            loadCategory("node[\"highway\"=\"bus_stop\"](area.searchArea);", R.drawable.category_bus)
                        }

                        R.id.restaurantBlock -> {
                            loadCategory("node[\"amenity\"=\"restaurant\"](area.searchArea);", R.drawable.services_food_3)
                        }

                        R.id.fuelBlock -> {
                            loadCategory("node[\"amenity\"=\"fuel\"](area.searchArea);", R.drawable.services_auto_2)
                        }

                        R.id.parkBlock -> {
                            loadCategory("way[\"leisure\"=\"park\"](area.searchArea);relation[\"leisure\"=\"park\"](area.searchArea);", R.drawable.services_dosug_parks)
                        }

                        R.id.goodsBlock -> {
                            loadCategory("node[\"shop\"=\"mall\"](area.searchArea);", R.drawable.services_dosug_cinema)
                        }

                        R.id.medicineBlock -> {
                            loadCategory("node[\"amenity\"=\"hospital\"](area.searchArea);node[\"amenity\"=\"pharmacy\"](area.searchArea);", R.drawable.services_medicine_hospital)
                        }
                    }
                }
                additionalsBlock.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction {
                        additionalsBlock.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start()
                    }
                    .start()

                additionalIcon.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction {
                        additionalIcon.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start()
                    }
                    .start()
            }
        }


        val blocks = listOf(
            cityBlock,
            additionalsBlock
        )

        val elements = listOf(
            rostovLogo,
            labelCityName,
            imageBuilding
        )

        // --- Первичная анимация cityBlock и additionalsBlock ---
        blocks.forEach {
            it.alpha = 0f
            it.translationY = 100f
        }

        elements.forEach {
            it.alpha = 0f
        }

        blocks.forEachIndexed { index, block ->
            block.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay((index * 200).toLong())
                .setInterpolator(android.view.animation.OvershootInterpolator())
                .start()
        }

        elements.forEachIndexed { index, view ->
            view.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay((index * 80).toLong())
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }

        // --- Получаем данные, переданные из адаптера ---
        val serviceName = arguments?.getString("service_name")
        val iconRes = arguments?.getInt("icon_res") ?: R.drawable.services_home_buildings


        serviceName?.let { name ->
            val query = queryByService[name]?.first
            val icon = queryByService[name]?.second ?: iconRes

            if (query != null) {
                loadCategory(query, icon)

                // ставим иконку в additionalsBlock
                val additionalIcon = view.findViewById<ImageView>(R.id.additionalIcon)
                additionalIcon.setImageResource(icon)

                // визуально подсветим additionalsBlock как выбранный
                val additionalsBlock = view.findViewById<ConstraintLayout>(R.id.additionalsBlock)
                additionalsBlock.setBackgroundResource(R.drawable.simple_container_selected)
            }
        }


        // --- Изначально скрываем smartMapBlock ---
        smartMapBlock.visibility = View.GONE
        smartMapBlock.alpha = 0f
        smartMapBlock.translationY = 100f

        // --- Клик по additionalsBlock ---
        additionalsBlock.setOnClickListener {
            if(!fromAdditonal){
                if (!isExpanded) {
                    additionalsBlock.animate()
                        .scaleX(1.16f)
                        .scaleY(1.16f)
                        .setDuration(100)
                        .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                        .withEndAction {
                            additionalsBlock.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                                .start()
                        }
                        .start()
                    // Показ с анимацией
                    smartMapBlock.visibility = View.VISIBLE
                    smartMapBlock.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .withEndAction {
                            innerBlocks.forEachIndexed { idx, block ->
                                block.alpha = 0f
                                block.translationY = 50f
                                block.animate()
                                    .alpha(1f)
                                    .translationY(0f)
                                    .setStartDelay((idx * 150).toLong())
                                    .setDuration(300)
                                    .setInterpolator(android.view.animation.OvershootInterpolator(1.2f))
                                    .start()
                            }
                        }
                        .start()
                } else {
                    // Плавное скрытие внутренних блоков
                    innerBlocks.forEachIndexed { idx, block ->
                        block.animate()
                            .alpha(0f)
                            .translationY(50f) // чуть вниз при исчезновении
                            .setDuration(300)
                            .setStartDelay((idx * 80).toLong()) // поочерёдно
                            .setInterpolator(android.view.animation.AccelerateInterpolator())
                            .start()
                    }

                    // Скрытие smartMapBlock после анимации внутренних
                    smartMapBlock.animate()
                        .alpha(0f)
                        .translationY(100f)
                        .setStartDelay(innerBlocks.size * 80L) // ждём, пока уйдут все внутренние
                        .setDuration(400)
                        .setInterpolator(android.view.animation.AccelerateInterpolator())
                        .withEndAction {
                            smartMapBlock.visibility = View.GONE
                        }
                        .start()

                    additionalsBlock.animate()
                        .scaleX(0.82f)
                        .scaleY(0.82f)
                        .setDuration(100)
                        .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                        .withEndAction {
                            additionalsBlock.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                                .start()
                        }
                        .start()
                }
                isExpanded = !isExpanded
            }else{
                additionalsBlock.animate()
                    .scaleX(0.82f)
                    .scaleY(0.82f)
                    .setDuration(100)
                    .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                    .withEndAction {
                        additionalsBlock.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                            .start()
                    }
                    .start()
                currentMapObjects!!.clear()
                fromAdditonal = false
                additionalIcon.setImageResource(R.drawable.services_home_buildings)
            }

        }
    }

    // --- Загрузка категории объектов ---
    private fun loadCategory(query: String, iconRes: Int) {
        val url =
            "https://overpass-api.de/api/interpreter?data=[out:json][timeout:25];area[\"name\"=\"Ростов-на-Дону\"]->.searchArea;($query);out%20center;"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { json ->
                    val gson = Gson()
                    val osm = gson.fromJson(json, OsmResponse::class.java)

                    view?.post {
                        showPlacesOnMap(osm.elements, iconRes)
                    }
                }
            }
        })
    }

    private fun showPlacesOnMap(elements: List<OsmElement>, iconRes: Int) {
        // Удаляем старую коллекцию
        currentMapObjects?.clear()

        currentMapObjects = null
        // Создаём коллекцию с кластеризацией
        val clusterCollection = mapView.map.mapObjects.addClusterizedPlacemarkCollection { cluster ->
            // Иконка кластера (можно использовать ту же иконку, что и для точек)
            cluster.appearance.setIcon(
                ImageProvider.fromResource(requireContext(), iconRes)
            )
            cluster.appearance.setIconStyle(
                IconStyle().apply { scale = 0.3f } // размер кластера
            )
        }

        // Добавляем точки
        elements.forEach { el ->
            val lat = el.lat ?: el.center?.lat ?: return@forEach
            val lon = el.lon ?: el.center?.lon ?: return@forEach

            clusterCollection.addPlacemark(
                Point(lat, lon),
                ImageProvider.fromResource(requireContext(), iconRes),
                IconStyle().apply { scale = 0.3f } // уменьшенная иконка
            )
        }

        // Кластеризация
        clusterCollection.clusterPlacemarks(50.0, 50)

        // Сохраняем для очистки в будущем
        currentMapObjects = clusterCollection
    }




    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}
