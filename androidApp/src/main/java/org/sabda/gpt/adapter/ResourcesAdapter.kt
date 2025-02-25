package org.sabda.gpt.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.sabda.gpt.model.ResourceData
import org.sabda.gpt.R

class ResourcesAdapter(private val resourceData: List<ResourceData>, private val onClick: (String) -> Unit ):
    RecyclerView.Adapter<ResourcesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.resources_recycler, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resource = resourceData[position]
        holder.bind(resource)

    }

    override fun getItemCount(): Int = resourceData.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(resourceData: ResourceData){
            val titleTextView = itemView.findViewById<TextView>(R.id.resourcesTextView)
            titleTextView.text = resourceData.title

            val imageView = itemView.findViewById<ImageView>(R.id.resourcesImage)
            imageView.setImageResource(resourceData.imageResId)


            itemView.setOnClickListener {
                onClick(resourceData.id)
            }

        }
    }
}