package com.eotel.datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Çift yönlü bağlı liste - bekleme listesi yönetimi için
 *
 * Zaman Karmaşıklığı:
 *   sona ekle (addLast)   : O(1) - kuyruk işaretçisi var
 *   başa ekle (addFirst)  : O(1)
 *   baştan sil (removeFirst): O(1)
 *   indeks ile eriş       : O(n)
 *   arama (contains)      : O(n)
 *
 * Alan Karmaşıklığı: O(n)
 */
public class CustomLinkedList<T> implements Iterable<T> {

    private static class Dugum<T> {
        T veri;
        Dugum<T> onceki;
        Dugum<T> sonraki;

        Dugum(T veri) {
            this.veri = veri;
        }
    }

    private Dugum<T> bas;
    private Dugum<T> son;
    private int boyut;

    public CustomLinkedList() {
        bas = null;
        son = null;
        boyut = 0;
    }

    // O(1)
    public void addFirst(T veri) {
        Dugum<T> yeni = new Dugum<>(veri);
        if (bosmu()) {
            bas = son = yeni;
        } else {
            yeni.sonraki = bas;
            bas.onceki = yeni;
            bas = yeni;
        }
        boyut++;
    }

    // O(1) - kuyruk işaretçisi sayesinde
    public void addLast(T veri) {
        Dugum<T> yeni = new Dugum<>(veri);
        if (bosmu()) {
            bas = son = yeni;
        } else {
            yeni.onceki = son;
            son.sonraki = yeni;
            son = yeni;
        }
        boyut++;
    }

    // O(1)
    public T removeFirst() {
        if (bosmu()) throw new NoSuchElementException("Liste boş");
        T veri = bas.veri;
        if (boyut == 1) {
            bas = son = null;
        } else {
            bas = bas.sonraki;
            bas.onceki = null;
        }
        boyut--;
        return veri;
    }

    // O(1)
    public T removeLast() {
        if (bosmu()) throw new NoSuchElementException("Liste boş");
        T veri = son.veri;
        if (boyut == 1) {
            bas = son = null;
        } else {
            son = son.onceki;
            son.sonraki = null;
        }
        boyut--;
        return veri;
    }

    // O(n) - indeks ile erişim
    public T get(int index) {
        if (index < 0 || index >= boyut)
            throw new IndexOutOfBoundsException("Geçersiz indeks: " + index);
        Dugum<T> mevcut = bas;
        for (int i = 0; i < index; i++) {
            mevcut = mevcut.sonraki;
        }
        return mevcut.veri;
    }

    // O(n) - belirli bir elemanı siler
    public boolean remove(T veri) {
        Dugum<T> mevcut = bas;
        while (mevcut != null) {
            if (mevcut.veri.equals(veri)) {
                if (mevcut.onceki != null) mevcut.onceki.sonraki = mevcut.sonraki;
                else bas = mevcut.sonraki;

                if (mevcut.sonraki != null) mevcut.sonraki.onceki = mevcut.onceki;
                else son = mevcut.onceki;

                boyut--;
                return true;
            }
            mevcut = mevcut.sonraki;
        }
        return false;
    }

    public T peekFirst() {
        if (bosmu()) throw new NoSuchElementException("Liste boş");
        return bas.veri;
    }

    public T peekLast() {
        if (bosmu()) throw new NoSuchElementException("Liste boş");
        return son.veri;
    }

    public boolean bosmu() { return boyut == 0; }
    public int getBoyut() { return boyut; }
    public int size() { return boyut; }

    public void temizle() {
        bas = son = null;
        boyut = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Dugum<T> simdiki = bas;

            @Override
            public boolean hasNext() { return simdiki != null; }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T veri = simdiki.veri;
                simdiki = simdiki.sonraki;
                return veri;
            }
        };
    }

    @Override
    public String toString() {
        if (bosmu()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        Dugum<T> mevcut = bas;
        while (mevcut != null) {
            sb.append(mevcut.veri);
            if (mevcut.sonraki != null) sb.append(" -> ");
            mevcut = mevcut.sonraki;
        }
        sb.append("]");
        return sb.toString();
    }
}
