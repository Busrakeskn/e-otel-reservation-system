package com.eotel.model;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String odaId;
    private String subeId;
    private int odaNumarasi;
    private RoomType tip;
    private double gecelikFiyat;
    private int kat;
    private String aciklama;
    private List<String> olanaklar;
    private boolean aktif;

    public Room() {
        this.olanaklar = new ArrayList<>();
        this.aktif = true;
    }

    public Room(String odaId, String subeId, int odaNumarasi, RoomType tip, double gecelikFiyat, int kat) {
        this();
        this.odaId = odaId;
        this.subeId = subeId;
        this.odaNumarasi = odaNumarasi;
        this.tip = tip;
        this.gecelikFiyat = gecelikFiyat;
        this.kat = kat;
    }

    public String getOdaId() { return odaId; }
    public void setOdaId(String odaId) { this.odaId = odaId; }

    public String getSubeId() { return subeId; }
    public void setSubeId(String subeId) { this.subeId = subeId; }

    public int getOdaNumarasi() { return odaNumarasi; }
    public void setOdaNumarasi(int odaNumarasi) { this.odaNumarasi = odaNumarasi; }

    public RoomType getTip() { return tip; }
    public void setTip(RoomType tip) { this.tip = tip; }

    public double getGecelikFiyat() { return gecelikFiyat; }
    public void setGecelikFiyat(double gecelikFiyat) { this.gecelikFiyat = gecelikFiyat; }

    public int getKat() { return kat; }
    public void setKat(int kat) { this.kat = kat; }

    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }

    public List<String> getOlanaklar() { return olanaklar; }
    public void setOlanaklar(List<String> olanaklar) { this.olanaklar = olanaklar; }

    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }

    public void olanak(String o) {
        if (!olanaklar.contains(o)) olanaklar.add(o);
    }

    @Override
    public String toString() {
        return String.format("Oda #%d [%s] - Kat %d - %.0f₺/gece", odaNumarasi, tip.getGoruntulenenAd(), kat, gecelikFiyat);
    }
}
