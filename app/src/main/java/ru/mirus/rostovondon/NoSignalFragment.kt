package ru.mirus.rostovondon

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment

class NoSignalFragment : Fragment() {

    private var retryListener: NoSignalRetryListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NoSignalRetryListener) {
            retryListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        retryListener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_no_net, container, false)

        val imageView = view.findViewById<View>(R.id.imageView)
        val textView = view.findViewById<View>(R.id.textView)
        val buttonBlock = view.findViewById<View>(R.id.next)

        // Анимации
        val blocks = listOf(imageView, textView)
        blocks.forEach {
            it.alpha = 0f
            it.translationY = 100f
        }

        buttonBlock.alpha = 0f
        buttonBlock.translationY = 40f

        blocks.forEachIndexed { index, block ->
            block.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay((index * 150).toLong())
                .setInterpolator(OvershootInterpolator())
                .start()
        }

        buttonBlock.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(400)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // 🔹 Кнопка "Повторить"
        buttonBlock.setOnClickListener {
            retryListener?.onRetryClicked()
        }

        return view
    }

    interface NoSignalRetryListener {
        fun onRetryClicked()
    }
}
