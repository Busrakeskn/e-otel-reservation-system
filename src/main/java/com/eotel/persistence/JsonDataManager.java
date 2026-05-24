package com.eotel.persistence;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.eotel.manager.HotelSystem;
import com.eotel.model.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class JsonDataManager {

    private static final String VERI_DIZINI = "data";
    private static final String SUBELER_DOSYA = "data/subeler.json";
    private static final String ODALAR_DOSYA = "data/odalar.json";
    private static final String MUSTERILER_DOSYA = "data/musteriler.json";
    private static final String REZERVASYONLAR_DOSYA = "data/rezervasyonlar.json";

    private final Gson gson;

    public JsonDataManager() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    // ---- Kaydet ----

    public void tumunuKaydet(HotelSystem sistem) {
        dizinOlustur();
        subelerKaydet(sistem.tumSubeler());
        odalarKaydet(sistem.tumOdalar());
        musterilerKaydet(sistem.tumMusteriler());
        rezervasyonlarKaydet(sistem.tumRezervasyonlar());
    }

    private void subelerKaydet(List<Branch> subeler) {
        jsonYaz(SUBELER_DOSYA, subeler);
    }

    private void odalarKaydet(List<Room> odalar) {
        jsonYaz(ODALAR_DOSYA, odalar);
    }

    private void musterilerKaydet(List<Customer> musteriler) {
        jsonYaz(MUSTERILER_DOSYA, musteriler);
    }

    private void rezervasyonlarKaydet(List<Reservation> rezervasyonlar) {
        jsonYaz(REZERVASYONLAR_DOSYA, rezervasyonlar);
    }

    // ---- Yükle ----

    public void tumunuYukle(HotelSystem sistem) {
        dizinOlustur();

        List<Branch> subeler = jsonOku(SUBELER_DOSYA, new TypeToken<List<Branch>>(){}.getType());
        if (subeler != null) subeler.forEach(sistem::subeEkle);

        List<Room> odalar = jsonOku(ODALAR_DOSYA, new TypeToken<List<Room>>(){}.getType());
        if (odalar != null) odalar.forEach(sistem::odaEkle);

        List<Customer> musteriler = jsonOku(MUSTERILER_DOSYA, new TypeToken<List<Customer>>(){}.getType());
        if (musteriler != null) musteriler.forEach(sistem::musteriEkle);

        List<Reservation> rezervasyonlar = jsonOku(REZERVASYONLAR_DOSYA, new TypeToken<List<Reservation>>(){}.getType());
        if (rezervasyonlar != null) {
            for (Reservation rez : rezervasyonlar) {
                sistem.getRezervasyonMap().put(rez.getRezervasyonId(), rez);

                // Onaylı rezervasyonları interval tree'ye geri ekle
                if (rez.getDurum() == ReservationStatus.ONAYLANDI) {
                    sistem.getIntervalTree().insert(new com.eotel.datastructures.IntervalTree.Aralik(
                            rez.getGiris(), rez.getCikis(), rez.getOdaId(), rez.getRezervasyonId()));
                }
                // Tamamlananları BST'ye ekle
                if (rez.getDurum() == ReservationStatus.TAMAMLANDI) {
                    sistem.getTamamlananBST().insert(rez);
                }
                // Bekleme listesindeki rezervasyonları linked list'e geri yükle
                if (rez.getDurum() == ReservationStatus.BEKLEME_LISTESI) {
                    sistem.beklemeListesineYukle(rez);
                }
            }
        }
    }

    // ---- Yardımcı ----

    private void jsonYaz(String dosyaYolu, Object nesne) {
        try (Writer yazar = new OutputStreamWriter(
                new FileOutputStream(dosyaYolu), StandardCharsets.UTF_8)) {
            gson.toJson(nesne, yazar);
        } catch (IOException e) {
            System.err.println("Kayıt hatası [" + dosyaYolu + "]: " + e.getMessage());
        }
    }

    private <T> T jsonOku(String dosyaYolu, Type tip) {
        File dosya = new File(dosyaYolu);
        if (!dosya.exists()) return null;
        try (Reader okuyucu = new InputStreamReader(
                new FileInputStream(dosya), StandardCharsets.UTF_8)) {
            return gson.fromJson(okuyucu, tip);
        } catch (IOException e) {
            System.err.println("Okuma hatası [" + dosyaYolu + "]: " + e.getMessage());
            return null;
        }
    }

    private void dizinOlustur() {
        File dizin = new File(VERI_DIZINI);
        if (!dizin.exists()) dizin.mkdirs();
    }

    // ---- LocalDate / LocalDateTime adaptörleri ----

    private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext ctx) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            return LocalDate.parse(json.getAsString());
        }
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext ctx) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            return LocalDateTime.parse(json.getAsString());
        }
    }
}
