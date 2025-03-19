package org.sabda.gpt.model

data class ResourceData(
    val id: String,
    val title: String,
    val imageRes: Int,
    val youtubeId: String,
    val pdfUrl: String,
    val deskripsi: String
)