package com.eotel.model;

import java.time.LocalDateTime;

public class Customer {
    private String musteriId;
    private String ad;
    private String soyad;
    private String email;
    private String telefon;
    private String tcKimlik;
    private LocalDateTime kayitTarihi;
    private int toplamRezervasyon;

    public Customer() {
        this.kayitTarihi = LocalDateTime.now();
        this.toplamRezervasyon = 0;
    }

    public Customer(String musteriId, String ad, String soyad, String email, String telefon) {
        this();
        this.musteriId = musteriId;
        this.ad = ad;
        this.soyad = soyad;
        this.email = email;
        this.telefon = telefon;
    }

    public String getMusteriId() { return musteriId; }
    public void setMusteriId(String musteriId) { this.musteriId = musteriId; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getTamAd() { return ad + " " + soyad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getTcKimlik() { return tcKimlik; }
    public void setTcKimlik(String tcKimlik) { this.tcKimlik = tcKimlik; }

    public LocalDateTime getKayitTarihi() { return kayitTarihi; }
    public void setKayitTarihi(LocalDateTime kayitTarihi) { this.kayitTarihi = kayitTarihi; }

    public int getToplamRezervasyon() { return toplamRezervasyon; }
    public void setToplamRezervasyon(int toplamRezervasyon) { this.toplamRezervasyon = toplamRezervasyon; }
    public void rezervasyonArtir() { this.toplamRezervasyon++; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s", musteriId, getTamAd(), email);
    }
}
