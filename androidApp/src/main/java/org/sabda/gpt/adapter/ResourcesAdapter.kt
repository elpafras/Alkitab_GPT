package org.sabda.gpt.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.sabda.gpt.Materi
import org.sabda.gpt.R
import org.sabda.gpt.model.ResourceData

class ResourcesAdapter(
    private val resourceList: List<ResourceData>
) : RecyclerView.Adapter<ResourcesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.resources_recycler, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resource = resourceList[position]
        val context = holder.itemView.context

        holder.title.text = resource.title
        holder.image.setImageResource(resource.imageRes)

        val description = getDescriptionFromTitle(resource.title, context)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, Materi::class.java).apply {
                putExtra("TITLE", resource.title)
                putExtra("YOUTUBE_ID", resource.youtubeId)
                putExtra("PDF_URL", resource.pdfUrl)
                putExtra("DESKRIPSI", description)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = resourceList.size

    private fun getDescriptionFromTitle(title: String, context: Context): String {
        val matchedEntry = descriptionMap.entries.find {
            context.getString(it.key) == title
        }
        return matchedEntry?.value?.let { context.getString(it) }
            ?: context.getString(R.string.deskripsi_tidak_tersedia)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.resourcesTextView)
        val image: ImageView = itemView.findViewById(R.id.resourcesImage)
    }

    companion object {
        private val descriptionMap = mapOf(
            R.string.aidanalkitab to R.string.teksartikelaidanalkitab,
            R.string.alkitab_gpt to R.string.teksartikelalkitab_gpt,
            R.string.metode_ai_squared to R.string.teksartikelmetode_ai_squared,
            R.string.metode_f_o_k_u_s to R.string.teksartikelmetode_f_o_k_u_s,
            R.string.bible_gpt to R.string.teksartikelbible_gpt
        )
    }
}