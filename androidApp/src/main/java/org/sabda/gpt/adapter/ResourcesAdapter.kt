package org.sabda.gpt.adapter

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
    private val resourceList: List<ResourceData>,
    private val onItemClick: (ResourceData) -> Unit
) : RecyclerView.Adapter<ResourcesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.resources_recycler, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resource = resourceList[position]
        holder.title.text = resource.title
        holder.image.setImageResource(resource.imageRes)

        val context = holder.itemView.context

        val deskripsi = when (resource.title) {
            "Seminar: AI & Alkitab" -> context.getString(R.string.teksartikelaidanalkitab)
            "Seminar: Alkitab GPT" -> context.getString(R.string.teksartikelalkitab_gpt)
            "Metode AI Squared" -> context.getString(R.string.teksartikelmetode_ai_squared)
            "Metode F.O.K.U.S." -> context.getString(R.string.teksartikelmetode_f_o_k_u_s)
            "Bible + GPT" -> context.getString(R.string.teksartikelbible_gpt)
            else -> "Deskripsi tidak tersedia untuk artikel ini."
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, Materi::class.java).apply {
                putExtra("TITLE", resource.title)
                putExtra("YOUTUBE_ID", resource.youtubeId)
                putExtra("PDF_URL", resource.pdfUrl)
                putExtra("DESKRIPSI", deskripsi)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = resourceList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.resourcesTextView)
        val image: ImageView = itemView.findViewById(R.id.resourcesImage)
    }
}