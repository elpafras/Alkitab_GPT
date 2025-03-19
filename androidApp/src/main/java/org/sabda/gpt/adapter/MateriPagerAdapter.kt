package org.sabda.gpt.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.sabda.gpt.fragment.PdfFragment
import org.sabda.gpt.fragment.YoutubeFragment

class MateriPagerAdapter(
    activity: FragmentActivity,
    private val youtubeUrl: String?,
    private val pdfUrl: String?,
    private val deskripsi: String,
    private val onContentLoaded: (String) -> Unit
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2 // Halaman YouTube & PDF

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> YoutubeFragment.newInstance(youtubeUrl ?: "", deskripsi, onContentLoaded)
            1 -> PdfFragment.newInstance(pdfUrl ?: "", deskripsi, onContentLoaded)
            else -> throw IllegalArgumentException("Posisi tidak valid")
        }
    }
}