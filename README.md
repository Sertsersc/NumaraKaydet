# 📱 Numara Kaydet — Deden için ♥

Bilinmeyen arayanları kolayca rehbere kaydetmek için tasarlanmış, **ses tanımalı** Android uygulaması.

---

## 🚀 GitHub Actions ile APK Oluşturma

### Adım 1: Repo oluştur
1. GitHub'da yeni bir repo aç (örn: `numara-kaydet`)
2. Bu klasördeki **tüm dosyaları** repoya yükle

### Adım 2: Gradle Wrapper ekle
GitHub'a push etmeden önce terminalden şunu çalıştır:
```bash
gradle wrapper --gradle-version 8.4
```
Ya da Android Studio'da projeyi aç, otomatik oluşturur.

### Adım 3: Build
- Repo'ya push yaptıktan sonra **Actions** sekmesine git
- `Android Build` workflow otomatik başlar
- Bittikten sonra **Artifacts** kısmından `NumaraKaydet-debug.apk` indir

---

## 📲 Uygulama Nasıl Çalışır?

### Ana Ekran
- Son arayan **bilinmeyen numaralar** otomatik listelenir
- Zaten kayıtlı numaralar gösterilmez
- Bir numaraya tıkla → isim girme ekranı açılır

### İsim Girme Ekranı
- **İsim kutusuna tıkla** → mikrofon otomatik açılır
- İsmi söyle (Türkçe): uygulama anında yazar
- Harfler yanlış yazıldıysa → 🎙️ "Tekrar Söyle" butonuna bas
- **✅ Rehbere Kaydet** → numara SIM karta kaydedilir

### Ses Tanıma Özellikleri
- Türkçe dil desteği (`tr-TR`)
- Gerçek zamanlı yazma (söylerken görürsün)
- Harf düzeltme motoru (q→k, w→v vb.)
- İsim formatı (Her Kelimenin Baş Harfi Büyük)

---

## 🔐 İzinler
| İzin | Neden |
|------|-------|
| `READ_CALL_LOG` | Arayan numaraları görmek için |
| `READ_CONTACTS` | Kayıtlıları filtrelemek için |
| `WRITE_CONTACTS` | Rehbere kaydetmek için |
| `RECORD_AUDIO` | Ses tanıma için |
| `INTERNET` | Google ses tanıma servisi için |

---

## 🎨 Tasarım
- Renk: Android Yeşili `#3DDC84`
- İkon: Android Robot (arka yüzü yeşil)
- Font: Büyük, net, yaşlı gözler için okunabilir

---

*Barış tarafından dedesi için yapılmıştır* 💚
