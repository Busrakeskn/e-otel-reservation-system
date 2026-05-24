package com.eotel.datastructures;

import java.util.ArrayList;
import java.util.List;

/**
 * İkili Arama Ağacı (BST) - tamamlanan rezervasyonları
 * giriş tarihine göre sıralı saklamak için
 *
 * Zaman Karmaşıklığı:
 *   ekle     : O(log n) ortalama, O(n) en kötü
 *   ara      : O(log n) ortalama, O(n) en kötü
 *   sil      : O(log n) ortalama, O(n) en kötü
 *   sıralı   : O(n)
 *
 * Alan Karmaşıklığı: O(n)
 */
public class CustomBST<T extends Comparable<T>> {

    private static class Dugum<T> {
        T veri;
        Dugum<T> sol;
        Dugum<T> sag;

        Dugum(T veri) {
            this.veri = veri;
        }
    }

    private Dugum<T> kok;
    private int boyut;

    public CustomBST() {
        kok = null;
        boyut = 0;
    }

    // O(log n) ortalama
    public void insert(T veri) {
        kok = ekleYardimci(kok, veri);
        boyut++;
    }

    private Dugum<T> ekleYardimci(Dugum<T> dugum, T veri) {
        if (dugum == null) return new Dugum<>(veri);

        int karsilastirma = veri.compareTo(dugum.veri);
        if (karsilastirma < 0)
            dugum.sol = ekleYardimci(dugum.sol, veri);
        else if (karsilastirma > 0)
            dugum.sag = ekleYardimci(dugum.sag, veri);
        // eşit ise tekrar eklemiyoruz
        return dugum;
    }

    // O(log n) ortalama
    public boolean contains(T veri) {
        return araYardimci(kok, veri) != null;
    }

    private Dugum<T> araYardimci(Dugum<T> dugum, T veri) {
        if (dugum == null) return null;
        int k = veri.compareTo(dugum.veri);
        if (k == 0) return dugum;
        if (k < 0) return araYardimci(dugum.sol, veri);
        return araYardimci(dugum.sag, veri);
    }

    // O(log n) ortalama
    public void delete(T veri) {
        if (!contains(veri)) return;
        kok = silYardimci(kok, veri);
        boyut--;
    }

    private Dugum<T> silYardimci(Dugum<T> dugum, T veri) {
        if (dugum == null) return null;

        int k = veri.compareTo(dugum.veri);
        if (k < 0) {
            dugum.sol = silYardimci(dugum.sol, veri);
        } else if (k > 0) {
            dugum.sag = silYardimci(dugum.sag, veri);
        } else {
            // iki çocuğu var → sağ alt ağacın en küçüğü ile değiştir
            if (dugum.sol == null) return dugum.sag;
            if (dugum.sag == null) return dugum.sol;

            Dugum<T> halef = enKucuk(dugum.sag);
            dugum.veri = halef.veri;
            dugum.sag = silYardimci(dugum.sag, halef.veri);
        }
        return dugum;
    }

    private Dugum<T> enKucuk(Dugum<T> dugum) {
        while (dugum.sol != null) dugum = dugum.sol;
        return dugum;
    }

    // Sıralı (in-order) geçiş - O(n), tarihe göre sıralı liste döner
    public List<T> inorderList() {
        List<T> liste = new ArrayList<>();
        inorderYardimci(kok, liste);
        return liste;
    }

    private void inorderYardimci(Dugum<T> dugum, List<T> liste) {
        if (dugum == null) return;
        inorderYardimci(dugum.sol, liste);
        liste.add(dugum.veri);
        inorderYardimci(dugum.sag, liste);
    }

    // Belirli aralıktaki elemanlar - O(k + log n), k = bulunan eleman sayısı
    public List<T> aralikSorgusu(T baslangic, T bitis) {
        List<T> sonuc = new ArrayList<>();
        aralikYardimci(kok, baslangic, bitis, sonuc);
        return sonuc;
    }

    private void aralikYardimci(Dugum<T> dugum, T baslangic, T bitis, List<T> sonuc) {
        if (dugum == null) return;

        if (dugum.veri.compareTo(baslangic) > 0)
            aralikYardimci(dugum.sol, baslangic, bitis, sonuc);

        if (dugum.veri.compareTo(baslangic) >= 0 && dugum.veri.compareTo(bitis) <= 0)
            sonuc.add(dugum.veri);

        if (dugum.veri.compareTo(bitis) < 0)
            aralikYardimci(dugum.sag, baslangic, bitis, sonuc);
    }

    public T minimum() {
        if (kok == null) throw new RuntimeException("BST boş");
        return enKucuk(kok).veri;
    }

    public T maximum() {
        if (kok == null) throw new RuntimeException("BST boş");
        Dugum<T> d = kok;
        while (d.sag != null) d = d.sag;
        return d.veri;
    }

    public int getBoyut() { return boyut; }
    public boolean isEmpty() { return boyut == 0; }

    public void temizle() {
        kok = null;
        boyut = 0;
    }

    public int yukseklik() {
        return yukseklikYardimci(kok);
    }

    private int yukseklikYardimci(Dugum<T> dugum) {
        if (dugum == null) return 0;
        return 1 + Math.max(yukseklikYardimci(dugum.sol), yukseklikYardimci(dugum.sag));
    }
}
