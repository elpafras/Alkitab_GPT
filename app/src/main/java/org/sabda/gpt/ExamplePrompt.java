package org.sabda.gpt;

import java.util.ArrayList;
import java.util.Random;

public class ExamplePrompt {

    ArrayList<String> prompt;
    Random random;

    public ExamplePrompt(){
        prompt = new ArrayList<>();
        random = new Random();
        initializePrompt();
    }

    private void initializePrompt() {
        prompt.add("Apa itu roti manna?");
        prompt.add("Siapakah nama anak Nuh?");
        prompt.add("Dimana tempat tinggal Ayub?");
        prompt.add("Kapan surat Roma ditulis oleh Paulus?");
        prompt.add("Bagaimana cara Daud mengalahkan Goliat?");
        prompt.add("Mengapa Yudas Iskariot mengkhianati Yesus?");
        prompt.add("Berapa tahun Yesus mulai menjadi pengajar?");
        prompt.add("Beri penjelasan mengenai Filipi 4:6");
        prompt.add("Beri penjelasan mengenai Imannuel");
        prompt.add("Sebutkan nama murid Tuhan Yesus");
        prompt.add("Sebutkan sifat-sifat Allah");
        prompt.add("Penggalian studi kata \"Kasih dan Pengharapan\" menurut bahasa asli");
    }

    public String getRandomPrompt(){
        int randomIndex = random.nextInt(prompt.size());
        return prompt.get(randomIndex);
    }
}
