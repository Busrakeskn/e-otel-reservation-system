package com.eotel.ui.javafx;

import com.eotel.manager.HotelSystem;
import com.eotel.model.Reservation;
import com.eotel.model.ReservationStatus;
import com.eotel.model.Room;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class MusaitlikSayfasi extends BorderPane {

    private final HotelSystem sistem;
    private LocalDate haftaBasi;
    private String seciliSubeId;

    // Renk göstergeleri
    private static final String RENK_MUSAIT = "#2ECC71";
    private static final String RENK_DOLU   = "#E74C3C";
    private static final String RENK_BEKLEME= "#F39C12";

    public MusaitlikSayfasi(HotelApp app) {
        this.sistem = app.getSistem();
        this.haftaBasi = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        setStyle("-fx-background-color: #F0F2F5;");
        setPadding(new Insets(28, 32, 28, 32));
        olustur();
    }

    private void olustur() {
        VBox icerik = new VBox(16);

        // Başlık + kontroller
        HBox ust = new HBox(12);
        ust.setAlignment(Pos.CENTER_LEFT);

        VBox baslikKutu = new VBox(4);
        Label b = new Label("Müsaitlik Takvimi");
        b.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        Label a = new Label("Oda doluluk durumunu haftalık görüntüleyin");
        a.setStyle("-fx-font-size: 13px; -fx-text-fill: #7F8C8D;");
        baslikKutu.getChildren().addAll(b, a);
        HBox.setHgrow(baslikKutu, Priority.ALWAYS);

        // Şube seçici
        ComboBox<String> subeCb = new ComboBox<>();
        subeCb.getItems().add("Tüm Şubeler");
        sistem.tumSubeler().forEach(s -> subeCb.getItems()
                .add(s.getSubeId() + " - " + s.getAd()));
        subeCb.setValue("Tüm Şubeler");
        subeCb.setStyle("-fx-font-size: 13px;");

        // Hafta navigasyonu
        Button onceki = HotelApp.grisBtn("◀ Önceki Hafta");
        Button sonraki = HotelApp.kirmiziBtnOlustur("Sonraki Hafta ▶");

        ust.getChildren().addAll(baslikKutu, subeCb, onceki, sonraki);

        // Takvim alanı
        ScrollPane takvimScroll = new ScrollPane();
        takvimScroll.setFitToWidth(true);
        takvimScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Runnable yenile = () -> {
            String secim = subeCb.getValue();
            seciliSubeId = secim.equals("Tüm Şubeler") ? null : secim.split(" - ")[0];
            takvimScroll.setContent(takvimOlustur());
        };

        onceki.setOnAction(e -> { haftaBasi = haftaBasi.minusWeeks(1); yenile.run(); });
        sonraki.setOnAction(e -> { haftaBasi = haftaBasi.plusWeeks(1);  yenile.run(); });
        subeCb.setOnAction(e -> yenile.run());

        yenile.run();

        // Lejant
        HBox lejant = lejantOlustur();

        icerik.getChildren().addAll(ust, lejant, takvimScroll);
        VBox.setVgrow(takvimScroll, Priority.ALWAYS);
        setCenter(icerik);
    }

    private VBox takvimOlustur() {
        VBox tablo = new VBox(2);
        tablo.setStyle("-fx-background-color: white; -fx-background-radius: 8;"
                + "-fx-padding: 12;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        // Başlık satırı (gün isimleri)
        HBox baslikSatiri = new HBox(2);
        baslikSatiri.getChildren().add(sutunBaslik("Oda", 160));
        for (int i = 0; i < 7; i++) {
            LocalDate gun = haftaBasi.plusDays(i);
            String gunText = gun.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, new Locale("tr")) + "\n" + gun.getDayOfMonth();
            Label l = new Label(gunText);
            l.setMinWidth(80); l.setMaxWidth(80);
            l.setAlignment(Pos.CENTER);
            l.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #2C3E50;"
                    + "-fx-background-color: #ECF0F1; -fx-padding: 6; -fx-alignment: center;");
            baslikSatiri.getChildren().add(l);
        }
        tablo.getChildren().add(baslikSatiri);

        // Oda satırları
        List<Room> odalar = seciliSubeId != null
                ? sistem.subeOdalari(seciliSubeId)
                : sistem.tumOdalar();

        if (odalar.isEmpty()) {
            Label bos = new Label("Oda bulunamadı");
            bos.setStyle("-fx-padding: 20; -fx-text-fill: #7F8C8D;");
            tablo.getChildren().add(bos);
            return tablo;
        }

        for (Room oda : odalar) {
            HBox satir = new HBox(2);
            satir.setAlignment(Pos.CENTER_LEFT);

            // Oda bilgisi
            VBox odaKutu = new VBox(2);
            odaKutu.setMinWidth(160); odaKutu.setMaxWidth(160);
            odaKutu.setPadding(new Insets(6));
            odaKutu.setStyle("-fx-background-color: #FAFAFA;");
            Label odaAd = new Label("Oda " + oda.getOdaNumarasi());
            odaAd.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            Label odaTip = new Label(oda.getTip().getGoruntulenenAd());
            odaTip.setStyle("-fx-font-size: 11px; -fx-text-fill: #7F8C8D;");
            odaKutu.getChildren().addAll(odaAd, odaTip);
            satir.getChildren().add(odaKutu);

            for (int i = 0; i < 7; i++) {
                LocalDate gun = haftaBasi.plusDays(i);
                String renk = gunRengi(oda.getOdaId(), gun);
                String durum = renk.equals(RENK_MUSAIT) ? "Müsait"
                        : renk.equals(RENK_DOLU) ? "Dolu" : "Bekleme";

                StackPane hucre = new StackPane();
                hucre.setMinWidth(80); hucre.setMaxWidth(80);
                hucre.setMinHeight(44);
                hucre.setStyle("-fx-background-color: " + renk + "22; -fx-border-color: "
                        + renk + "44; -fx-border-width: 1;");

                Rectangle dikdortgen = new Rectangle(6, 28);
                dikdortgen.setFill(Color.web(renk));
                dikdortgen.setArcWidth(4); dikdortgen.setArcHeight(4);

                Label durumLabel = new Label(durum);
                durumLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + renk + ";");

                VBox hucreIcerik = new VBox(2, dikdortgen, durumLabel);
                hucreIcerik.setAlignment(Pos.CENTER);
                hucre.getChildren().add(hucreIcerik);
                satir.getChildren().add(hucre);
            }
            tablo.getChildren().add(satir);
        }
        return tablo;
    }

    private String gunRengi(String odaId, LocalDate gun) {
        LocalDate sonraki = gun.plusDays(1);

        // Bekleme listesinde mi?
        var beklemeListe = sistem.beklemeListe(odaId);
        for (Reservation r : beklemeListe) {
            if (r.cakisiyor(gun, sonraki)) return RENK_BEKLEME;
        }

        // Onaylı rezervasyonla çakışıyor mu?
        for (Reservation r : sistem.tumRezervasyonlar()) {
            if (r.getOdaId().equals(odaId)
                    && r.getDurum() == ReservationStatus.ONAYLANDI
                    && r.cakisiyor(gun, sonraki)) {
                return RENK_DOLU;
            }
        }
        return RENK_MUSAIT;
    }

    private Label sutunBaslik(String metin, double genislik) {
        Label l = new Label(metin);
        l.setMinWidth(genislik); l.setMaxWidth(genislik);
        l.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #2C3E50;"
                + "-fx-background-color: #ECF0F1; -fx-padding: 6;");
        return l;
    }

    private HBox lejantOlustur() {
        HBox kutu = new HBox(20);
        kutu.setAlignment(Pos.CENTER_LEFT);
        kutu.getChildren().addAll(
            lejantOge(RENK_MUSAIT, "Müsait"),
            lejantOge(RENK_DOLU,   "Dolu"),
            lejantOge(RENK_BEKLEME,"Bekleme")
        );
        return kutu;
    }

    private HBox lejantOge(String renk, String metin) {
        Rectangle r = new Rectangle(14, 14);
        r.setFill(Color.web(renk));
        r.setArcWidth(3); r.setArcHeight(3);
        Label l = new Label(metin);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        HBox k = new HBox(6, r, l);
        k.setAlignment(Pos.CENTER_LEFT);
        return k;
    }
}
