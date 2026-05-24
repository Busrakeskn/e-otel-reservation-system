package com.eotel.ui;

import com.eotel.datastructures.CustomLinkedList;
import com.eotel.manager.HotelSystem;
import com.eotel.model.*;
import com.eotel.persistence.JsonDataManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private final HotelSystem sistem;
    private final JsonDataManager jsonManager;
    private final Scanner sc;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String KIRMIZI = "[31m";
    private static final String YESIL   = "[32m";
    private static final String SARI    = "[33m";
    private static final String MAVI    = "[34m";
    private static final String RESET   = "[0m";

    public ConsoleUI() {
        this.sistem = HotelSystem.getInstance();
        this.jsonManager = new JsonDataManager();
        this.sc = new Scanner(System.in);
    }

    public void baslat() {
        jsonManager.tumunuYukle(sistem);
        baslikYaz("E-OTEL REZERVASYON YÖNETİM SİSTEMİ");

        boolean calis = true;
        while (calis) {
            anaMenu();
            int secim = intOku("Seçiminiz");
            switch (secim) {
                case 1 -> subeMenu();
                case 2 -> odaMenu();
                case 3 -> musteriMenu();
                case 4 -> rezervasyonMenu();
                case 5 -> raporMenu();
                case 6 -> { jsonManager.tumunuKaydet(sistem); bilgi("Veriler kaydedildi."); }
                case 0 -> {
                    jsonManager.tumunuKaydet(sistem);
                    bilgi("Veriler kaydedildi. Çıkılıyor...");
                    calis = false;
                }
                default -> hata("Geçersiz seçim");
            }
        }
    }

    // ==================== MENÜLER ====================

    private void anaMenu() {
        System.out.println();
        baslik("ANA MENÜ");
        System.out.println("  1) Şube Yönetimi");
        System.out.println("  2) Oda Yönetimi");
        System.out.println("  3) Müşteri Yönetimi");
        System.out.println("  4) Rezervasyon Yönetimi");
        System.out.println("  5) Raporlar");
        System.out.println("  6) Kaydet");
        System.out.println("  0) Çıkış");
        ayrac();
    }

    // ---- Şube ----

    private void subeMenu() {
        boolean donme = true;
        while (donme) {
            baslik("ŞUBE YÖNETİMİ");
            System.out.println("  1) Şube Ekle");
            System.out.println("  2) Şubeleri Listele");
            System.out.println("  0) Geri");
            ayrac();
            switch (intOku("Seçim")) {
                case 1 -> subeEkle();
                case 2 -> subeleriListele();
                case 0 -> donme = false;
                default -> hata("Geçersiz seçim");
            }
        }
    }

    private void subeEkle() {
        baslik("YENİ ŞUBE");
        String ad    = metinOku("Şube adı");
        String sehir = metinOku("Şehir");
        String adres = metinOku("Adres");
        Branch sube  = sistem.subeEkle(ad, sehir, adres);
        basari("Şube oluşturuldu: " + sube.getSubeId());
    }

    private void subeleriListele() {
        List<Branch> subeler = sistem.tumSubeler();
        if (subeler.isEmpty()) { hata("Kayıtlı şube yok"); return; }
        baslik("ŞUBELER");
        subeler.forEach(s -> System.out.printf("  %-12s %-25s %s%n",
                s.getSubeId(), s.getAd(), s.getSehir()));
    }

    // ---- Oda ----

    private void odaMenu() {
        boolean donme = true;
        while (donme) {
            baslik("ODA YÖNETİMİ");
            System.out.println("  1) Oda Ekle");
            System.out.println("  2) Odaları Listele (şubeye göre)");
            System.out.println("  3) Müsait Odaları Ara");
            System.out.println("  0) Geri");
            ayrac();
            switch (intOku("Seçim")) {
                case 1 -> odaEkle();
                case 2 -> odalariListele();
                case 3 -> musaitOdaAra();
                case 0 -> donme = false;
                default -> hata("Geçersiz seçim");
            }
        }
    }

    private void odaEkle() {
        if (sistem.tumSubeler().isEmpty()) { hata("Önce şube eklemelisiniz"); return; }
        subeleriListele();

        String subeId = metinOku("Şube ID");
        if (sistem.subeBul(subeId) == null) { hata("Şube bulunamadı"); return; }

        int odaNo = intOku("Oda numarası");
        int kat   = intOku("Kat");

        System.out.println("Oda tipleri:");
        RoomType[] tipler = RoomType.values();
        for (int i = 0; i < tipler.length; i++)
            System.out.printf("  %d) %s (%.0f₺, max %d kişi)%n",
                    i + 1, tipler[i].getGoruntulenenAd(),
                    tipler[i].getTemelFiyat(), tipler[i].getMaksKapasite());

        int tipSecim = intOku("Tip (1-" + tipler.length + ")");
        if (tipSecim < 1 || tipSecim > tipler.length) { hata("Geçersiz tip"); return; }
        RoomType tip = tipler[tipSecim - 1];

        double fiyat = doubleOku("Gecelik fiyat (₺)");

        try {
            Room oda = sistem.odaEkle(subeId, odaNo, tip, fiyat, kat);
            basari("Oda eklendi: " + oda.getOdaId());
        } catch (IllegalArgumentException e) {
            hata(e.getMessage());
        }
    }

    private void odalariListele() {
        if (sistem.tumSubeler().isEmpty()) { hata("Şube yok"); return; }
        subeleriListele();
        String subeId = metinOku("Şube ID");
        List<Room> odalar = sistem.subeOdalari(subeId);
        if (odalar.isEmpty()) { hata("Bu şubeye ait oda bulunamadı"); return; }

        baslik("ODALAR - " + subeId);
        System.out.printf("  %-14s %-6s %-20s %-5s %s%n",
                "ID", "No", "Tip", "Kat", "Fiyat");
        ayrac();
        for (Room o : odalar)
            System.out.printf("  %-14s %-6d %-20s %-5d %.0f₺%n",
                    o.getOdaId(), o.getOdaNumarasi(),
                    o.getTip().getGoruntulenenAd(), o.getKat(), o.getGecelikFiyat());
    }

    private void musaitOdaAra() {
        if (sistem.tumSubeler().isEmpty()) { hata("Şube yok"); return; }
        subeleriListele();
        String subeId = metinOku("Şube ID");

        LocalDate giris = tarihOku("Giriş tarihi (gg/aa/yyyy)");
        LocalDate cikis = tarihOku("Çıkış tarihi (gg/aa/yyyy)");
        if (giris == null || cikis == null) return;
        if (!giris.isBefore(cikis)) { hata("Giriş çıkıştan önce olmalıdır"); return; }

        int misafir = intOku("Misafir sayısı");
        List<Room> musaitler = sistem.musaitOdalari(subeId, giris, cikis, misafir);

        if (musaitler.isEmpty()) {
            bilgi("Belirtilen tarihler için müsait oda bulunamadı");
            return;
        }
        baslik("MÜSAİT ODALAR");
        musaitler.forEach(o -> System.out.printf("  %s - %s - %.0f₺/gece%n",
                o.getOdaId(), o.getTip().getGoruntulenenAd(), o.getGecelikFiyat()));
    }

    // ---- Müşteri ----

    private void musteriMenu() {
        boolean donme = true;
        while (donme) {
            baslik("MÜŞTERİ YÖNETİMİ");
            System.out.println("  1) Müşteri Ekle");
            System.out.println("  2) Müşterileri Listele");
            System.out.println("  3) Müşteri Ara (e-posta)");
            System.out.println("  0) Geri");
            ayrac();
            switch (intOku("Seçim")) {
                case 1 -> musteriEkle();
                case 2 -> musterileriListele();
                case 3 -> musteriAra();
                case 0 -> donme = false;
                default -> hata("Geçersiz seçim");
            }
        }
    }

    private void musteriEkle() {
        baslik("YENİ MÜŞTERİ");
        String ad      = metinOku("Ad");
        String soyad   = metinOku("Soyad");
        String email   = metinOku("E-posta");
        String telefon = metinOku("Telefon");
        try {
            Customer m = sistem.musteriEkle(ad, soyad, email, telefon);
            basari("Müşteri oluşturuldu: " + m.getMusteriId());
        } catch (IllegalArgumentException e) {
            hata(e.getMessage());
        }
    }

    private void musterileriListele() {
        List<Customer> musteriler = sistem.tumMusteriler();
        if (musteriler.isEmpty()) { hata("Kayıtlı müşteri yok"); return; }
        baslik("MÜŞTERİLER");
        System.out.printf("  %-14s %-25s %-30s %s%n", "ID", "Ad Soyad", "E-posta", "Rezervasyon");
        ayrac();
        musteriler.forEach(m -> System.out.printf("  %-14s %-25s %-30s %d%n",
                m.getMusteriId(), m.getTamAd(), m.getEmail(), m.getToplamRezervasyon()));
    }

    private void musteriAra() {
        String email = metinOku("E-posta adresi");
        Customer m = sistem.musteriEmailIleBul(email);
        if (m == null) { hata("Müşteri bulunamadı"); return; }
        System.out.println();
        System.out.println("  ID      : " + m.getMusteriId());
        System.out.println("  Ad Soyad: " + m.getTamAd());
        System.out.println("  E-posta : " + m.getEmail());
        System.out.println("  Telefon : " + m.getTelefon());
        System.out.println("  Rezerv. : " + m.getToplamRezervasyon());
    }

    // ---- Rezervasyon ----

    private void rezervasyonMenu() {
        boolean donme = true;
        while (donme) {
            baslik("REZERVASYON YÖNETİMİ");
            System.out.println("  1) Yeni Rezervasyon");
            System.out.println("  2) Rezervasyonları Listele");
            System.out.println("  3) Rezervasyon İptal");
            System.out.println("  4) Rezervasyon Tamamla");
            System.out.println("  5) Tamamlananlar (BST - Tarih Sırası)");
            System.out.println("  6) Bekleme Listesi");
            System.out.println("  7) Müşteri Rezervasyonları");
            System.out.println("  0) Geri");
            ayrac();
            switch (intOku("Seçim")) {
                case 1 -> yeniRezervasyon();
                case 2 -> rezervasyonlariListele();
                case 3 -> rezervasyonIptal();
                case 4 -> rezervasyonTamamla();
                case 5 -> tamamlananlarListele();
                case 6 -> beklemeListesiGoster();
                case 7 -> musteriRezervasyonlari();
                case 0 -> donme = false;
                default -> hata("Geçersiz seçim");
            }
        }
    }

    private void yeniRezervasyon() {
        baslik("YENİ REZERVASYON");

        String musteriId = metinOku("Müşteri ID (yoksa 'yeni')");
        if (musteriId.equalsIgnoreCase("yeni")) {
            musteriEkle();
            System.out.print("Oluşturulan müşteri ID'yi girin: ");
            musteriId = sc.nextLine().trim();
        }

        String odaId = metinOku("Oda ID");
        LocalDate giris = tarihOku("Giriş (gg/aa/yyyy)");
        LocalDate cikis = tarihOku("Çıkış (gg/aa/yyyy)");
        if (giris == null || cikis == null) return;

        int misafir = intOku("Misafir sayısı");

        try {
            Reservation rez = sistem.rezervasyonYap(musteriId, odaId, giris, cikis, misafir);
            if (rez.getDurum() == ReservationStatus.BEKLEME_LISTESI) {
                bilgi("Oda dolu! Bekleme listesine alındı: " + rez.getRezervasyonId());
            } else {
                basari("Rezervasyon oluşturuldu: " + rez.getRezervasyonId());
                System.out.printf("  Toplam tutar: %.0f₺ (%d gece)%n",
                        rez.getToplamTutar(), rez.getGeceSayisi());
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            hata(e.getMessage());
        }
    }

    private void rezervasyonlariListele() {
        List<Reservation> liste = sistem.tumRezervasyonlar();
        if (liste.isEmpty()) { hata("Rezervasyon bulunamadı"); return; }
        baslik("TÜM REZERVASYONLAR");
        System.out.printf("  %-14s %-14s %-14s %-12s %-12s %-15s %s%n",
                "ID", "Müşteri", "Oda", "Giriş", "Çıkış", "Durum", "Tutar");
        ayrac();
        for (Reservation r : liste) {
            System.out.printf("  %-14s %-14s %-14s %-12s %-12s %-15s %.0f₺%n",
                    r.getRezervasyonId(), r.getMusteriId(), r.getOdaId(),
                    r.getGiris(), r.getCikis(), r.getDurum().getLabel(), r.getToplamTutar());
        }
    }

    private void rezervasyonIptal() {
        String id = metinOku("Rezervasyon ID");
        try {
            sistem.rezervasyonIptalEt(id);
            basari("Rezervasyon iptal edildi: " + id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            hata(e.getMessage());
        }
    }

    private void rezervasyonTamamla() {
        String id = metinOku("Rezervasyon ID");
        try {
            sistem.rezervasyonTamamla(id);
            basari("Rezervasyon tamamlandı ve BST'ye eklendi: " + id);
        } catch (IllegalArgumentException e) {
            hata(e.getMessage());
        }
    }

    private void tamamlananlarListele() {
        List<Reservation> liste = sistem.tamamlananRezervasyonlar();
        if (liste.isEmpty()) { bilgi("Tamamlanan rezervasyon yok"); return; }
        baslik("TAMAMLANAN REZERVASYONLAR (Tarihe Göre Sıralı - BST)");
        liste.forEach(r -> System.out.printf("  %-14s %-14s %s → %s  %.0f₺%n",
                r.getRezervasyonId(), r.getMusteriId(), r.getGiris(), r.getCikis(), r.getToplamTutar()));
    }

    private void beklemeListesiGoster() {
        String odaId = metinOku("Oda ID");
        CustomLinkedList<Reservation> liste = sistem.beklemeListe(odaId);
        if (liste.bosmu()) { bilgi("Bekleme listesi boş"); return; }
        baslik("BEKLEME LİSTESİ - " + odaId + " (Bağlı Liste)");
        int sira = 1;
        for (Reservation r : liste) {
            System.out.printf("  %d) %-14s %-14s %s → %s%n",
                    sira++, r.getRezervasyonId(), r.getMusteriId(), r.getGiris(), r.getCikis());
        }
    }

    private void musteriRezervasyonlari() {
        String musteriId = metinOku("Müşteri ID");
        List<Reservation> liste = sistem.musteriRezervasyonlari(musteriId);
        if (liste.isEmpty()) { bilgi("Bu müşteriye ait rezervasyon bulunamadı"); return; }
        baslik("MÜŞTERİ REZERVASYONLARI");
        liste.forEach(r -> System.out.printf("  %-14s %-14s %s → %s  %-15s %.0f₺%n",
                r.getRezervasyonId(), r.getOdaId(),
                r.getGiris(), r.getCikis(), r.getDurum().getLabel(), r.getToplamTutar()));
    }

    // ---- Rapor ----

    private void raporMenu() {
        boolean donme = true;
        while (donme) {
            baslik("RAPORLAR");
            System.out.println("  1) Şube Gelir Raporu");
            System.out.println("  2) Doluluk Oranı");
            System.out.println("  3) Tüm Şubeler Özet");
            System.out.println("  0) Geri");
            ayrac();
            switch (intOku("Seçim")) {
                case 1 -> subeGelirRaporu();
                case 2 -> dolulukOrani();
                case 3 -> tumSubelerOzet();
                case 0 -> donme = false;
                default -> hata("Geçersiz seçim");
            }
        }
    }

    private void subeGelirRaporu() {
        subeleriListele();
        String subeId = metinOku("Şube ID");
        double gelir = sistem.subeToplam(subeId);
        long aktif = sistem.subeAktifRezervasyon(subeId);
        baslik("GELİR RAPORU - " + subeId);
        System.out.printf("  Toplam Gelir   : %.0f₺%n", gelir);
        System.out.printf("  Aktif Rezerv.  : %d%n", aktif);
    }

    private void dolulukOrani() {
        subeleriListele();
        String subeId = metinOku("Şube ID");
        LocalDate bas = tarihOku("Başlangıç tarihi (gg/aa/yyyy)");
        LocalDate bit = tarihOku("Bitiş tarihi (gg/aa/yyyy)");
        if (bas == null || bit == null) return;
        double oran = sistem.dolulukOrani(subeId, bas, bit);
        System.out.printf("%n  Doluluk Oranı: %%.1f%%%n".formatted(oran));
    }

    private void tumSubelerOzet() {
        List<Branch> subeler = sistem.tumSubeler();
        if (subeler.isEmpty()) { hata("Şube yok"); return; }
        baslik("TÜM ŞUBELER ÖZET");
        System.out.printf("  %-12s %-25s %-12s %-12s %s%n",
                "ID", "Ad", "Şehir", "Oda", "Gelir");
        ayrac();
        for (Branch s : subeler) {
            int odaSayisi = sistem.subeOdalari(s.getSubeId()).size();
            double gelir  = sistem.subeToplam(s.getSubeId());
            System.out.printf("  %-12s %-25s %-12s %-12d %.0f₺%n",
                    s.getSubeId(), s.getAd(), s.getSehir(), odaSayisi, gelir);
        }
    }

    // ==================== YARDIMCI ====================

    private String metinOku(String mesaj) {
        System.out.print("  " + mesaj + ": ");
        return sc.nextLine().trim();
    }

    private int intOku(String mesaj) {
        while (true) {
            System.out.print("  " + mesaj + ": ");
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                hata("Lütfen geçerli bir sayı girin");
            }
        }
    }

    private double doubleOku(String mesaj) {
        while (true) {
            System.out.print("  " + mesaj + ": ");
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                hata("Lütfen geçerli bir sayı girin");
            }
        }
    }

    private LocalDate tarihOku(String mesaj) {
        while (true) {
            System.out.print("  " + mesaj + ": ");
            String girdi = sc.nextLine().trim();
            try {
                return LocalDate.parse(girdi, FMT);
            } catch (DateTimeParseException e) {
                hata("Geçersiz tarih formatı. Örnek: 25/05/2025");
            }
        }
    }

    private void baslikYaz(String metin) {
        System.out.println();
        System.out.println(KIRMIZI + "╔══════════════════════════════════════╗");
        System.out.printf( "║  %-36s║%n", metin);
        System.out.println("╚══════════════════════════════════════╝" + RESET);
    }

    private void baslik(String metin) {
        System.out.println();
        System.out.println(MAVI + "─── " + metin + " ───" + RESET);
    }

    private void ayrac() {
        System.out.println("  ─────────────────────────────────────");
    }

    private void basari(String msg) {
        System.out.println(YESIL + "  ✓ " + msg + RESET);
    }

    private void bilgi(String msg) {
        System.out.println(SARI + "  ℹ " + msg + RESET);
    }

    private void hata(String msg) {
        System.out.println(KIRMIZI + "  ✗ HATA: " + msg + RESET);
    }
}
