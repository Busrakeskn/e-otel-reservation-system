package com.eotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Reservation implements Comparable<Reservation> {
    private String rezervasyonId;
    private String musteriId;
    private String odaId;
    private String subeId;
    private LocalDate giris;
    private LocalDate cikis;
    private int misafirSayisi;
    private ReservationStatus durum;
    private double toplamTutar;
    private double odenenTutar;
    private OdemeDurumu odemeDurumu;
    private LocalDateTime olusturmaTarihi;
    private String notlar;

    public Reservation() {
        this.olusturmaTarihi = LocalDateTime.now();
        this.durum = ReservationStatus.BEKLEMEDE;
        this.odenenTutar = 0;
        this.odemeDurumu = OdemeDurumu.ODENMEDI;
    }

    public Reservation(String rezervasyonId, String musteriId, String odaId,
                       String subeId, LocalDate giris, LocalDate cikis, int misafirSayisi) {
        this();
        this.rezervasyonId = rezervasyonId;
        this.musteriId = musteriId;
        this.odaId = odaId;
        this.subeId = subeId;
        this.giris = giris;
        this.cikis = cikis;
        this.misafirSayisi = misafirSayisi;
    }

    public long getGeceSayisi() {
        if (giris == null || cikis == null) return 0;
        return ChronoUnit.DAYS.between(giris, cikis);
    }

    // iki rezervasyon çakışıyor mu?
    public boolean cakisiyor(LocalDate basla, LocalDate bitis) {
        return giris.isBefore(bitis) && basla.isBefore(cikis);
    }

    @Override
    public int compareTo(Reservation diger) {
        return this.giris.compareTo(diger.giris);
    }

    public String getRezervasyonId() { return rezervasyonId; }
    public void setRezervasyonId(String rezervasyonId) { this.rezervasyonId = rezervasyonId; }

    public String getMusteriId() { return musteriId; }
    public void setMusteriId(String musteriId) { this.musteriId = musteriId; }

    public String getOdaId() { return odaId; }
    public void setOdaId(String odaId) { this.odaId = odaId; }

    public String getSubeId() { return subeId; }
    public void setSubeId(String subeId) { this.subeId = subeId; }

    public LocalDate getGiris() { return giris; }
    public void setGiris(LocalDate giris) { this.giris = giris; }

    public LocalDate getCikis() { return cikis; }
    public void setCikis(LocalDate cikis) { this.cikis = cikis; }

    public int getMisafirSayisi() { return misafirSayisi; }
    public void setMisafirSayisi(int misafirSayisi) { this.misafirSayisi = misafirSayisi; }

    public ReservationStatus getDurum() { return durum; }
    public void setDurum(ReservationStatus durum) { this.durum = durum; }

    public double getToplamTutar() { return toplamTutar; }
    public void setToplamTutar(double toplamTutar) { this.toplamTutar = toplamTutar; }

    public double getOdenenTutar() { return odenenTutar; }
    public void setOdenenTutar(double odenenTutar) { this.odenenTutar = odenenTutar; }

    public OdemeDurumu getOdemeDurumu() { return odemeDurumu; }
    public void setOdemeDurumu(OdemeDurumu odemeDurumu) { this.odemeDurumu = odemeDurumu; }

    public LocalDateTime getOlusturmaTarihi() { return olusturmaTarihi; }
    public void setOlusturmaTarihi(LocalDateTime olusturmaTarihi) { this.olusturmaTarihi = olusturmaTarihi; }

    public String getNotlar() { return notlar; }
    public void setNotlar(String notlar) { this.notlar = notlar; }

    @Override
    public String toString() {
        return String.format("[%s] Oda:%s Müşteri:%s %s→%s %s %.0f₺",
                rezervasyonId, odaId, musteriId, giris, cikis, durum, toplamTutar);
    }
}
