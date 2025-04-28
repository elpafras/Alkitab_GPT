package org.sabda.gpt.adapter

import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.sabda.gpt.R
import org.sabda.gpt.model.ChatMessage
import org.sabda.gpt.utility.HtmlUtil

class ChatAdapter(private val messageData: List<ChatMessage> ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2

        private fun stripHtml(html: String): String {
            val cleanText = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
            return cleanText.replace(Regex("\\s+"), " ").trim()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageData[position].isSent) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SENT -> SentViewHolder(inflater.inflate(R.layout.item_send, parent, false))
            else -> ReceiveViewHolder(inflater.inflate(R.layout.item_receive, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageData[position]
        when (holder) {
            is SentViewHolder -> holder.bind(message)
            is ReceiveViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messageData.size


    class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewMessage: TextView = view.findViewById(R.id.textViewMessage)

        fun bind(data: ChatMessage) {
            textViewMessage.text = data.text
        }
    }

    class ReceiveViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewMessage: TextView = view.findViewById(R.id.textViewMessageReceive)

        fun bind(data: ChatMessage) {
            textViewMessage.text = stripHtml(HtmlUtil.replaceSpecificTags(data.text).toString())
            Log.d("chatadapter", "bind - received message edited: ${textViewMessage.text} ")
        }
    }
}