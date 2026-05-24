package com.eotel.model;

public enum RoomType {
    STANDART("Standart Oda", 2, 1500),
    DELUXE("Deluxe Oda", 3, 2500),
    SUPERIOR("Superior Oda", 4, 3500),
    SUIT("Süit Oda", 4, 5000),
    AILE("Aile Odası", 6, 4000);

    private final String goruntulenenAd;
    private final int maksKapasite;
    private final double temelFiyat;

    RoomType(String goruntulenenAd, int maksKapasite, double temelFiyat) {
        this.goruntulenenAd = goruntulenenAd;
        this.maksKapasite = maksKapasite;
        this.temelFiyat = temelFiyat;
    }

    public String getGoruntulenenAd() { return goruntulenenAd; }
    public int getMaksKapasite() { return maksKapasite; }
    public double getTemelFiyat() { return temelFiyat; }

    @Override
    public String toString() { return goruntulenenAd; }
}
