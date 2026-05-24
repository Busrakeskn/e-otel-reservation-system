package com.eotel.ui.javafx;

import com.eotel.manager.HotelSystem;
import com.eotel.model.Branch;
import com.eotel.model.Reservation;
import com.eotel.model.ReservationStatus;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

public class RaporSayfasi extends BorderPane {

    private final HotelSystem sistem;

    public RaporSayfasi(HotelApp app) {
        this.sistem = app.getSistem();
        setStyle("-fx-background-color: #F0F2F5;");
        setPadding(new Insets(28, 32, 28, 32));
        olustur();
    }

    private void olustur() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox icerik = new VBox(20);

        HBox baslik = HotelApp.sayfaBasligi("Raporlar",
                "Tüm şubeler için gelir ve doluluk analizi");

        // Özet kartları
        HBox kartlar = ozetKartlari();

        // Şube gelir tablosu
        VBox gelirTablo = gelirTabloBolumu();

        // Bar grafik - şube bazlı gelir
        VBox grafikBolum = grafikBolumu();

        icerik.getChildren().addAll(baslik, kartlar, gelirTablo, grafikBolum);
        sp.setContent(icerik);
        setCenter(sp);
    }

    private HBox ozetKartlari() {
        HBox kutu = new HBox(16);

        long toplamRez = sistem.tumRezervasyonlar().size();
        long tamamlanan = sistem.tumRezervasyonlar().stream()
                .filter(r -> r.getDurum() == ReservationStatus.TAMAMLANDI).count();
        long iptal = sistem.tumRezervasyonlar().stream()
                .filter(r -> r.getDurum() == ReservationStatus.IPTAL_EDILDI).count();
        double toplamGelir = sistem.tumRezervasyonlar().stream()
                .filter(r -> r.getDurum() == ReservationStatus.TAMAMLANDI
                          || r.getDurum() == ReservationStatus.ONAYLANDI)
                .mapToDouble(Reservation::getToplamTutar).sum();

        kutu.getChildren().addAll(
            HotelApp.istatKart("Toplam Rezervasyon", String.valueOf(toplamRez), "#2980B9"),
            HotelApp.istatKart("Tamamlanan",         String.valueOf(tamamlanan), "#27AE60"),
            HotelApp.istatKart("İptal",               String.valueOf(iptal),     "#C0392B"),
            HotelApp.istatKart("Toplam Gelir",        String.format("%.0f₺", toplamGelir), "#8E44AD")
        );

        for (var k : kutu.getChildren()) {
            HBox.setHgrow(k, Priority.ALWAYS);
            ((VBox) k).setMaxWidth(Double.MAX_VALUE);
        }
        return kutu;
    }

    private VBox gelirTabloBolumu() {
        VBox bolum = new VBox(10);
        bolum.setStyle("-fx-background-color: white; -fx-background-radius: 8;"
                + "-fx-padding: 20;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        Label baslik = new Label("Şube Bazlı Performans");
        baslik.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(8);
        grid.setPadding(new Insets(10, 0, 0, 0));

        // Başlık satırı
        String[] basliklar = {"Şube", "Şehir", "Oda Sayısı", "Aktif Rezerv.", "Toplam Gelir", "Doluluk"};
        for (int i = 0; i < basliklar.length; i++) {
            Label l = new Label(basliklar[i]);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #7F8C8D; -fx-font-size: 12px;");
            grid.add(l, i, 0);
        }

        List<Branch> subeler = sistem.tumSubeler();
        for (int satirNo = 0; satirNo < subeler.size(); satirNo++) {
            Branch s = subeler.get(satirNo);
            int odaSayisi = sistem.subeOdalari(s.getSubeId()).size();
            long aktif    = sistem.subeAktifRezervasyon(s.getSubeId());
            double gelir  = sistem.subeToplam(s.getSubeId());
            double doluluk = sistem.dolulukOrani(s.getSubeId(),
                    LocalDate.now(), LocalDate.now().plusDays(1));

            String[] degerler = {
                s.getAd(),
                s.getSehir(),
                String.valueOf(odaSayisi),
                String.valueOf(aktif),
                String.format("%.0f₺", gelir),
                String.format("%.0f%%", doluluk)
            };

            for (int j = 0; j < degerler.length; j++) {
                Label l = new Label(degerler[j]);
                l.setStyle("-fx-text-fill: #2C3E50; -fx-font-size: 13px;");
                if (j == 5) {
                    l.setStyle(doluluk > 70
                            ? "-fx-text-fill: #27AE60; -fx-font-weight: bold;"
                            : "-fx-text-fill: #E67E22;");
                }
                grid.add(l, j, satirNo + 1);
            }
        }

        if (subeler.isEmpty()) {
            grid.add(new Label("Şube bulunamadı"), 0, 1);
        }

        bolum.getChildren().addAll(baslik, new Separator(), grid);
        return bolum;
    }

    private VBox grafikBolumu() {
        VBox bolum = new VBox(10);
        bolum.setStyle("-fx-background-color: white; -fx-background-radius: 8;"
                + "-fx-padding: 20;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        Label baslik = new Label("Şube Gelir Karşılaştırması");
        baslik.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        CategoryAxis xEksen = new CategoryAxis();
        NumberAxis   yEksen = new NumberAxis();
        xEksen.setLabel("Şube");
        yEksen.setLabel("Gelir (₺)");

        BarChart<String, Number> bar = new BarChart<>(xEksen, yEksen);
        bar.setTitle("");
        bar.setLegendVisible(false);
        bar.setPrefHeight(280);
        bar.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> seri = new XYChart.Series<>();
        seri.setName("Gelir");

        sistem.tumSubeler().forEach(s -> {
            double gelir = sistem.subeToplam(s.getSubeId());
            seri.getData().add(new XYChart.Data<>(s.getAd(), gelir));
        });

        bar.getData().add(seri);

        // Bar rengini kırmızı yap
        bar.setOnMouseEntered(e -> {});
        bar.getStylesheets().add("data:text/css,.chart-bar{-fx-bar-fill:#C0392B;}");

        bolum.getChildren().addAll(baslik, new Separator(), bar);
        return bolum;
    }
}
