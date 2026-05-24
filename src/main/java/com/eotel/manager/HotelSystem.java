package com.eotel.manager;

import com.eotel.datastructures.CustomBST;
import com.eotel.datastructures.CustomHashMap;
import com.eotel.datastructures.CustomLinkedList;
import com.eotel.datastructures.IntervalTree;
import com.eotel.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HotelSystem {

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
        // aynı e-posta ile zaten kayıt var mı?
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
