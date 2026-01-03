package ru.mirus.rostovondon

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), NoSignalFragment.NoSignalRetryListener {

    lateinit var bottomNav: BottomNavigationView
    private var currentSelectedItemId: Int = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        bottomNav = findViewById(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            if (isInternetAvailable()) {
                showHomeFragment()
            } else {
                showNoSignalFragment()
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == currentSelectedItemId) return@setOnItemSelectedListener false

            val newFragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_map -> {
                    val fragment = MapFragment.newInstance(MapFragment.pendingArgs)
                    MapFragment.pendingArgs = null
                    fragment
                }
                R.id.nav_services -> ServicesFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> null
            }

            newFragment?.let {
                val oldIndex = getMenuItemIndex(currentSelectedItemId)
                val newIndex = getMenuItemIndex(item.itemId)
                val isToRight = newIndex > oldIndex

                val transaction = supportFragmentManager.beginTransaction()
                if (isToRight) {
                    transaction.setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                } else {
                    transaction.setCustomAnimations(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                }

                transaction.replace(R.id.fragment_container, it).commit()
                currentSelectedItemId = item.itemId
                true
            } ?: false
        }

        // Назад
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentSelectedItemId != R.id.nav_home) {
                    bottomNav.selectedItemId = R.id.nav_home
                    currentSelectedItemId = R.id.nav_home
                    showHomeFragment()
                } else {
                    finishAffinity()
                }
            }
        })
    }

    private fun showHomeFragment() {
        bottomNav.visibility = BottomNavigationView.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
        currentSelectedItemId = R.id.nav_home
    }

    private fun showNoSignalFragment() {
        bottomNav.visibility = BottomNavigationView.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, NoSignalFragment())
            .commit()
    }

    private fun getMenuItemIndex(itemId: Int): Int {
        val menu = bottomNav.menu
        for (i in 0 until menu.size()) {
            if (menu.getItem(i).itemId == itemId) return i
        }
        return -1
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // 🔹 Callback от NoSignalFragment
    override fun onRetryClicked() {
        if (isInternetAvailable()) {
            showHomeFragment()
        } else {
            // можно добавить Toast("Нет соединения")
        }
    }

    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val mainLayout = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            val bottomPadding = when {
                ime.bottom > 0 -> ime.bottom // если клавиатура открыта
                systemBars.bottom > 100 -> systemBars.bottom // если панель навигации большая (например, на раскладушке)
                else -> 0 // иначе — ничего не добавляем
            }

            v.setPadding(
                0, // сверху ничего не добавляем — фуллскрин остаётся
                0,
                0,
                bottomPadding
            )

            // возвращаем insets, чтобы они не "съедались"
            insets
        }
    }
}
