package org.sabda.gpt.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sabda.gpt.R
import org.sabda.gpt.data.local.ChatMetaData
import org.sabda.gpt.data.local.ChatbotDao

class ChatPreviewAdapter(
    private var chats: List<ChatMetaData>,
    private val lifecyclescope: LifecycleCoroutineScope,
    private val messageDao: ChatbotDao,
    private val onChatClick: (Long) -> Unit
) : RecyclerView.Adapter<ChatPreviewAdapter.ChatPreviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatPreviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_preview, parent, false)
        return ChatPreviewViewHolder(view)
    }

    override fun getItemCount(): Int = chats.size

    override fun onBindViewHolder(holder: ChatPreviewViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    fun updateData(newPreviews: List<ChatMetaData>) {
        chats = newPreviews
        notifyDataSetChanged()
    }

    private fun showDeleteConfirmationDialog(position: Int, context: android.content.Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_delete)
            .setMessage(R.string.dialog_message_delete)
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                deleteChat(position)
            }
            .setNegativeButton(R.string.dialog_no, null)
            .show()
    }

    private fun deleteChat(position: Int) {
        val chatId = chats[position].chatId
        val updatedChats = chats.toMutableList().apply { removeAt(position) }
        chats = updatedChats
        notifyItemRemoved(position)

        lifecyclescope.launch(Dispatchers.IO) {
            messageDao.deleteChatMetaData(chatId)
        }
    }

    inner class ChatPreviewViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chatPreviewText: TextView = itemView.findViewById(R.id.textViewChatPreview)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onChatClick(chats[position].chatId)
                }
            }

            itemView.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showPopupMenu(position, itemView)
                }
                true
            }
        }

        fun bind(preview: ChatMetaData){
            chatPreviewText.text = preview.title
        }

        private fun showPopupMenu(position: Int, view: View) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.chat_preview_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit_title -> {
                        showEditTitleDialog(position)
                        true
                    }
                    R.id.menu_delete -> {
                        showDeleteConfirmationDialog(position, view.context)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun showEditTitleDialog(position: Int) {
            val context = itemView.context
            val chat = chats[position]

            val editText = android.widget.EditText(context).apply {
                setText(chat.title)
            }

            AlertDialog.Builder(context)
                .setTitle("Edit Judul")
                .setView(editText)
                .setPositiveButton("Simpan") { _, _ ->
                    val newTitle = editText.text.toString()
                    if (newTitle.isNotBlank()) {
                        val updatedChats = chats.toMutableList()
                        updatedChats[position] = chat.copy(title = newTitle)
                        chats = updatedChats
                        notifyItemChanged(position)

                        lifecyclescope.launch(Dispatchers.IO) {
                            messageDao.updateChatMetaDataTitle(chat.chatId, newTitle)
                        }
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }
}