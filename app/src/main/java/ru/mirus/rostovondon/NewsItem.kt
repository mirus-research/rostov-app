package ru.mirus.rostovondon

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import ru.mirus.rostovondon.databinding.NewsItemBinding
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.*
import com.squareup.picasso.Transformation

data class NewsItem(
    val title: String,
    val link: String,
    val pubDate: String,
    val category: String,
    val imageUrl: String?,
    val author: String?
)

object RssParser {
    fun fetchNews(rssUrl: String): List<NewsItem> {
        val result = mutableListOf<NewsItem>()
        val url = URL(rssUrl)
        val inputStream = url.openConnection().getInputStream()

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var title: String? = null
        var link: String? = null
        var pubDate: String? = null
        var category: String? = null
        var imageUrl: String? = null
        var author: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tagName) {
                        "title" -> title = parser.nextText()
                        "link" -> link = parser.nextText()
                        "pubDate" -> pubDate = parser.nextText()
                        "category" -> category = parser.nextText()
                        "author" -> author = parser.nextText()
                        "enclosure" -> {
                            if (parser.getAttributeValue(null, "type")
                                    ?.startsWith("image") == true
                            ) {
                                imageUrl = parser.getAttributeValue(null, "url")
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> if (tagName == "item") {
                    result.add(
                        NewsItem(
                            title ?: "",
                            link ?: "",
                            formatDate(pubDate),
                            category ?: "Новости",
                            imageUrl,
                            author
                        )
                    )
                    title = null
                    link = null
                    pubDate = null
                    category = null
                    imageUrl = null
                    author = null
                }
            }
            eventType = parser.next()
        }
        return result
    }

    private fun formatDate(pubDate: String?): String {
        if (pubDate == null) return ""
        return try {
            val parser = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
            val date = parser.parse(pubDate)
            SimpleDateFormat("HH:mm", Locale("ru")).format(date!!)
        } catch (e: Exception) {
            pubDate
        }
    }
}

class NewsAdapter(private val newsList: List<NewsItem>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(val binding: NewsItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = NewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = newsList[position]
        with(holder.binding) {
            newsTitle.text = item.title
            dateText.text = item.pubDate
            pointer.text = "Источник: 161.ru"

            categoryText.text = item.category.ifBlank { "Новости" }

            val iconRes = when (item.category.lowercase()) {
                "город" -> R.drawable.services_home_buildings
                "культура" -> R.drawable.services_dosug_cinema
                "здоровье" -> R.drawable.services_medicine_doctor
                "авто" -> R.drawable.services_auto_1
                "семья" -> R.drawable.services_childs_kindergarden
                "развлечения" -> R.drawable.services_dosug_cinema
                else -> R.drawable.services_home_buildings
            }
            img.setImageResource(iconRes)

            Picasso.get()
                .load(item.imageUrl)
                .placeholder(R.drawable.donotload)
                .error(R.drawable.errorphoto)
                .transform(RoundedPercentTransformation(0.1f)) // одинаковый визуальный радиус
                .into(newsImage)

            root.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                root.context.startActivity(intent)
            }
        }

    }

    override fun getItemCount(): Int = newsList.size
}

class RoundedPercentTransformation(private val percent: Float) : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        val radius = source.width * percent
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rectF = RectF(0f, 0f, source.width.toFloat(), source.height.toFloat())

        canvas.drawRoundRect(rectF, radius, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, 0f, 0f, paint)

        source.recycle()
        return output
    }

    override fun key(): String = "rounded_percent(percent=$percent)"
}
