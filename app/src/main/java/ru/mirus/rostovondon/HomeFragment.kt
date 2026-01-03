package ru.mirus.rostovondon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vk.id.VKID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// HomeFragment.kt
class HomeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val searchBar = view.findViewById<View>(R.id.search_bar)
        val newsBlock = view.findViewById<View>(R.id.newsBlock)
        val afisha = view.findViewById<View>(R.id.afisha)
        val addButton = view.findViewById<View>(R.id.addButton)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_view)
        val user_icon = view.findViewById<ImageView>(R.id.user_icon)

        Glide.with(requireActivity())
            .load(VKID.instance.accessToken!!.userData.photo200)
            .transform(CircleCrop())
            .into(user_icon)

        // Блоки (подпрыгивают + fade)
        val blocks = listOf(searchBar, newsBlock)
        val blocksFaded = listOf(afisha, addButton)
        blocks.forEach {
            it.alpha = 0f
            it.translationY = 100f
        }
        blocksFaded.forEach {
            it.alpha = 0f
            it.translationY = 40f
        }

        // Элементы (только fade)
        val elements = listOf(recycler)
        elements.forEach {
            it.alpha = 0f
        }

        // Анимация для блоков
        blocks.forEachIndexed { index, block ->
            block.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay((index * 150).toLong()) // по очереди
                .setInterpolator(OvershootInterpolator())
                .start()
        }

        blocksFaded.forEachIndexed { index, blocksFaded ->
            blocksFaded.animate()
                .alpha(0.4f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay((index * 150).toLong()) // по очереди
                .setInterpolator(OvershootInterpolator())
                .start()
        }

        // Анимация для элементов (позже блоков)
        elements.forEachIndexed { index, v ->
            v.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay((index * 150).toLong() + 600)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        loadNews()
        return view
    }


    private fun loadNews() {
        CoroutineScope(Dispatchers.IO).launch {
            val news = RssParser.fetchNews("https://161.ru/rss-feeds/rss.xml")
            withContext(Dispatchers.Main) {
                adapter = NewsAdapter(news)
                recyclerView.adapter = adapter
            }
        }
    }
}
