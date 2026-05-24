package com.eotel.model;

public enum ReservationStatus {
    BEKLEMEDE("Beklemede"),
    ONAYLANDI("Onaylandı"),
    IPTAL_EDILDI("İptal Edildi"),
    TAMAMLANDI("Tamamlandı"),
    BEKLEME_LISTESI("Bekleme Listesi");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}
