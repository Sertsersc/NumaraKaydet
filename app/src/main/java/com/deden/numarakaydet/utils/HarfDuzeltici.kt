package com.deden.numarakaydet.utils

object HarfDuzeltici {

    private val karakterDonusumleri = mapOf(
        'q' to "k",
        'w' to "v",
        'x' to "k"
    )

    fun duzelt(ham: String): String {
        if (ham.isBlank()) return ham

        val temiz = ham
            .trim()
            .replace(Regex("[0-9!@#\$%^&*()_+=\\[\\]{};':\"\\\\|,.<>/?]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        val donusturulmus = StringBuilder()
        for (ch in temiz) {
            val donusum = karakterDonusumleri[ch.lowercaseChar()]
            if (donusum != null) {
                if (ch.isUpperCase()) {
                    donusturulmus.append(donusum.uppercase())
                } else {
                    donusturulmus.append(donusum)
                }
            } else {
                donusturulmus.append(ch)
            }
        }

        return donusturulmus.toString()
            .split(" ")
            .joinToString(" ") { kelime ->
                if (kelime.isEmpty()) kelime
                else kelime[0].uppercaseChar() + kelime.substring(1).lowercase()
            }
    }

    fun enIyiSonucu(alternatifler: List<String>): String {
        if (alternatifler.isEmpty()) return ""
        return duzelt(alternatifler.first())
    }
}
