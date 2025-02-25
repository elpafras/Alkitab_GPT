package org.sabda.gpt.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.sabda.gpt.model.ChatbotData
import org.sabda.gpt.R

class ChatAdapter(private val chatbotData: List<ChatbotData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatbotData[position].isSent) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_send, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_receive, parent, false)
            ReceiveViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatbotData = chatbotData[position]
        if (holder is SentViewHolder) {
            holder.text1.text = chatbotData.text
        } else if (holder is ReceiveViewHolder) {
            holder.text2.text = chatbotData.text
        }
    }

    override fun getItemCount(): Int = chatbotData.size

    class SentViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val text1: TextView = view.findViewById(R.id.textViewMessage)
    }

    class ReceiveViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text2: TextView = view.findViewById(R.id.textViewMessageReceive)
    }


}