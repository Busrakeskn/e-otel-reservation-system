package com.eotel.manager;

import com.eotel.datastructures.CustomBST;
import com.eotel.datastructures.CustomHashMap;
import com.eotel.datastructures.CustomLinkedList;
import com.eotel.datastructures.IntervalTree;
import com.eotel.model.*;

import java.util.regex.Pattern;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HotelSystem {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFON_PATTERN =
            Pattern.compile("^(\\+90|0)?5[0-9]{9}$");

    // --- Veri yapıları ---
    // Oda ve müşterilere O(1) erişim için HashMap
    private final CustomHashMap<String, Room> odaMap;
    private final CustomHashMap<String, Customer> musteriMap;
    private final CustomHashMap<String, Branch> subeMap;
    private final CustomHashMap<String, Reservation> rezervasyonMap;

    // Tarih çakışma kontrolü için Interval Tree
    private final IntervalTree intervalTree;

    // Bekleme listesi için çift yönlü bağlı liste (her oda için ayrı)
    private final CustomHashMap<String, CustomLinkedList<Reservation>> beklemeListe;

    // Tamamlanan rezervasyonlar için BST (tarihe göre sıralı)
    private final CustomBST<Reservation> tamamlananBST;

    private static HotelSystem instance;

    private HotelSystem() {
        odaMap = new CustomHashMap<>();
        musteriMap = new CustomHashMap<>();
        subeMap = new CustomHashMap<>();
        rezervasyonMap = new CustomHashMap<>();
        intervalTree = new IntervalTree();
        beklemeListe = new CustomHashMap<>();
        tamamlananBST = new CustomBST<>();
    }

    public static HotelSystem getInstance() {
        if (instance == null) instance = new HotelSystem();
        return instance;
    }

    // ==================== ŞUBE ====================

    public Branch subeEkle(String ad, String sehir, String adres) {
        String id = "SUB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Branch sube = new Branch(id, ad, sehir, adres);
        subeMap.put(id, sube);
        return sube;
    }

    public void subeEkle(Branch sube) {
        subeMap.put(sube.getSubeId(), sube);
    }

    public Branch subeBul(String subeId) {
        return subeMap.get(subeId);
    }

    public List<Branch> tumSubeler() {
        return subeMap.values().stream()
                .filter(Branch::isAktif)
                .collect(Collectors.toList());
    }

    // ==================== ODA ====================

    public Room odaEkle(String subeId, int odaNo, RoomType tip, double fiyat, int kat) {
        if (subeMap.get(subeId) == null)
            throw new IllegalArgumentException("Şube bulunamadı: " + subeId);

        String id = subeId + "-R" + String.format("%03d", odaNo);
        if (odaMap.containsKey(id))
            throw new IllegalArgumentException("Bu oda zaten mevcut: " + id);

        Room oda = new Room(id, subeId, odaNo, tip, fiyat, kat);
        odaMap.put(id, oda);

        Branch sube = subeMap.get(subeId);
        sube.setToplamOda(sube.getToplamOda() + 1);
        return oda;
    }

    public void odaEkle(Room oda) {
        odaMap.put(oda.getOdaId(), oda);
    }

    public Room odaBul(String odaId) {
        return odaMap.get(odaId);
    }

    public List<Room> subeOdalari(String subeId) {
        return odaMap.values().stream()
                .filter(o -> o.getSubeId().equals(subeId) && o.isAktif())
                .collect(Collectors.toList());
    }

    public List<Room> musaitOdalari(String subeId, LocalDate giris, LocalDate cikis, int misafirSayisi) {
        List<Room> sonuc = new ArrayList<>();
        for (Room oda : subeOdalari(subeId)) {
            if (oda.getTip().getMaksKapasite() >= misafirSayisi
                    && !intervalTree.cakismaVarMi(oda.getOdaId(), giris, cikis)) {
                sonuc.add(oda);
            }
        }
        return sonuc;
    }

    public List<Room> tumOdalar() {
        return odaMap.values();
    }

    // ==================== MÜŞTERİ ====================

    public Customer musteriEkle(String ad, String soyad, String email, String telefon) {
        if (!EMAIL_PATTERN.matcher(email.trim()).matches())
            throw new IllegalArgumentException("Geçersiz e-posta formatı (örn: ad@mail.com)");

        if (telefon != null && !telefon.isBlank()) {
            String temizTel = telefon.replaceAll("[\\s\\-()]", "");
            if (!TELEFON_PATTERN.matcher(temizTel).matches())
                throw new IllegalArgumentException("Geçersiz telefon numarası (örn: 05XX XXX XX XX)");
        }

        for (Customer m : musteriMap.values()) {
            if (m.getEmail().equalsIgnoreCase(email))
                throw new IllegalArgumentException("Bu e-posta zaten kayıtlı: " + email);
        }
        String id = "MST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Customer musteri = new Customer(id, ad, soyad, email, telefon);
        musteriMap.put(id, musteri);
        return musteri;
    }

    public void musteriEkle(Customer musteri) {
        musteriMap.put(musteri.getMusteriId(), musteri);
    }

    public Customer musteriBul(String musteriId) {
        return musteriMap.get(musteriId);
    }

    public Customer musteriEmailIleBul(String email) {
        for (Customer m : musteriMap.values()) {
            if (m.getEmail().equalsIgnoreCase(email)) return m;
        }
        return null;
    }

    public List<Customer> tumMusteriler() {
        return musteriMap.values();
    }

    // ==================== REZERVASYON ====================

    public Reservation rezervasyonYap(String musteriId, String odaId,
                                       LocalDate giris, LocalDate cikis, int misafirSayisi) {
        Customer musteri = musteriMap.get(musteriId);
        if (musteri == null)
            throw new IllegalArgumentException("Müşteri bulunamadı: " + musteriId);

        Room oda = odaMap.get(odaId);
        if (oda == null)
            throw new IllegalArgumentException("Oda bulunamadı: " + odaId);

        if (!giris.isBefore(cikis))
            throw new IllegalArgumentException("Giriş tarihi çıkıştan önce olmalıdır");

        if (oda.getTip().getMaksKapasite() < misafirSayisi)
            throw new IllegalArgumentException("Oda kapasitesi yetersiz");

        // Interval tree ile çakışma kontrolü - O(log n)
        if (intervalTree.cakismaVarMi(odaId, giris, cikis)) {
            // Müsait değil → bekleme listesine al
            return beklemeListesineEkle(musteriId, odaId, oda.getSubeId(), giris, cikis, misafirSayisi);
        }

        String id = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Reservation rez = new Reservation(id, musteriId, odaId, oda.getSubeId(), giris, cikis, misafirSayisi);
        rez.setDurum(ReservationStatus.ONAYLANDI);

        long gece = rez.getGeceSayisi();
        rez.setToplamTutar(gece * oda.getGecelikFiyat());

        rezervasyonMap.put(id, rez);
        intervalTree.insert(new IntervalTree.Aralik(giris, cikis, odaId, id));
        musteri.rezervasyonArtir();

        return rez;
    }

    private Reservation beklemeListesineEkle(String musteriId, String odaId, String subeId,
                                              LocalDate giris, LocalDate cikis, int misafirSayisi) {
        String id = "BEK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Reservation rez = new Reservation(id, musteriId, odaId, subeId, giris, cikis, misafirSayisi);
        rez.setDurum(ReservationStatus.BEKLEME_LISTESI);

        Room oda = odaMap.get(odaId);
        if (oda != null) rez.setToplamTutar(rez.getGeceSayisi() * oda.getGecelikFiyat());

        rezervasyonMap.put(id, rez);

        if (!beklemeListe.containsKey(odaId))
            beklemeListe.put(odaId, new CustomLinkedList<>());

        beklemeListe.get(odaId).addLast(rez);
        return rez;
    }

    public boolean rezervasyonIptalEt(String rezervasyonId) {
        Reservation rez = rezervasyonMap.get(rezervasyonId);
        if (rez == null)
            throw new IllegalArgumentException("Rezervasyon bulunamadı: " + rezervasyonId);

        if (rez.getDurum() == ReservationStatus.IPTAL_EDILDI)
            throw new IllegalStateException("Rezervasyon zaten iptal edilmiş");

        if (rez.getDurum() == ReservationStatus.BEKLEME_LISTESI) {
            // bekleme listesinden çıkar
            CustomLinkedList<Reservation> liste = beklemeListe.get(rez.getOdaId());
            if (liste != null) liste.remove(rez);
        } else {
            // interval tree'den kaldır
            intervalTree.remove(rezervasyonId);
            // bekleme listesinde birisi varsa otomatik onayla
            siradakiBekleyeniOnayla(rez.getOdaId());
        }

        rez.setDurum(ReservationStatus.IPTAL_EDILDI);
        return true;
    }

    private void siradakiBekleyeniOnayla(String odaId) {
        CustomLinkedList<Reservation> liste = beklemeListe.get(odaId);
        if (liste == null || liste.bosmu()) return;

        // listedeki ilk çakışmayan rezervasyonu onayla
        List<Reservation> bekleyenler = new ArrayList<>();
        for (Reservation r : liste) bekleyenler.add(r);

        for (Reservation bekleyen : bekleyenler) {
            if (!intervalTree.cakismaVarMi(odaId, bekleyen.getGiris(), bekleyen.getCikis())) {
                liste.remove(bekleyen);
                bekleyen.setDurum(ReservationStatus.ONAYLANDI);
                intervalTree.insert(new IntervalTree.Aralik(
                        bekleyen.getGiris(), bekleyen.getCikis(), odaId, bekleyen.getRezervasyonId()));
                break;
            }
        }
    }

    public void rezervasyonTamamla(String rezervasyonId) {
        Reservation rez = rezervasyonMap.get(rezervasyonId);
        if (rez == null)
            throw new IllegalArgumentException("Rezervasyon bulunamadı: " + rezervasyonId);

        rez.setDurum(ReservationStatus.TAMAMLANDI);
        intervalTree.remove(rezervasyonId);
        tamamlananBST.insert(rez);
        siradakiBekleyeniOnayla(rez.getOdaId());
    }

    public Reservation rezervasyonGuncelle(String rezervasyonId,
                                            LocalDate yeniGiris, LocalDate yeniCikis,
                                            String yeniOdaId) {
        Reservation rez = rezervasyonMap.get(rezervasyonId);
        if (rez == null)
            throw new IllegalArgumentException("Rezervasyon bulunamadı: " + rezervasyonId);
        if (rez.getDurum() != ReservationStatus.ONAYLANDI)
            throw new IllegalStateException("Yalnızca onaylanmış rezervasyonlar değiştirilebilir");
        if (!yeniGiris.isBefore(yeniCikis))
            throw new IllegalArgumentException("Giriş tarihi çıkıştan önce olmalıdır");

        String hedefOdaId = (yeniOdaId != null && !yeniOdaId.isBlank()) ? yeniOdaId : rez.getOdaId();
        Room hedefOda = odaMap.get(hedefOdaId);
        if (hedefOda == null)
            throw new IllegalArgumentException("Oda bulunamadı: " + hedefOdaId);

        // Mevcut aralığı geçici olarak kaldır
        intervalTree.remove(rezervasyonId);

        // Hedef oda ve tarihlerde çakışma kontrolü
        if (intervalTree.cakismaVarMi(hedefOdaId, yeniGiris, yeniCikis)) {
            // Eski aralığı geri ekle
            intervalTree.insert(new IntervalTree.Aralik(
                    rez.getGiris(), rez.getCikis(), rez.getOdaId(), rezervasyonId));
            throw new IllegalStateException("Seçilen tarihler için oda müsait değil");
        }

        rez.setGiris(yeniGiris);
        rez.setCikis(yeniCikis);
        rez.setOdaId(hedefOdaId);
        rez.setSubeId(hedefOda.getSubeId());
        rez.setToplamTutar(rez.getGeceSayisi() * hedefOda.getGecelikFiyat());

        intervalTree.insert(new IntervalTree.Aralik(yeniGiris, yeniCikis, hedefOdaId, rezervasyonId));
        return rez;
    }

    public void odemeKaydet(String rezervasyonId, double miktar) {
        Reservation rez = rezervasyonMap.get(rezervasyonId);
        if (rez == null)
            throw new IllegalArgumentException("Rezervasyon bulunamadı: " + rezervasyonId);
        if (miktar <= 0)
            throw new IllegalArgumentException("Ödeme miktarı sıfırdan büyük olmalıdır");

        double yeniOdenen = rez.getOdenenTutar() + miktar;
        if (yeniOdenen > rez.getToplamTutar() + 0.01)
            throw new IllegalArgumentException(
                    String.format("Kalan tutar %.0f₺, bu kadar ödeme yapılamaz", rez.getToplamTutar() - rez.getOdenenTutar()));

        rez.setOdenenTutar(Math.min(yeniOdenen, rez.getToplamTutar()));
        rez.setOdemeDurumu(rez.getOdenenTutar() >= rez.getToplamTutar()
                ? OdemeDurumu.TAM_ODENDI : OdemeDurumu.ON_ODEME);
    }

    public Reservation rezervasyonBul(String rezervasyonId) {
        return rezervasyonMap.get(rezervasyonId);
    }

    public List<Reservation> musteriRezervasyonlari(String musteriId) {
        return rezervasyonMap.values().stream()
                .filter(r -> r.getMusteriId().equals(musteriId))
                .collect(Collectors.toList());
    }

    public List<Reservation> subeRezervasyonlari(String subeId) {
        return rezervasyonMap.values().stream()
                .filter(r -> r.getSubeId().equals(subeId))
                .collect(Collectors.toList());
    }

    public List<Reservation> tumRezervasyonlar() {
        return rezervasyonMap.values();
    }

    public void musteriSil(String musteriId) {
        Customer musteri = musteriMap.get(musteriId);
        if (musteri == null)
            throw new IllegalArgumentException("Müşteri bulunamadı: " + musteriId);

        boolean aktifVarMi = rezervasyonMap.values().stream()
                .filter(r -> r.getMusteriId().equals(musteriId))
                .anyMatch(r -> r.getDurum() == ReservationStatus.ONAYLANDI
                            || r.getDurum() == ReservationStatus.BEKLEME_LISTESI);
        if (aktifVarMi)
            throw new IllegalStateException("Aktif rezervasyonu olan müşteri silinemez. Önce rezervasyonları iptal edin.");

        musteriMap.remove(musteriId);
    }

    public void rezervasyonSil(String rezervasyonId) {
        Reservation rez = rezervasyonMap.get(rezervasyonId);
        if (rez == null)
            throw new IllegalArgumentException("Rezervasyon bulunamadı: " + rezervasyonId);

        if (rez.getDurum() == ReservationStatus.ONAYLANDI
                || rez.getDurum() == ReservationStatus.BEKLEME_LISTESI)
            throw new IllegalStateException("Aktif rezervasyon silinemez. Önce iptal edin.");

        rezervasyonMap.remove(rezervasyonId);
    }

    public List<Reservation> tamamlananRezervasyonlar() {
        return tamamlananBST.inorderList();
    }

    public CustomLinkedList<Reservation> beklemeListe(String odaId) {
        CustomLinkedList<Reservation> liste = beklemeListe.get(odaId);
        return liste != null ? liste : new CustomLinkedList<>();
    }

    public void beklemeListesineYukle(Reservation rez) {
        String odaId = rez.getOdaId();
        if (!beklemeListe.containsKey(odaId))
            beklemeListe.put(odaId, new CustomLinkedList<>());
        beklemeListe.get(odaId).addLast(rez);
    }

    // ==================== RAPOR ====================

    public double subeToplam(String subeId) {
        return subeRezervasyonlari(subeId).stream()
                .filter(r -> r.getDurum() == ReservationStatus.TAMAMLANDI
                          || r.getDurum() == ReservationStatus.ONAYLANDI)
                .mapToDouble(Reservation::getToplamTutar)
                .sum();
    }

    public long subeAktifRezervasyon(String subeId) {
        return subeRezervasyonlari(subeId).stream()
                .filter(r -> r.getDurum() == ReservationStatus.ONAYLANDI)
                .count();
    }

    public double dolulukOrani(String subeId, LocalDate giris, LocalDate cikis) {
        List<Room> odalar = subeOdalari(subeId);
        if (odalar.isEmpty()) return 0;
        long dolu = odalar.stream()
                .filter(o -> intervalTree.cakismaVarMi(o.getOdaId(), giris, cikis))
                .count();
        return (double) dolu / odalar.size() * 100;
    }

    // Sistemi sıfırla (test için)
    public static void resetInstance() {
        instance = null;
    }

    public CustomHashMap<String, Room> getOdaMap() { return odaMap; }
    public CustomHashMap<String, Customer> getMusteriMap() { return musteriMap; }
    public CustomHashMap<String, Branch> getSubeMap() { return subeMap; }
    public CustomHashMap<String, Reservation> getRezervasyonMap() { return rezervasyonMap; }
    public IntervalTree getIntervalTree() { return intervalTree; }
    public CustomBST<Reservation> getTamamlananBST() { return tamamlananBST; }
}
