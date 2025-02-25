package org.sabda.gpt.data.local

import java.util.Random

class ExamplePrompt {

    private val prompts = listOf(
        "Apa itu roti manna?",
        "Siapakah nama anak Nuh?",
        "Dimana tempat tinggal Ayub?",
        "Kapan surat Roma ditulis oleh Paulus?",
        "Bagaimana cara Daud mengalahkan Goliat?",
        "Mengapa Yudas Iskariot mengkhianati Yesus?",
        "Berapa tahun Yesus mulai menjadi pengajar?",
        "Beri penjelasan mengenai Filipi 4:6",
        "Beri penjelasan mengenai Imannuel",
        "Sebutkan nama murid Tuhan Yesus",
        "Sebutkan sifat-sifat Allah",
        "Penggalian studi kata \"Kasih dan Pengharapan\" menurut bahasa asli"
    )

    private val random = Random()

    fun getRandomPrompt(): String {
        return prompts[random.nextInt(prompts.size)]
    }
}