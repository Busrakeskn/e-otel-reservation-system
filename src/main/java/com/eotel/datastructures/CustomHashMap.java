package com.eotel.datastructures;

import java.util.ArrayList;
import java.util.List;

/**
 * Ayrık zincirleme (separate chaining) ile HashMap implementasyonu
 * Oda ve müşteri bilgilerine hızlı erişim sağlar
 *
 * Zaman Karmaşıklığı (ortalama):
 *   put  : O(1)
 *   get  : O(1)
 *   remove: O(1)
 *   Yükleme faktörü > 0.75 olunca yeniden boyutlandırma: O(n)
 *
 * Alan Karmaşıklığı: O(n)
 */
public class CustomHashMap<K, V> {

    private static final int BASLANGIC_KAPASITE = 16;
    private static final double YUKLEME_FAKTORU = 0.75;

    private static class Girdi<K, V> {
        K anahtar;
        V deger;
        Girdi<K, V> sonraki;

        Girdi(K anahtar, V deger) {
            this.anahtar = anahtar;
            this.deger = deger;
        }
    }

    private Girdi<K, V>[] tablo;
    private int boyut;
    private int kapasite;

    @SuppressWarnings("unchecked")
    public CustomHashMap() {
        this.kapasite = BASLANGIC_KAPASITE;
        this.tablo = new Girdi[kapasite];
        this.boyut = 0;
    }

    private int hashHesapla(K anahtar) {
        if (anahtar == null) return 0;
        int h = anahtar.hashCode();
        // daha iyi dağılım için
        h = h ^ (h >>> 16);
        return Math.abs(h % kapasite);
    }

    // O(1) ortalama
    public void put(K anahtar, V deger) {
        if ((double) boyut / kapasite >= YUKLEME_FAKTORU) {
            yenidenBoyutlandir();
        }
        int indeks = hashHesapla(anahtar);
        Girdi<K, V> mevcut = tablo[indeks];

        while (mevcut != null) {
            if (mevcut.anahtar.equals(anahtar)) {
                mevcut.deger = deger;
                return;
            }
            mevcut = mevcut.sonraki;
        }

        Girdi<K, V> yeni = new Girdi<>(anahtar, deger);
        yeni.sonraki = tablo[indeks];
        tablo[indeks] = yeni;
        boyut++;
    }

    // O(1) ortalama
    public V get(K anahtar) {
        int indeks = hashHesapla(anahtar);
        Girdi<K, V> mevcut = tablo[indeks];
        while (mevcut != null) {
            if (mevcut.anahtar.equals(anahtar)) return mevcut.deger;
            mevcut = mevcut.sonraki;
        }
        return null;
    }

    // O(1) ortalama
    public V remove(K anahtar) {
        int indeks = hashHesapla(anahtar);
        Girdi<K, V> mevcut = tablo[indeks];
        Girdi<K, V> onceki = null;

        while (mevcut != null) {
            if (mevcut.anahtar.equals(anahtar)) {
                if (onceki == null) tablo[indeks] = mevcut.sonraki;
                else onceki.sonraki = mevcut.sonraki;
                boyut--;
                return mevcut.deger;
            }
            onceki = mevcut;
            mevcut = mevcut.sonraki;
        }
        return null;
    }

    public boolean containsKey(K anahtar) {
        return get(anahtar) != null;
    }

    public int size() { return boyut; }
    public boolean isEmpty() { return boyut == 0; }

    public List<K> keySet() {
        List<K> anahtarlar = new ArrayList<>();
        for (Girdi<K, V> basBucket : tablo) {
            Girdi<K, V> mevcut = basBucket;
            while (mevcut != null) {
                anahtarlar.add(mevcut.anahtar);
                mevcut = mevcut.sonraki;
            }
        }
        return anahtarlar;
    }

    public List<V> values() {
        List<V> degerler = new ArrayList<>();
        for (Girdi<K, V> basBucket : tablo) {
            Girdi<K, V> mevcut = basBucket;
            while (mevcut != null) {
                degerler.add(mevcut.deger);
                mevcut = mevcut.sonraki;
            }
        }
        return degerler;
    }

    // yükleme faktörü aşılınca kapasiteyi 2 katına çıkar - O(n)
    @SuppressWarnings("unchecked")
    private void yenidenBoyutlandir() {
        int eskiKapasite = kapasite;
        kapasite = kapasite * 2;
        Girdi<K, V>[] eskiTablo = tablo;
        tablo = new Girdi[kapasite];
        boyut = 0;

        for (int i = 0; i < eskiKapasite; i++) {
            Girdi<K, V> mevcut = eskiTablo[i];
            while (mevcut != null) {
                put(mevcut.anahtar, mevcut.deger);
                mevcut = mevcut.sonraki;
            }
        }
    }

    public void clear() {
        for (int i = 0; i < kapasite; i++) tablo[i] = null;
        boyut = 0;
    }
}
