package ru.mirus.rostovondon

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.vk.id.VKID

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val districtList = listOf(
        District(
            "Кировский",
            "Центр города, историческое и административное ядро. Театры, набережная, бизнес-центры.",
            R.drawable.kirovski
        ),
        District(
            "Советский",
            "Юго-запад города. Спокойные жилые кварталы, много новостроек, зелени и школ.",
            R.drawable.sovetski
        ),
        District(
            "Пролетарский",
            "Юго-восток города. Рабочая атмосфера, рынки, складские и промышленные зоны.",
            R.drawable.proletarski
        ),
        District(
            "Железнодорожный",
            "Промышленный район с железнодорожной инфраструктурой. Старый жилой фонд и частный сектор.",
            R.drawable.shelezno
        ),
        District(
            "Октябрьский",
            "Северо-запад. Смешанная застройка, торговые центры, удобная инфраструктура и парки.",
            R.drawable.oktabrsky
        ),
        District(
            "Первомайский",
            "Восток города. Частный сектор, зелёные зоны, спокойные спальные кварталы.",
            R.drawable.pervomaisk
        ),
        District(
            "Ворошиловский",
            "Север города. Комбинация старой и новой застройки, ТЦ, крупные дороги и транспорт.",
            R.drawable.voroshilovski
        ),
        District(
            "Ленинский",
            "Развитая инфраструктура, разнообразный жилой фонд, удобный транспорт и зоны отдыха",
            R.drawable.leninski
        )
    )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val USERDATA = VKID.instance.accessToken!!.userData

        val loadingBar = view.findViewById<ProgressBar>(R.id.profileLoadingBar)
        val profileContent = view.findViewById<NestedScrollView>(R.id.profileContent)

        // Показываем загрузку и скрываем контент
        loadingBar.visibility = View.VISIBLE
        profileContent.visibility = View.GONE

        val imageView2 = view.findViewById<ImageView>(R.id.imageView2)
        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        val nameOfUser = view.findViewById<TextView>(R.id.nameOfUser)
        val rankOfUser = view.findViewById<TextView>(R.id.rankOfUser)
        val levelNum = view.findViewById<TextView>(R.id.levelNum)
        val pointsForNextLevel = view.findViewById<TextView>(R.id.pointsForNextLevel)

        // Additional views for ranking
        val rankingBlok = view.findViewById<ConstraintLayout>(R.id.rankingBlok)
        val left = view.findViewById<ConstraintLayout>(R.id.left)
        val right = view.findViewById<ConstraintLayout>(R.id.right)

        // Additional views for achivements
        val achivementsText = view.findViewById<TextView>(R.id.achivementsText)
        val achivementsBlock = view.findViewById<ConstraintLayout>(R.id.achivementsBlock)
        val imageViewAchivement = view.findViewById<ImageView>(R.id.imageViewAchivement)
        val nameOfAchivement = view.findViewById<TextView>(R.id.nameOfAchivement)
        val descriptionTextAchivement = view.findViewById<TextView>(R.id.descriptionTextAchivement)
        val nameOfDistrict = view.findViewById<TextView>(R.id.nameOfDistrict)

        // Additional views for district
        val districtLabel = view.findViewById<TextView>(R.id.districtLabel)
        val descriptionTextDistrict = view.findViewById<TextView>(R.id.descriptionTextDistrict)
        val imageViewDistrict = view.findViewById<ImageView>(R.id.imageViewDistrict)
        val districtBlock = view.findViewById<ConstraintLayout>(R.id.districtBlock)

        val dimBackground = view.findViewById<View>(R.id.dimBackground)
        val districtPopup = view.findViewById<androidx.cardview.widget.CardView>(R.id.districtPopup)
        val popupDistrictImage = view.findViewById<ImageView>(R.id.popupDistrictImage)
        val popupDistrictName = view.findViewById<TextView>(R.id.popupDistrictName)
        val popupDistrictDescription = view.findViewById<TextView>(R.id.popupDistrictDescription)
        val closeButton = view.findViewById<ConstraintLayout>(R.id.close)

        fun showDistrictPopup(
            districtName: String,
            districtDescription: String,
            imageRes: Int
        ) {
            popupDistrictName.text = districtName
            popupDistrictDescription.text = districtDescription
            popupDistrictImage.setImageResource(imageRes)

            dimBackground.visibility = View.VISIBLE
            districtPopup.visibility = View.VISIBLE

            districtPopup.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }

        fun hideDistrictPopup() {
            districtPopup.animate()
                .alpha(0f)
                .translationY(100f)
                .setDuration(600)
                .withEndAction {
                    districtPopup.visibility = View.GONE
                    dimBackground.visibility = View.GONE
                }
                .start()
        }

        closeButton.setOnClickListener {
            hideDistrictPopup()
        }

        dimBackground.setOnClickListener {
            hideDistrictPopup()
        }

        districtBlock.setOnClickListener {
            val clickedDistrictName = nameOfDistrict.text.toString()

            // Ищем объект района из списка по названию
            val district = districtList.firstOrNull { it.name == clickedDistrictName }

            if (district != null) {
                // Передаем данные из найденного района в попап
                showDistrictPopup(
                    districtName = district.name,
                    districtDescription = district.description,
                    imageRes = district.imageResId
                )
            } else {
                // Если не нашли - показываем дефолтные данные или сообщение
                showDistrictPopup(
                    districtName = clickedDistrictName,
                    districtDescription = "Описание недоступно",
                    imageRes = R.drawable.rayon // или любой дефолт
                )
            }
        }


        achivementsBlock.setOnClickListener {
            val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
            achivementsBlock.startAnimation(shake)
            achivementsBlock.alpha = 0.8f
            achivementsBlock.isClickable = false

            achivementsBlock.postDelayed({
                achivementsBlock.alpha = 1f
                achivementsBlock.isClickable = true
            }, 600)
        }


        // Имя пользователя из VKID
        nameOfUser.text = "${USERDATA.lastName} ${USERDATA.firstName}"

        // Чистим URL перед загрузкой
        val rawPhotoUrl = USERDATA.photo200 ?: ""
        val cleanPhotoUrl = extractVkAvatarUrl(rawPhotoUrl)

        // Загружаем аватар с Glide
        Glide.with(this)
            .load(cleanPhotoUrl)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(profileImage)

        // Загружаем данные пользователя из Firestore
        val db = Firebase.firestore

        val levels = listOf(
            0..49 to "Новичок",
            50..149 to "Житель",
            150..299 to "Горожанин",
            300..499 to "Исследователь",
            500..799 to "Помощник города",
            800..998 to "Волонтёр",
            999..1499 to "Знаток Ростова",
            1500..1999 to "Культурный навигатор",
            2000..2499 to "Амбассадор района",
            2500..2999 to "Городской инженер",
            3000..3999 to "Уважаемый ростовчанин",
            4000..4999 to "Городской герой",
            5000..6999 to "Почётный гражданин Ростова",
            7000..Int.MAX_VALUE to "Городская легенда"
        )

        db.collection("users")
            .document(VKID.instance.accessToken!!.userID.toString())
            .get()
            .addOnSuccessListener { document ->
                nameOfDistrict.text = document.getString("district")
                val points = document.getLong("points")?.toInt() ?: 0

                val (index, level) = levels.withIndex().firstOrNull { points in it.value.first }
                    ?.let {
                        it.index to it.value
                    } ?: (levels.size - 1 to levels.last())

                val levelName = level.second
                val levelRange = level.first

                rankOfUser.text = levelName
                levelNum.text = (index + 1).toString()
                pointsForNextLevel.text = if (points >= 999) {
                    "$points"
                } else if (levelRange.last != Int.MAX_VALUE) {
                    "$points / ${levelRange.last + 1}"
                } else {
                    "$points / ∞"
                }


                loadingBar.visibility = View.GONE
                profileContent.visibility = View.VISIBLE

                // 🧱 Контейнеры
                val blocks = listOf(
                    imageView2,
                    profileImage,
                    rankingBlok,
                    achivementsBlock,
                    districtBlock,
                )

                // 🌟 Все элементы внутри блоков
                val elements = listOf(
                    nameOfUser,
                    rankOfUser,
                    levelNum,
                    left,
                    right,
                    pointsForNextLevel,
                    achivementsText,
                    imageViewAchivement,
                    nameOfAchivement,
                    descriptionTextAchivement,
                    districtLabel,
                    imageViewDistrict,
                    nameOfDistrict,
                    descriptionTextDistrict
                )

// Подготовка блоков: сдвигаем вниз и прячем
                // Замените подготовку блоков на это:
                blocks.forEach {
                    it.alpha = 0f
                    it.translationY = 100f
                    it.visibility = View.VISIBLE // Явно установите видимость
                }

// Измените анимацию блоков:
                blocks.forEachIndexed { index, block ->
                    block.postDelayed({
                        block.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(600)
                            .setInterpolator(android.view.animation.OvershootInterpolator(1.2f)) // Увеличьте фактор
                            .start()
                    }, (index * 200).toLong())
                }

// Анимация элементов:
                elements.forEachIndexed { index, view ->
                    view.postDelayed({
                        view.animate()
                            .alpha(1f)
                            .setDuration(400)
                            .setInterpolator(android.view.animation.DecelerateInterpolator())
                            .start()
                    }, (index * 80 + 200).toLong()) // Начнётся после начала анимации блоков
                }


            }
            .addOnFailureListener {
                rankOfUser.text = "Ошибка загрузки"
                levelNum.text = "-"
                pointsForNextLevel.text = "-"
                loadingBar.visibility = View.GONE
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNav.selectedItemId = R.id.nav_home
            }
        })
    }

    private fun extractVkAvatarUrl(originalUrl: String): String {
        val baseUrl = originalUrl.substringBefore("?")
        val query = originalUrl.substringAfter("?", "")

        val updatedParams = query.split("&").map { param ->
            if (param.startsWith("cs=")) {
                "cs=400x400"
            } else {
                param
            }
        }

        return "$baseUrl?${updatedParams.joinToString("&")}"
    }
}
