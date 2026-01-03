package ru.mirus.rostovondon

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ServicesFragment : Fragment(R.layout.fragment_service) {
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var serviceAdapter: ServiceAdapter

    private val servicesByCategory = mapOf(
        "Транспорт" to listOf(
            Service(
                R.drawable.services_transport_taxi,
                "Такси",
                "Выбор приложения",
                type = ServiceType.THREE_APPS,
                apps = listOf(
                    AppItem("Яндекс Go", "ru.yandex.taxi", "https://go.yandex/"),
                    AppItem("Uber Russia",  "com.ubercab", "https://www.uber.com/ee/ru/ride/"),
                    AppItem("Ситимобил", "ru.citymobil", "https://city-mobil.ru/rostov-na-donu")
                )
            ),
            Service(
                R.drawable.services_transport_parking,
                "Парковки",
                "Оплата в RuParking",
                type = ServiceType.ONE_APP,
                apps = listOf(
                    AppItem("RuParking", "ru.rupaking.app", "https://www.rustore.ru/catalog/app/ru.angelsit.parking")
                )
            ),
            Service(
                R.drawable.services_transport_scooters,
                "Самокаты",
                "Выбор сервиса",
                type = ServiceType.TWO_APPS,
                apps = listOf(
                    AppItem("Whoosh", "me.whoosh", "https://www.rustore.ru/catalog/app/com.punicapp.whoosh"),
                    AppItem("Urent", "com.urent", "https://urent.ru/")
                )
            )
        ),

        "Еда" to listOf(
            Service(
                R.drawable.services_food_blue,
                "Доставка еды",
                "Выбор приложения",
                type = ServiceType.TWO_APPS,
                apps = listOf(
                    AppItem("Яндекс Еда", "ru.yandex.eda", "https://eda.yandex.ru"),
                    AppItem("Самокат", null, "https://samokat.ru")
                )
            ),
            Service(
                R.drawable.services_food_red,
                "Доставка продуктов",
                "Выбор приложения",
                type = ServiceType.TWO_APPS,
                apps = listOf(
                    AppItem("СберМаркет", "ru.sbermarket", "https://sbermarket.ru"),
                    AppItem("Лента Онлайн", null, "https://lenta.com")
                )
            ),
            Service(
                R.drawable.services_food_3,
                "Рестораны",
                "Ближайшие рестораны на карте",
                type = ServiceType.ON_MAP
            )
        ),

        "Дом" to listOf(
            Service(
                R.drawable.services_home_buildings,
                "Оплата ЖКХ",
                "Выбор сервиса",
                type = ServiceType.TWO_APPS,
                apps = listOf(
                    AppItem("ГорЭнергоСбыт", "ru.gorenergosbyt", "https://gesbt.ru"),
                    AppItem("ГИС ЖКХ", null, "https://dom.gosuslugi.ru")
                )
            ),
            Service(
                R.drawable.services_home_master,
                "Вызов мастера",
                "электрик, сантехник, уборка",
                type = ServiceType.ONE_APP,
                apps = listOf(
                    AppItem("YouDo", "ru.youdo", "https://youdo.com")
                )
            ),
            Service(
                R.drawable.services_home_services,
                "Услуги",
                "Домофон, Видеонаблюдение",
                type = ServiceType.ON_MAP
            )
        ),

        "Здоровье" to listOf(
            Service(
                R.drawable.services_medicine_doctor,
                "Запись к врачу",
                "Госуслуги, Здоровье Дона",
                type = ServiceType.TWO_APPS,
                apps = listOf(
                    AppItem("Госуслуги", null, "https://www.gosuslugi-rostov.ru/Registratura.aspx"),
                    AppItem("Здоровье Дона", null, "https://www.minzdrav.donland.ru")
                )
            ),
            Service(
                R.drawable.services_medicine_lekarstvo,
                "Лекарства",
                "Аптеки и доставка лекарств",
                type = ServiceType.ONE_APP,
                apps = listOf(
                    AppItem("Аптека.ру", "ru.apteka", "https://apteka.ru")
                )
            ),
            Service(
                R.drawable.services_medicine_hospital,
                "Больницы",
                "Медицинские учреждения на карте",
                type = ServiceType.ON_MAP
            )
        ),

        "Дети" to listOf(
            Service(
                R.drawable.services_childs_kindergarden,
                "Детские сады",
                "Детские сады на карте",
                type = ServiceType.ON_MAP
            ),
            Service(
                R.drawable.services_childs_school,
                "Школы",
                "Школы на карте",
                type = ServiceType.ON_MAP
            )
        ),

        "Досуг" to listOf(
            Service(
                R.drawable.services_dosug_cinema,
                "Кино, театры, концерты",
                "Интересные места на карте",
                type = ServiceType.ON_MAP
            ),
            Service(
                R.drawable.services_dosug_parks,
                "Парки, набережные",
                "Куда сходить",
                type = ServiceType.ON_MAP
            ),
            Service(
                R.drawable.services_dosug_sport,
                "Спорт",
                "Спортзалы, бассейны, йога-центры",
                type = ServiceType.ON_MAP
            )
        ),
        "Документы" to listOf(
            Service(
                R.drawable.services_gos_1,
                "Госуслуги",
                "Ссылка на ресурс",
                type = ServiceType.ONE_APP,
                apps = listOf(
                    AppItem("Госуслуги", "ru.gosuslugi", "https://www.gosuslugi.ru")
                )
            ),
            Service(
                R.drawable.services_gov_2,
                "МФЦ",
                "МФЦ на карте",
                type = ServiceType.ON_MAP
            ),
            Service(
                R.drawable.services_gov_3,
                "Гос. учреждения",
                "Паспортные столы, ГИБДД и т. д.",
                type = ServiceType.ON_MAP
            )
        ),

        "Авто" to listOf(
            Service(
                R.drawable.services_auto_1,
                "Сервис",
                "Автомойки, шиномонтажи, СТО",
                type = ServiceType.ON_MAP
            ),
            Service(
                R.drawable.services_auto_2,
                "Заправки",
                "Ближайшие АЗС на карте",
                type = ServiceType.ON_MAP
            )
        ),

        "Доставка" to listOf(
            Service(
                R.drawable.services_delivery_1,
                "Посылки",
                "Почта России, Boxberry, CDEK",
                type = ServiceType.THREE_APPS,
                apps = listOf(
                    AppItem("Почта России", "ru.russianpost.client", "https://www.pochta.ru"),
                    AppItem("Boxberry", null, "https://boxberry.ru"),
                    AppItem("CDEK", "ru.cdek.client", "https://www.cdek.ru")
                )
            ),
            Service(
                R.drawable.services_delivery_2,
                "Маркетплейсы",
                "Ozon, Wildberries, Яндекс Маркет",
                type = ServiceType.THREE_APPS,
                apps = listOf(
                    AppItem("Ozon", "ru.ozon.app.android", "https://ozon.ru"),
                    AppItem("Wildberries", "ru.wildberries.app", "https://wildberries.ru"),
                    AppItem("Яндекс Маркет", "ru.yandex.market", "https://market.yandex.ru")
                )
            )
        ),

        )

    private lateinit var adapter: CategoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔍 Получаем нужные элементы
        val goodServicesText = view.findViewById<TextView>(R.id.good_services_text)
        val addBlock = view.findViewById<View>(R.id.addBlock)
        val categoryText = view.findViewById<TextView>(R.id.categoryText)
        val categoriesRecyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val servicesRecyclerView = view.findViewById<RecyclerView>(R.id.servicesRecyclerView)

        val categories = listOf(
            CategoryServiceItem("Транспорт", R.drawable.category_bus, isSelected = true),
            CategoryServiceItem("Еда", R.drawable.category_food),
            CategoryServiceItem("Дом", R.drawable.category_house),
            CategoryServiceItem("Здоровье", R.drawable.category_health),
            CategoryServiceItem("Дети", R.drawable.category_family),
            CategoryServiceItem("Досуг", R.drawable.category_bed),
            CategoryServiceItem("Документы", R.drawable.category_document),
            CategoryServiceItem("Авто", R.drawable.category_car),
            CategoryServiceItem("Доставка", R.drawable.category_delivery)
        )

        categoryAdapter = CategoryAdapter(categories) { clickedCategory ->
            // Обновляем выделение в категориях
            categories.forEach { it.isSelected = it.title == clickedCategory.title }
            categoryAdapter.notifyDataSetChanged()

            // Обновляем сервисы внизу
            val newServices = servicesByCategory[clickedCategory.title] ?: emptyList()
            serviceAdapter.updateData(newServices)
        }

// Настраиваем адаптеры и layout менеджеры
        categoriesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        categoriesRecyclerView.adapter = categoryAdapter

        serviceAdapter = ServiceAdapter(
            servicesByCategory[categories.first().title] ?: emptyList(),
            requireContext()
        )

        servicesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        servicesRecyclerView.adapter = serviceAdapter

        //Анимашки
        val blocks = listOf(addBlock)
        val elements =
            listOf(goodServicesText, categoryText, categoriesRecyclerView, servicesRecyclerView)
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
                .setStartDelay((index * 120).toLong() + 400) // позже блоков
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }

        val addButton = view.findViewById<View>(R.id.addButton)
        addButton.setOnClickListener {
            animateClick(addButton) {
                val targetCategory = categories.find { it.title == "Документы" }

                if (targetCategory != null) {
                    categories.forEach { it.isSelected = it.title == targetCategory.title }
                    categoryAdapter.notifyDataSetChanged()

                    val newServices = servicesByCategory[targetCategory.title] ?: emptyList()
                    serviceAdapter.updateData(newServices)

                    val position = categories.indexOf(targetCategory)
                    if (position != -1) {
                        categoriesRecyclerView.scrollToPosition(position)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Категория 'Документы' не найдена",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)

                if (bottomNav.selectedItemId != R.id.nav_home) {
                    // Переключаемся на домашний экран
                    bottomNav.selectedItemId = R.id.nav_home
                } else {
                    // Если уже на домашнем, то выполняем стандартный выход
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
    }

    private fun animateClick(view: View, onEnd: () -> Unit) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .withEndAction {
                        onEnd()
                    }
                    .start()
            }
            .start()
    }

}
