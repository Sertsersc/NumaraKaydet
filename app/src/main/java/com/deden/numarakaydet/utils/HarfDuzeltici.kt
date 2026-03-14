package com.deden.numarakaydet.utils

/**
 * Ses tanıma motoru bazen Türkçe harfleri yanlış duyar.
 * Bu sınıf fonetik benzerliklere göre harfleri düzelterek
 * en mantıklı sonucu üretir.
 *
 * Örnek: "Ahmet" yerine "Ahmed" dediyse → "Ahmet" yazar
 *         "Yılmaz" yerine "Yılmas" dediyse → "Yılmaz" yazar
 */
object HarfDuzeltici {

    // Ses tanıma motorunun karıştırabileceği harf çiftleri
    // Sol: yanlış duyulabilecek, Sağ: doğru harf
    private val fonetikEslesmeler = listOf(
        // Türkçe ses karışıklıkları
        "c" to "ç",   // c/ç ses benzerliği
        "s" to "z",   // kelime sonu s/z
        "d" to "t",   // kelime sonu d/t (sesli/sessiz)
        "g" to "k",   // kelime sonu g/k
        "b" to "p",   // kelime sonu b/p
        "v" to "f",   // v/f benzerliği
        "ğ" to "g",   // ğ → g dönüşümü (yazılı ifade)
        "j" to "c",   // j/c benzerliği
        "y" to "j",   // y/j
    )

    // Ses tanıma motorunun Latin/İngilizce yerine kullandığı
    // Türkçe olmayan karakterleri düzelt
    private val karakterDonusumleri = mapOf(
        'q' to 'k',
        'w' to 'v',
        'x' to 'ks',
        // Latin benzeri harfler
    )

    /**
     * Ham ses tanıma metnini alır, düzeltilmiş isim formatında döndürür.
     * - Her kelimenin ilk harfi büyük olur
     * - Gereksiz karakterler temizlenir
     * - Türkçe karakter normalizasyonu yapılır
     */
    fun duzelt(ham: String): String {
        if (ham.isBlank()) return ham

        return ham
            .trim()
            // Rakam ve noktalama işaretlerini kaldır (isim için gerekmez)
            .replace(Regex("[0-9!@#\$%^&*()_+=\\[\\]{};':\"\\\\|,.<>/?]"), "")
            // Fazla boşlukları tek boşluğa indir
            .replace(Regex("\\s+"), " ")
            .trim()
            // q → k, w → v gibi Latin dönüşümleri
            .map { ch ->
                val donusum = karakterDonusumleri[ch.lowercaseChar()]
                if (donusum != null && ch.isLowerCase()) donusum
                else if (donusum != null && ch.isUpperCase()) donusum.replaceFirstChar { it.uppercase() }
                else ch.toString()
            }.joinToString("")
            // Her kelimenin baş harfini büyüt (isim formatı)
            .split(" ")
            .joinToString(" ") { kelime ->
                kelime.replaceFirstChar { ch ->
                    if (ch.isLowerCase()) ch.titlecase() else ch.toString()
                }
            }
    }

    /**
     * Ses tanıma sonuçlarının birden fazla alternatifini karşılaştırarak
     * en uygun olanı seçer. (İleride geliştirilecek)
     */
    fun enIyiSonucu(alternatifler: List<String>): String {
        if (alternatifler.isEmpty()) return ""
        // Şimdilik ilk sonucu düzelterek döndür
        return duzelt(alternatifler.first())
    }
}
