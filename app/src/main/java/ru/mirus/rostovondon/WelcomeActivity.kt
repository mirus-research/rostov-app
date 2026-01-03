package ru.mirus.rostovondon

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.vk.id.VKID
import com.vk.id.onetap.xml.OneTapBottomSheet
import com.google.firebase.firestore.FirebaseFirestore


class WelcomeActivity : AppCompatActivity() {
    private lateinit var layouts: List<ConstraintLayout>

    object VKIDHelper {
        var isInitialized = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("USER", MODE_PRIVATE)
        if (!VKIDHelper.isInitialized) {
            VKID.init(this)
            VKIDHelper.isInitialized = true
        }
        if (sharedPref.getBoolean("logged", false) && VKID.instance.accessToken != null) {
            startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
        } else {
            setContentView(R.layout.activity_welcome)

            layouts = listOf(
                findViewById(R.id.login),
                findViewById(R.id.login1),
                findViewById(R.id.login2),
                findViewById(R.id.login3),
                findViewById(R.id.login4),
                findViewById(R.id.login5)
            )
            val hren = findViewById<TextView>(R.id.byCode)
            val firestore = Firebase.firestore
            firestore.collection("terms").document("tools")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val showRuStore = document.getBoolean("showRuStore") ?: false
                        if (showRuStore) {
                            hren.visibility = View.VISIBLE
                        }
                    } else {
                        Log.d("Firestore", "Документ не существует")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Ошибка при получении документа", exception)
                }

            layouts.forEachIndexed { index, layout ->
                layout.findViewById<View>(R.id.next)?.setOnClickListener {
                    if (index < layouts.lastIndex) {
                        switchToLayout(index, index + 1)
                    } else {
                        val vkidOneTapBottomSheet =
                            findViewById<OneTapBottomSheet>(R.id.vkid_bottom_sheet)
                        vkidOneTapBottomSheet.setCallbacks(
                            onAuth = { oAuth, token ->
                                startActivity(Intent(this, DistrictActivity::class.java))
                                overridePendingTransition(
                                    R.anim.from_left,
                                    R.anim.to_left
                                )
                            }, onFail = { oAuth, fail ->
                                Log.e("HUI", "Ошибка")
                            })

                        vkidOneTapBottomSheet.show()
                    }
                }
            }
        }


    }

    private fun switchToLayout(fromIndex: Int, toIndex: Int) {
        val fromLayout = layouts[fromIndex]
        val toLayout = layouts[toIndex]

        val outAnim = AnimationUtils.loadAnimation(this, R.anim.to_left)
        val inAnim = AnimationUtils.loadAnimation(this, R.anim.from_left)

        fromLayout.startAnimation(outAnim)
        fromLayout.visibility = View.GONE

        toLayout.visibility = View.VISIBLE
        toLayout.startAnimation(inAnim)
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
}