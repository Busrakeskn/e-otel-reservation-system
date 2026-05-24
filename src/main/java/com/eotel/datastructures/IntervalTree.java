package com.eotel.datastructures;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Interval Tree (Aralık Ağacı) - tarih aralığı çakışma kontrolü için
 *
 * Her düğüm [giris, cikis] aralığını saklar.
 * Her düğüm ayrıca alt ağacındaki maksimum bitiş tarihini tutar
 * → bu sayede gereksiz dallara girmekten kaçınılır.
 *
 * Zaman Karmaşıklığı:
 *   ekle            : O(log n) ortalama
 *   çakışma sorgusu : O(log n + k), k = bulunan çakışma sayısı
 *   sil             : O(log n) ortalama
 *
 * Alan Karmaşıklığı: O(n)
 */
public class IntervalTree {

    public static class Aralik {
        public final LocalDate baslangic;
        public final LocalDate bitis;
        public final String odaId;
        public final String rezervasyonId;

        public Aralik(LocalDate baslangic, LocalDate bitis, String odaId, String rezervasyonId) {
            this.baslangic = baslangic;
            this.bitis = bitis;
            this.odaId = odaId;
            this.rezervasyonId = rezervasyonId;
        }

        public boolean cakisiyorMu(LocalDate sorguBaslangic, LocalDate sorguBitis) {
            // [a,b] ve [c,d] çakışır ↔ a < d && c < b
            return baslangic.isBefore(sorguBitis) && sorguBaslangic.isBefore(bitis);
        }

        @Override
        public String toString() {
            return String.format("Aralik{oda=%s, %s→%s}", odaId, baslangic, bitis);
        }
    }

    private static class Dugum {
        Aralik aralik;
        LocalDate maxBitis;   // alt ağaçtaki en büyük bitiş tarihi
        Dugum sol;
        Dugum sag;

        Dugum(Aralik aralik) {
            this.aralik = aralik;
            this.maxBitis = aralik.bitis;
        }
    }

    private Dugum kok;
    private int boyut;

    public IntervalTree() {
        kok = null;
        boyut = 0;
    }

    // O(log n) ortalama - baslangic tarihine göre BST sıralaması
    public void insert(Aralik aralik) {
        kok = ekle(kok, aralik);
        boyut++;
    }

    private Dugum ekle(Dugum dugum, Aralik aralik) {
        if (dugum == null) return new Dugum(aralik);

        // maxBitis güncelle
        if (aralik.bitis.isAfter(dugum.maxBitis))
            dugum.maxBitis = aralik.bitis;

        if (aralik.baslangic.isBefore(dugum.aralik.baslangic))
            dugum.sol = ekle(dugum.sol, aralik);
        else
            dugum.sag = ekle(dugum.sag, aralik);

        return dugum;
    }

    /**
     * Belirli bir oda için [sorguBaslangic, sorguBitis] aralığı ile
     * çakışan tüm rezervasyonları döner.
     * O(log n + k) - k çakışma sayısı
     */
    public List<Aralik> cakisanlariGetir(String odaId, LocalDate sorguBaslangic, LocalDate sorguBitis) {
        List<Aralik> sonuc = new ArrayList<>();
        cakisanAra(kok, odaId, sorguBaslangic, sorguBitis, sonuc);
        return sonuc;
    }

    private void cakisanAra(Dugum dugum, String odaId,
                             LocalDate sorguBaslangic, LocalDate sorguBitis,
                             List<Aralik> sonuc) {
        if (dugum == null) return;

        // sol alt ağacın max bitişi sorgu başlangıcından önce ise oraya gitme
        if (dugum.sol != null && dugum.sol.maxBitis.isBefore(sorguBaslangic)) {
            // sol tarafta çakışma olamaz, sadece sağa bak
        } else {
            cakisanAra(dugum.sol, odaId, sorguBaslangic, sorguBitis, sonuc);
        }

        // mevcut düğüm çakışıyor mu?
        if (dugum.aralik.odaId.equals(odaId) && dugum.aralik.cakisiyorMu(sorguBaslangic, sorguBitis)) {
            sonuc.add(dugum.aralik);
        }

        // düğümün başlangıcı sorgu bitişinden sonra ise sağa gitmeye gerek yok
        if (!dugum.aralik.baslangic.isAfter(sorguBitis)) {
            cakisanAra(dugum.sag, odaId, sorguBaslangic, sorguBitis, sonuc);
        }
    }

    // Herhangi bir çakışma var mı? - O(log n)
    public boolean cakismaVarMi(String odaId, LocalDate baslangic, LocalDate bitis) {
        return cakismaKontrol(kok, odaId, baslangic, bitis);
    }

    private boolean cakismaKontrol(Dugum dugum, String odaId,
                                    LocalDate baslangic, LocalDate bitis) {
        if (dugum == null) return false;

        if (dugum.aralik.odaId.equals(odaId) && dugum.aralik.cakisiyorMu(baslangic, bitis))
            return true;

        if (dugum.sol != null && !dugum.sol.maxBitis.isBefore(baslangic))
            if (cakismaKontrol(dugum.sol, odaId, baslangic, bitis)) return true;

        return cakismaKontrol(dugum.sag, odaId, baslangic, bitis);
    }

    // Rezervasyon silinince ağaçtan da kaldır - O(n)
    public void remove(String rezervasyonId) {
        kok = sil(kok, rezervasyonId);
    }

    private Dugum sil(Dugum dugum, String rezervasyonId) {
        if (dugum == null) return null;

        if (dugum.aralik.rezervasyonId.equals(rezervasyonId)) {
            boyut--;
            if (dugum.sol == null) return dugum.sag;
            if (dugum.sag == null) return dugum.sol;

            // iki çocuk var: sağın en solunu bul
            Dugum halef = enSol(dugum.sag);
            dugum.aralik = halef.aralik;
            dugum.sag = sil(dugum.sag, halef.aralik.rezervasyonId);
        } else {
            dugum.sol = sil(dugum.sol, rezervasyonId);
            dugum.sag = sil(dugum.sag, rezervasyonId);
        }

        // maxBitis'i yeniden hesapla
        dugum.maxBitis = dugum.aralik.bitis;
        if (dugum.sol != null && dugum.sol.maxBitis.isAfter(dugum.maxBitis))
            dugum.maxBitis = dugum.sol.maxBitis;
        if (dugum.sag != null && dugum.sag.maxBitis.isAfter(dugum.maxBitis))
            dugum.maxBitis = dugum.sag.maxBitis;

        return dugum;
    }

    private Dugum enSol(Dugum dugum) {
        while (dugum.sol != null) dugum = dugum.sol;
        return dugum;
    }

    public int getBoyut() { return boyut; }
    public boolean isEmpty() { return boyut == 0; }

    public void temizle() {
        kok = null;
        boyut = 0;
    }
}
