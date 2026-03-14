package com.deden.numarakaydet.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deden.numarakaydet.R
import com.deden.numarakaydet.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val IZIN_KODU = 100
    private val bilinmeyenNumaralar = mutableListOf<String>()
    private lateinit var adapter: NumaraAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = NumaraAdapter(bilinmeyenNumaralar) { numara ->
            val intent = Intent(this, IsimGirActivity::class.java)
            intent.putExtra("NUMARA", numara)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        izinleriKontrolEt()
    }

    override fun onResume() {
        super.onResume()
        if (izinVarMi()) numaralariYukle()
    }

    private fun izinleriKontrolEt() {
        val gerekliIzinler = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.RECORD_AUDIO
        )
        val eksikIzinler = gerekliIzinler.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (eksikIzinler.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, eksikIzinler.toTypedArray(), IZIN_KODU)
        } else {
            numaralariYukle()
        }
    }

    private fun izinVarMi(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == IZIN_KODU) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                numaralariYukle()
            } else {
                binding.txtBilgi.text = "⚠️ Lütfen uygulamaya gerekli izinleri verin."
                binding.txtBilgi.visibility = View.VISIBLE
            }
        }
    }

    private fun numaralariYukle() {
        bilinmeyenNumaralar.clear()

        val kaydedilenler = kaydedilenNumaralariGetir()
        val gorulmusNumaralar = linkedSetOf<String>()

        // Tip filtresi YOK — tüm aramalar (gelen + cevapsız + giden)
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE
        )
        val sortOrder = "${CallLog.Calls.DATE} DESC"

        val cursor: Cursor? = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val numIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
            val tipIdx = it.getColumnIndex(CallLog.Calls.TYPE)

            while (it.moveToNext()) {
                val numara = it.getString(numIdx) ?: continue
                val tip = it.getInt(tipIdx)

                // Sadece gelen ve cevapsız aramaları göster (giden hariç)
                if (tip != CallLog.Calls.INCOMING_TYPE &&
                    tip != CallLog.Calls.MISSED_TYPE) continue

                // Numarayı normalize et
                val temiz = numara.trim()
                if (temiz.isEmpty() || temiz == "-1" || temiz == "unknown") continue

                // Kayıtlı değilse ekle
                val normalized = normalizeNumara(temiz)
                if (!kaydedilenler.any { kayitli ->
                    normalizeNumara(kayitli) == normalized
                }) {
                    gorulmusNumaralar.add(temiz)
                }
            }
        }

        bilinmeyenNumaralar.addAll(gorulmusNumaralar.take(50))
        adapter.notifyDataSetChanged()

        if (bilinmeyenNumaralar.isEmpty()) {
            binding.txtBilgi.text = "✅ Tüm arayan numaralar zaten kayıtlı!"
            binding.txtBilgi.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.txtBilgi.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    // Numaraları karşılaştırmak için sadece rakamları al, ülke kodu farkını yok say
    private fun normalizeNumara(numara: String): String {
        val sadeceSayi = numara.replace(Regex("[^0-9]"), "")
        // Son 10 haneyi al (Türkiye numaraları için yeterli)
        return if (sadeceSayi.length >= 10) sadeceSayi.takeLast(10) else sadeceSayi
    }

    private fun kaydedilenNumaralariGetir(): Set<String> {
        val kaydedilenler = mutableSetOf<String>()
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, null, null, null
        )
        cursor?.use {
            val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val num = it.getString(numIdx) ?: continue
                kaydedilenler.add(num)
            }
        }
        return kaydedilenler
    }
}

class NumaraAdapter(
    private val liste: List<String>,
    private val tiklamaCallback: (String) -> Unit
) : RecyclerView.Adapter<NumaraAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNumara: TextView = itemView.findViewById(R.id.txtNumara)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_numara, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.txtNumara.text = liste[position]
        holder.itemView.setOnClickListener { tiklamaCallback(liste[position]) }
    }

    override fun getItemCount() = liste.size
}
