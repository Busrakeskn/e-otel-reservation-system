package com.eotel.model;

public class Branch {
    private String subeId;
    private String ad;
    private String sehir;
    private String adres;
    private String telefon;
    private String yoneticiAdi;
    private int toplamOda;
    private boolean aktif;

    public Branch() {
        this.aktif = true;
    }

    public Branch(String subeId, String ad, String sehir, String adres) {
        this();
        this.subeId = subeId;
        this.ad = ad;
        this.sehir = sehir;
        this.adres = adres;
    }

    public String getSubeId() { return subeId; }
    public void setSubeId(String subeId) { this.subeId = subeId; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSehir() { return sehir; }
    public void setSehir(String sehir) { this.sehir = sehir; }

    public String getAdres() { return adres; }
    public void setAdres(String adres) { this.adres = adres; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getYoneticiAdi() { return yoneticiAdi; }
    public void setYoneticiAdi(String yoneticiAdi) { this.yoneticiAdi = yoneticiAdi; }

    public int getToplamOda() { return toplamOda; }
    public void setToplamOda(int toplamOda) { this.toplamOda = toplamOda; }

    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s", subeId, ad, sehir);
    }
}
