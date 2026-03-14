package com.deden.numarakaydet.ui

import android.content.ContentProviderOperation
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.deden.numarakaydet.databinding.ActivityIsimGirBinding
import com.deden.numarakaydet.utils.HarfDuzeltici
import java.util.Locale

class IsimGirActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIsimGirBinding
    private lateinit var speechRecognizer: SpeechRecognizer
    private var numara: String = ""
    private var dinliyorMu = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIsimGirBinding.inflate(layoutInflater)
        setContentView(binding.root)

        numara = intent.getStringExtra("NUMARA") ?: ""
        binding.txtNumaraGoster.text = numara

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(dinleyici)

        // İsim kutusuna tıklayınca otomatik ses başlasın
        binding.edtIsim.setOnClickListener {
            if (!dinliyorMu) sesDinlemeBaslat()
        }

        binding.edtIsim.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !dinliyorMu) sesDinlemeBaslat()
        }

        binding.btnMikrofon.setOnClickListener {
            if (dinliyorMu) {
                speechRecognizer.stopListening()
                dinliyorMu = false
                mikrofonDurumu(false)
            } else {
                sesDinlemeBaslat()
            }
        }

        binding.btnKaydet.setOnClickListener {
            val isim = binding.edtIsim.text.toString().trim()
            if (isim.isEmpty()) {
                Toast.makeText(this, "Lütfen bir isim girin veya söyleyin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (numara.isEmpty()) {
                Toast.makeText(this, "Numara bulunamadı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            simKartaKaydet(isim, numara)
        }

        binding.btnGeriDon.setOnClickListener {
            finish()
        }
    }

    private fun sesDinlemeBaslat() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Bu cihazda ses tanıma yok!", Toast.LENGTH_LONG).show()
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // Güçlü ses tanıma: uzun sessizliğe dayanıklı
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
        }
        speechRecognizer.startListening(intent)
        dinliyorMu = true
        mikrofonDurumu(true)
    }

    private val dinleyici = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) {
            binding.txtSesDurumu.text = "🎙️ Şimdi söyleyin..."
            binding.txtSesDurumu.visibility = View.VISIBLE
        }

        override fun onBeginningOfSpeech() {
            binding.txtSesDurumu.text = "🔊 Dinliyorum..."
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Ses seviyesi animasyonu
            val olcek = 1.0f + (rmsdB / 20.0f).coerceIn(0f, 0.5f)
            binding.btnMikrofon.animate().scaleX(olcek).scaleY(olcek).setDuration(100).start()
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            dinliyorMu = false
            mikrofonDurumu(false)
            binding.btnMikrofon.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }

        override fun onError(error: Int) {
            dinliyorMu = false
            mikrofonDurumu(false)
            binding.btnMikrofon.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            val mesaj = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "Ses anlaşılamadı, tekrar deneyin"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Ses algılanamadı"
                SpeechRecognizer.ERROR_NETWORK -> "İnternet bağlantısı gerekli"
                SpeechRecognizer.ERROR_AUDIO -> "Ses kaydı hatası"
                else -> "Hata oluştu, tekrar deneyin"
            }
            binding.txtSesDurumu.text = "⚠️ $mesaj"
        }

        override fun onResults(results: Bundle?) {
            val sonuclar = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!sonuclar.isNullOrEmpty()) {
                // En iyi sonucu al ve harf düzelt
                val ham = sonuclar[0]
                val duzeltilmis = HarfDuzeltici.duzelt(ham)
                binding.edtIsim.setText(duzeltilmis)
                binding.edtIsim.setSelection(duzeltilmis.length)
                binding.txtSesDurumu.text = "✅ Anlaşıldı: \"$duzeltilmis\""
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val sonuclar = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!sonuclar.isNullOrEmpty()) {
                val gecici = HarfDuzeltici.duzelt(sonuclar[0])
                binding.edtIsim.setText(gecici)
                binding.edtIsim.setSelection(gecici.length)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun mikrofonDurumu(aktif: Boolean) {
        if (aktif) {
            binding.btnMikrofon.text = "⏹ Durdur"
            binding.btnMikrofon.setBackgroundColor(resources.getColor(android.R.color.holo_red_light, theme))
        } else {
            binding.btnMikrofon.text = "🎙️ Tekrar Söyle"
            binding.btnMikrofon.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark, theme))
            binding.txtSesDurumu.visibility = View.GONE
        }
    }

    private fun simKartaKaydet(isim: String, telefon: String) {
        try {
            val ops = arrayListOf<ContentProviderOperation>()

            // SIM karta kaydet (AccountType = null = telefon/SIM)
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            // İsim
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, isim)
                    .build()
            )

            // Telefon numarası
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, telefon)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )

            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)

            Toast.makeText(this, "✅ \"$isim\" başarıyla kaydedildi!", Toast.LENGTH_LONG).show()
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "❌ Kayıt başarısız: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
