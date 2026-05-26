# E-Otel Rezervasyon Yönetim Sistemi

Oda rezervasyonu, müsaitlik kontrolü ve müşteri yönetimi işlemlerini gerçekleştiren, farklı veri yapılarının bir arada kullanıldığı Java tabanlı bir otel yönetim sistemidir.

---

## İçindekiler

- [Özellikler](#özellikler)
- [Veri Yapıları](#veri-yapıları)
- [Kurulum](#kurulum)
- [Çalıştırma](#çalıştırma)
- [Proje Yapısı](#proje-yapısı)
- [Test Senaryoları](#test-senaryoları)
- [Big-O Karmaşıklık Analizi](#big-o-karmaşıklık-analizi)

---

## Özellikler

| Özellik | Açıklama |
|---|---|
| Rezervasyon yönetimi | Oluşturma, iptal, tamamlama, kalıcı silme |
| Rezervasyon değiştirme | Onaylı rezervasyonlarda tarih ve oda güncelleme |
| Ödeme takibi | Ön ödeme / tam ödeme kaydı, kalan bakiye görünümü |
| Müsaitlik kontrolü | Tarih aralığı çakışma denetimi |
| Bekleme listesi | Dolu oda için otomatik sıra |
| Müşteri yönetimi | Kayıt, arama, listeleme, silme |
| Format doğrulama | E-posta ve telefon formatı gerçek zamanlı kontrol |
| Çoklu şube | Bağımsız şube ve oda yönetimi |
| Raporlama | Gelir, doluluk oranı, şube karşılaştırması |
| Veri kalıcılığı | JSON ile otomatik kayıt/yükleme |
| Grafik arayüz | JavaFX ile masaüstü uygulaması |
| Konsol arayüzü | Renkli terminal menüsü |

---

## Veri Yapıları

Projede dört veri yapısı **sıfırdan** implement edilmiştir.

### 1. Interval Tree — Tarih Çakışma Kontrolü

`src/main/java/com/eotel/datastructures/IntervalTree.java`

Her rezervasyon `[giris, cikis]` aralığı olarak saklanır. Yeni bir rezervasyon yapılmak istendiğinde bu ağaç sorgulanarak çakışma olup olmadığı kontrol edilir.

```
Düğüm yapısı:
  [ baslangic, bitis ] → odaId, rezervasyonId
  maxBitis             → alt ağaçtaki en büyük bitiş tarihi
```

İki aralık `[a,b]` ve `[c,d]` şu koşulda çakışır: `a < d && c < b`

### 2. Çift Yönlü Bağlı Liste — Bekleme Listesi

`src/main/java/com/eotel/datastructures/CustomLinkedList.java`

Bir oda dolu olduğunda yeni gelen rezervasyon istekleri bu listeye eklenir. İptal sonrası baştaki bekleyen otomatik olarak onaylanır.

```
bas ←→ [rez1] ←→ [rez2] ←→ [rez3] ←→ son
```

### 3. Binary Search Tree — Tamamlanan Rezervasyonlar

`src/main/java/com/eotel/datastructures/CustomBST.java`

Tamamlanan rezervasyonlar giriş tarihine göre sıralı olarak saklanır. In-order gezişi doğrudan kronolojik sıralı liste üretir.

```
         [15 Haz]
        /         \
   [10 Haz]    [20 Haz]
```

### 4. Hash Map — Hızlı Oda/Müşteri Erişimi

`src/main/java/com/eotel/datastructures/CustomHashMap.java`

Ayrık zincirleme (separate chaining) ile çarpışma çözümü. Yükleme faktörü 0.75'i geçince otomatik yeniden boyutlandırma yapılır.

```
index = abs(key.hashCode() ^ (hashCode >>> 16)) % kapasite
```

---

## Kurulum

### Gereksinimler

- Java 17+
- Maven 3.8+

### Bağımlılıklar

| Kütüphane | Sürüm | Kullanım |
|---|---|---|
| JavaFX | 21.0.2 | Grafik arayüz |
| Google Gson | 2.10.1 | JSON serileştirme |

### Derleme

```bash
mvn clean package
```

---

## Çalıştırma

### JavaFX Arayüzü (varsayılan)

```bash
mvn javafx:run
```

### Konsol Modu

```bash
mvn exec:java -Dexec.mainClass="com.eotel.Main" -Dexec.args="--konsol"
```

### Test Senaryoları

```bash
mvn exec:java -Dexec.mainClass="com.eotel.test.TestScenarios"
```

---

## Proje Yapısı

```
e-otel/
├── pom.xml
├── data/                          ← JSON dosyaları (otomatik oluşur)
│   ├── subeler.json
│   ├── odalar.json
│   ├── musteriler.json
│   └── rezervasyonlar.json
└── src/main/java/com/eotel/
    ├── Main.java
    ├── model/
    │   ├── Room.java
    │   ├── Customer.java
    │   ├── Reservation.java
    │   ├── Branch.java
    │   ├── RoomType.java          ← enum
    │   ├── ReservationStatus.java ← enum
    │   └── OdemeDurumu.java       ← enum (Ödenmedi / Ön Ödeme / Tam Ödendi)
    ├── datastructures/            ← sıfırdan implement
    │   ├── CustomLinkedList.java
    │   ├── CustomHashMap.java
    │   ├── CustomBST.java
    │   └── IntervalTree.java
    ├── manager/
    │   └── HotelSystem.java       ← tüm iş mantığı (singleton)
    ├── persistence/
    │   └── JsonDataManager.java   ← JSON kayıt/yükleme
    ├── ui/
    │   ├── ConsoleUI.java         ← renkli konsol menüsü
    │   └── javafx/
    │       ├── HotelApp.java      ← JavaFX giriş noktası
    │       ├── AnaSayfa.java
    │       ├── RezervasyonSayfasi.java
    │       ├── MusaitlikSayfasi.java
    │       ├── OdaSayfasi.java
    │       ├── MusteriSayfasi.java
    │       ├── SubeSayfasi.java
    │       └── RaporSayfasi.java
    └── test/
        └── TestScenarios.java
```

---

## Test Senaryoları

### Senaryo 1 — Normal Rezervasyon Akışı
Şube ve oda oluşturulur, müşteri kaydedilir, rezervasyon yapılır. Durumun `ONAYLANDI` olduğu, tutarın doğru hesaplandığı ve tamamlandıktan sonra BST'ye eklendiği doğrulanır.

### Senaryo 2 — Tarih Çakışması ve Bekleme Listesi
Aynı oda için çakışan iki rezervasyon yapılır. İkincisinin `BEKLEME_LISTESI` durumuna düştüğü, ardından birinci iptal edilince bekleme listesindekinin otomatik `ONAYLANDI` durumuna geçtiği doğrulanır.

### Senaryo 3 — Çoklu Şube ve Hata Yönetimi
İki farklı şubede eş zamanlı rezervasyon yapılır. Şube bazlı gelir hesabı kontrol edilir. Geçersiz oda, kapasite aşımı ve yanlış tarih sırası gibi hata durumları test edilir.

---

## Big-O Karmaşıklık Analizi

| İşlem | Veri Yapısı | Ortalama | En Kötü |
|---|---|---|---|
| Oda/Müşteri erişimi | CustomHashMap | **O(1)** | O(n) |
| Çakışma kontrolü | IntervalTree | **O(log n + k)** | O(n) |
| Bekleme listesi ekle | CustomLinkedList | **O(1)** | O(1) |
| Bekleme listesinden sil | CustomLinkedList | **O(1)** | O(1) |
| Tamamlanan kaydet | CustomBST | **O(log n)** | O(n) |
| Kronolojik listeleme | CustomBST in-order | **O(n)** | O(n) |
| HashMap yeniden boyutlandırma | CustomHashMap | O(n) | — |

> `k` = bulunan çakışma sayısı, `n` = yapıdaki eleman sayısı
