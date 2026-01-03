package ru.mirus.rostovondon

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.firestore.FirebaseFirestore
import com.vk.id.VKID
import kotlin.math.abs

class DistrictActivity : AppCompatActivity() {
    private lateinit var districtList: List<District>
    private lateinit var viewPager: ViewPager2
    private lateinit var buttonNext: ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_district)
        val sharedPref = getSharedPreferences("USER", MODE_PRIVATE)

        viewPager = findViewById(R.id.viewPager)
        buttonNext = findViewById(R.id.next)

        districtList = listOf(
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

        viewPager.adapter = DistrictAdapter(districtList)
        viewPager.offscreenPageLimit = 3

        // Анимация прокрутки
        viewPager.setPageTransformer { page, position ->
            val scale = 0.85f + (1 - abs(position)) * 0.15f
            page.scaleY = scale
            page.alpha = 0.7f + (1 - abs(position)) * 0.3f
        }


        buttonNext.setOnClickListener {
            val currentDistrict = districtList[viewPager.currentItem]
            addUserIfNotExists(
                    userId = VKID.instance.accessToken!!.userID.toString(),
                    ava = VKID.instance.accessToken!!.userData.photo200.toString(),
                    badges = listOf("first_arrival"),
                    district = currentDistrict.name,
                    name = VKID.instance.accessToken!!.userData.firstName,
                    lastname = VKID.instance.accessToken!!.userData.lastName,
                    points = 0,
                    onSuccess = {
                        sharedPref.edit { putBoolean("logged", true) }
                        startActivity(
                            Intent(
                                this@DistrictActivity,
                                MainActivity::class.java
                            )
                        )
                        overridePendingTransition(
                            R.anim.from_left,
                            R.anim.to_left
                        )
                    },
                    onAlreadyExists = {
                        sharedPref.edit { putBoolean("logged", true) }
                        startActivity(
                            Intent(
                                this@DistrictActivity,
                                MainActivity::class.java
                            )
                        )
                        overridePendingTransition(
                            R.anim.from_left,
                            R.anim.to_left
                        )
                    },
                    onFailure = { error ->
                        println("❌ Ошибка при добавлении: ${error.message}")
                    }
                )
            }
    }

    private fun addUserIfNotExists(
        userId: String,
        ava: String,
        badges: List<String>,
        district: String,
        name: String,
        lastname: String,
        points: Int,
        onSuccess: (() -> Unit)? = null,
        onAlreadyExists: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onAlreadyExists?.invoke()
                    return@addOnSuccessListener
                }

                val userData = hashMapOf(
                    "ava" to ava,
                    "badges" to badges,
                    "district" to district,
                    "name" to name,
                    "lastname" to lastname,
                    "points" to points
                )

                userRef.set(userData)
                    .addOnSuccessListener {
                        onSuccess?.invoke()
                    }
                    .addOnFailureListener { e ->
                        onFailure?.invoke(e)
                    }
            }
            .addOnFailureListener { e ->
                onFailure?.invoke(e)
            }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }
}
