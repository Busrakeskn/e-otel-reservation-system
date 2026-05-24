package com.eotel.test;

import com.eotel.manager.HotelSystem;
import com.eotel.model.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 3 temel test senaryosu:
 *  1) Normal rezervasyon akışı
 *  2) Tarih çakışması ve bekleme listesi
 *  3) Çoklu şube + iptal sonrası bekleme listesi otomasyonu
 */
public class TestScenarios {

    private static int testSayisi = 0;
    private static int basarili = 0;
    private static int basarisiz = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║         TEST SENARYOLARI ÇALIŞIYOR       ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        senaryo1_NormalRezervasyonAkisi();
        senaryo2_TarihCakismasiVeBekleme();
        senaryo3_CokluSubeVeIptalOtomasyonu();

        System.out.printf("%n=== SONUÇ: %d başarılı, %d başarısız (toplam %d) ===%n",
                basarili, basarisiz, testSayisi);
    }

    // ------------------------------------------------------------------
    // SENARYO 1: Normal Rezervasyon Akışı
    // ------------------------------------------------------------------
    private static void senaryo1_NormalRezervasyonAkisi() {
        HotelSystem.resetInstance();
        HotelSystem s = HotelSystem.getInstance();
        System.out.println("─── SENARYO 1: Normal Rezervasyon Akışı ───\n");

        // Veri oluştur
        Branch sube = s.subeEkle("İstanbul Merkez", "İstanbul", "Sultanahmet Mh.");
        Room oda    = s.odaEkle(sube.getSubeId(), 101, RoomType.DELUXE, 2500, 1);
        Customer m  = s.musteriEkle("Ayşe", "Yılmaz", "ayse@test.com", "5550001111");

        LocalDate giris = LocalDate.of(2025, 6, 10);
        LocalDate cikis = LocalDate.of(2025, 6, 15);

        // Rezervasyon yap
        Reservation rez = s.rezervasyonYap(m.getMusteriId(), oda.getOdaId(), giris, cikis, 2);
        dogrula("Rezervasyon oluşturuldu", rez != null);
        dogrula("Durum ONAYLANDI", rez.getDurum() == ReservationStatus.ONAYLANDI);
        dogrula("Toplam tutar 12500₺ (5 gece × 2500₺)",
                rez.getToplamTutar() == 12500.0);
        dogrula("Müşteri rezervasyon sayısı 1 oldu",
                m.getToplamRezervasyon() == 1);

        // Tamamla ve BST kontrolü
        s.rezervasyonTamamla(rez.getRezervasyonId());
        dogrula("Tamamlananlar BST'de 1 kayıt var",
                s.getTamamlananBST().getBoyut() == 1);
        dogrula("BST sıralı liste boş değil",
                !s.tamamlananRezervasyonlar().isEmpty());

        System.out.println();
    }

    // ------------------------------------------------------------------
    // SENARYO 2: Tarih Çakışması ve Bekleme Listesi
    // ------------------------------------------------------------------
    private static void senaryo2_TarihCakismasiVeBekleme() {
        HotelSystem.resetInstance();
        HotelSystem s = HotelSystem.getInstance();
        System.out.println("─── SENARYO 2: Tarih Çakışması ve Bekleme Listesi ───\n");

        Branch sube = s.subeEkle("Ankara Şube", "Ankara", "Kızılay Cad.");
        Room oda    = s.odaEkle(sube.getSubeId(), 201, RoomType.STANDART, 1500, 2);
        Customer m1 = s.musteriEkle("Mehmet", "Demir", "mehmet@test.com", "5550002222");
        Customer m2 = s.musteriEkle("Zeynep", "Kaya", "zeynep@test.com", "5550003333");
        Customer m3 = s.musteriEkle("Ali", "Çelik", "ali@test.com", "5550004444");

        LocalDate giris = LocalDate.of(2025, 7, 1);
        LocalDate cikis = LocalDate.of(2025, 7, 5);

        // İlk rezervasyon onaylanmalı
        Reservation rez1 = s.rezervasyonYap(m1.getMusteriId(), oda.getOdaId(), giris, cikis, 1);
        dogrula("1. rezervasyon onaylandı", rez1.getDurum() == ReservationStatus.ONAYLANDI);

        // Aynı tarih çakışan ikincisi bekleme listesine düşmeli
        Reservation rez2 = s.rezervasyonYap(m2.getMusteriId(), oda.getOdaId(),
                LocalDate.of(2025, 7, 3), LocalDate.of(2025, 7, 7), 1);
        dogrula("2. rezervasyon bekleme listesinde", rez2.getDurum() == ReservationStatus.BEKLEME_LISTESI);

        // Bir daha farklı tarih → çakışmıyor, direkt onaylanmalı
        Reservation rez3 = s.rezervasyonYap(m3.getMusteriId(), oda.getOdaId(),
                LocalDate.of(2025, 7, 10), LocalDate.of(2025, 7, 12), 1);
        dogrula("3. rezervasyon (farklı tarih) onaylandı", rez3.getDurum() == ReservationStatus.ONAYLANDI);

        // Bekleme listesinde 1 kayıt olmalı
        dogrula("Bekleme listesi boyutu 1",
                s.beklemeListe(oda.getOdaId()).getBoyut() == 1);

        // İlk rezervasyonu iptal et → bekleme listesindeki otomatik onaylanabilmeli
        // (rez3 çakışmıyor ama rez2 hâlâ çakışıyor diye onaylanamaz)
        s.rezervasyonIptalEt(rez1.getRezervasyonId());
        dogrula("1. rezervasyon iptal edildi", rez1.getDurum() == ReservationStatus.IPTAL_EDILDI);

        // rez2 artık çakışmıyor → otomatik onaylanmış olmalı
        dogrula("Bekleme listesindeki rez2 otomatik onaylandı",
                rez2.getDurum() == ReservationStatus.ONAYLANDI);

        System.out.println();
    }

    // ------------------------------------------------------------------
    // SENARYO 3: Çoklu Şube, Raporlama ve Hata Yönetimi
    // ------------------------------------------------------------------
    private static void senaryo3_CokluSubeVeIptalOtomasyonu() {
        HotelSystem.resetInstance();
        HotelSystem s = HotelSystem.getInstance();
        System.out.println("─── SENARYO 3: Çoklu Şube ve Hata Yönetimi ───\n");

        // İki farklı şube
        Branch istanbul = s.subeEkle("İstanbul", "İstanbul", "Adres A");
        Branch ankara   = s.subeEkle("Ankara",   "Ankara",   "Adres B");

        Room odaIst = s.odaEkle(istanbul.getSubeId(), 101, RoomType.SUIT, 5000, 5);
        Room odaAnk = s.odaEkle(ankara.getSubeId(),   101, RoomType.AILE, 4000, 3);

        Customer m = s.musteriEkle("Test", "Kullanıcı", "test@test.com", "5559999999");

        LocalDate g = LocalDate.of(2025, 8, 1);
        LocalDate c = LocalDate.of(2025, 8, 4);

        Reservation r1 = s.rezervasyonYap(m.getMusteriId(), odaIst.getOdaId(), g, c, 2);
        Reservation r2 = s.rezervasyonYap(m.getMusteriId(), odaAnk.getOdaId(), g, c, 4);
        dogrula("İstanbul şubesi rezervasyonu onaylandı", r1.getDurum() == ReservationStatus.ONAYLANDI);
        dogrula("Ankara şubesi rezervasyonu onaylandı",   r2.getDurum() == ReservationStatus.ONAYLANDI);

        // Şube bazlı gelir kontrolü (3 gece × 5000 = 15000, 3 gece × 4000 = 12000)
        dogrula("İstanbul geliri 15000₺", s.subeToplam(istanbul.getSubeId()) == 15000.0);
        dogrula("Ankara geliri 12000₺",   s.subeToplam(ankara.getSubeId())   == 12000.0);

        // Hata yönetimi: var olmayan oda
        try {
            s.rezervasyonYap(m.getMusteriId(), "YOK-ODA", g, c, 1);
            dogrula("Hata fırlatılmalıydı - fırlatılmadı", false);
        } catch (IllegalArgumentException e) {
            dogrula("Geçersiz oda ID hatası yakalandı", true);
        }

        // Hata yönetimi: kapasite aşımı
        try {
            s.rezervasyonYap(m.getMusteriId(), odaIst.getOdaId(),
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 3), 10);
            dogrula("Kapasite hatası fırlatılmalıydı", false);
        } catch (IllegalArgumentException e) {
            dogrula("Kapasite aşımı hatası yakalandı", true);
        }

        // Hata yönetimi: yanlış tarih sırası
        try {
            s.rezervasyonYap(m.getMusteriId(), odaIst.getOdaId(),
                    LocalDate.of(2025, 9, 5), LocalDate.of(2025, 9, 3), 1);
            dogrula("Tarih hatası fırlatılmalıydı", false);
        } catch (IllegalArgumentException e) {
            dogrula("Giriş/çıkış tarih sırası hatası yakalandı", true);
        }

        // Çoklu şube özet
        List<Branch> subeler = s.tumSubeler();
        dogrula("Sistemde 2 şube var", subeler.size() == 2);

        System.out.println();
    }

    // ---- Yardımcı ----

    private static void dogrula(String aciklama, boolean kosul) {
        testSayisi++;
        if (kosul) {
            basarili++;
            System.out.printf("  ✓ [%2d] %s%n", testSayisi, aciklama);
        } else {
            basarisiz++;
            System.out.printf("  ✗ [%2d] BAŞARISIZ: %s%n", testSayisi, aciklama);
        }
    }
}
