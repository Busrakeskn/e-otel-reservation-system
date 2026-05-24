package com.eotel.ui.javafx;

import com.eotel.manager.HotelSystem;
import com.eotel.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.application.Platform;

import java.time.LocalDate;
import java.util.List;

public class RezervasyonSayfasi extends BorderPane {

    private final HotelSystem sistem;
    private TableView<Reservation> tablo;

    public RezervasyonSayfasi(HotelApp app) {
        this.sistem = app.getSistem();
        setStyle("-fx-background-color: #F0F2F5;");
        setPadding(new Insets(28, 32, 28, 32));
        olustur();
    }

    private void olustur() {
        // Başlık
        HBox baslikSatiri = HotelApp.sayfaBasligi("Rezervasyonlar",
                sistem.tumRezervasyonlar().size() + " kayıt");

        Button yeniBtn = HotelApp.kirmiziBtnOlustur("+ Yeni Rezervasyon");
        yeniBtn.setOnAction(e -> yeniRezervasyonDiyalog());
        HBox.setHgrow(baslikSatiri.getChildren().get(0), Priority.ALWAYS);
        baslikSatiri.getChildren().add(yeniBtn);
        baslikSatiri.setAlignment(Pos.CENTER_LEFT);

        // Filtre satırı
        HBox filtreler = filtreSatiriOlustur();

        // Tablo
        tablo = tabloOlustur();
        tabloVeriYukle(null);

        VBox icerik = new VBox(12, baslikSatiri, filtreler, tablo);
        VBox.setVgrow(tablo, Priority.ALWAYS);
        setCenter(icerik);
    }

    private HBox filtreSatiriOlustur() {
        // Durum hap butonları
        ToggleGroup grup = new ToggleGroup();
        HBox haplar = new HBox(6);

        String[] durumlar = {"Tümü", "Onaylandı", "Beklemede", "Bekleme Listesi", "Tamamlandı", "İptal Edildi"};
        for (String d : durumlar) {
            ToggleButton tb = new ToggleButton(d);
            tb.setToggleGroup(grup);
            tb.setStyle("-fx-background-color: #ECF0F1; -fx-text-fill: #2C3E50;"
                    + "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 14 6 14;"
                    + "-fx-background-radius: 20;");
            tb.selectedProperty().addListener((obs, ov, nv) -> {
                if (nv) tb.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;"
                        + "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 14 6 14;"
                        + "-fx-background-radius: 20;");
                else    tb.setStyle("-fx-background-color: #ECF0F1; -fx-text-fill: #2C3E50;"
                        + "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 14 6 14;"
                        + "-fx-background-radius: 20;");
            });
            if (d.equals("Tümü")) tb.setSelected(true);
            haplar.getChildren().add(tb);
        }

        TextField aramaField = new TextField();
        aramaField.setPromptText("🔍  Rezervasyon veya müşteri ara...");
        aramaField.setPrefWidth(250);
        aramaField.setStyle("-fx-background-radius: 20; -fx-padding: 7 14 7 14; -fx-font-size: 12px;");

        grup.selectedToggleProperty().addListener((obs, ov, nv) -> {
            if (nv == null) return;
            String secim = ((ToggleButton) nv).getText();
            String arama = aramaField.getText().trim().toLowerCase();
            tabloVeriYukle(secim.equals("Tümü") ? null : secim);
            if (!arama.isEmpty()) {
                tablo.setItems(FXCollections.observableArrayList(
                    tablo.getItems().filtered(r -> aramaEsles(r, arama))
                ));
            }
        });

        aramaField.textProperty().addListener((obs, ov, nv) -> {
            ToggleButton secili = (ToggleButton) grup.getSelectedToggle();
            String durum = secili != null ? secili.getText() : "Tümü";
            tabloVeriYukle(durum.equals("Tümü") ? null : durum);
            if (!nv.isBlank()) {
                String aranan = nv.toLowerCase();
                tablo.setItems(FXCollections.observableArrayList(
                    tablo.getItems().filtered(r -> aramaEsles(r, aranan))
                ));
            }
        });

        Region bosluk = new Region();
        HBox.setHgrow(bosluk, Priority.ALWAYS);

        HBox kutu = new HBox(10);
        kutu.setAlignment(Pos.CENTER_LEFT);
        kutu.setPadding(new Insets(4, 0, 8, 0));
        kutu.getChildren().addAll(haplar, bosluk, aramaField);
        return kutu;
    }

    @SuppressWarnings("unchecked")
    private TableView<Reservation> tabloOlustur() {
        TableView<Reservation> t = new TableView<>();
        t.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Reservation, String> idKol = sutun("Rezervasyon ID", 140,
                r -> r.getRezervasyonId());
        TableColumn<Reservation, String> musteriKol = sutun("Müşteri", 150, r -> {
            Customer m = sistem.musteriBul(r.getMusteriId());
            return m != null ? m.getTamAd() : r.getMusteriId();
        });
        TableColumn<Reservation, String> odaKol = sutun("Oda ID", 130,
                r -> r.getOdaId());
        TableColumn<Reservation, String> girisKol = sutun("Giriş", 100,
                r -> r.getGiris().toString());
        TableColumn<Reservation, String> cikisKol = sutun("Çıkış", 100,
                r -> r.getCikis().toString());
        TableColumn<Reservation, String> durumKol = sutun("Durum", 120,
                r -> r.getDurum().getLabel());
        TableColumn<Reservation, String> tutarKol = sutun("Tutar", 100,
                r -> String.format("%.0f₺", r.getToplamTutar()));

        // Durum rozeti
        durumKol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                setGraphic(HotelApp.durumRozeti(item));
            }
        });

        // İşlem sütunu
        TableColumn<Reservation, String> islemKol = new TableColumn<>("İşlem");
        islemKol.setPrefWidth(160);
        islemKol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDurum().name()));
        islemKol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) { setGraphic(null); return; }

                if (item.equals(ReservationStatus.ONAYLANDI.name())) {
                    Button tBtn = HotelApp.kirmiziBtnOlustur("Tamamla");
                    Button iBtn = HotelApp.grisBtn("İptal");
                    tBtn.setPadding(new Insets(4, 10, 4, 10));
                    iBtn.setPadding(new Insets(4, 10, 4, 10));
                    tBtn.setOnAction(e -> {
                        Reservation r = getTableView().getItems().get(getIndex());
                        try {
                            sistem.rezervasyonTamamla(r.getRezervasyonId());
                            Platform.runLater(() -> { tabloVeriYukle(null); tablo.refresh(); });
                        } catch (Exception ex) {
                            HotelApp.uyariGoster("Hata", ex.getMessage());
                        }
                    });
                    iBtn.setOnAction(e -> {
                        Reservation r = getTableView().getItems().get(getIndex());
                        if (HotelApp.onayDiyalog("Rezervasyon iptal edilsin mi?")) {
                            try {
                                sistem.rezervasyonIptalEt(r.getRezervasyonId());
                                Platform.runLater(() -> { tabloVeriYukle(null); tablo.refresh(); });
                            } catch (Exception ex) {
                                HotelApp.uyariGoster("Hata", ex.getMessage());
                            }
                        }
                    });
                    setGraphic(new HBox(6, tBtn, iBtn));
                } else if (item.equals(ReservationStatus.BEKLEME_LISTESI.name())) {
                    Button iBtn = HotelApp.grisBtn("İptal");
                    iBtn.setPadding(new Insets(4, 10, 4, 10));
                    iBtn.setOnAction(e -> {
                        Reservation r = getTableView().getItems().get(getIndex());
                        if (HotelApp.onayDiyalog("Rezervasyon iptal edilsin mi?")) {
                            try {
                                sistem.rezervasyonIptalEt(r.getRezervasyonId());
                                Platform.runLater(() -> { tabloVeriYukle(null); tablo.refresh(); });
                            } catch (Exception ex) {
                                HotelApp.uyariGoster("Hata", ex.getMessage());
                            }
                        }
                    });
                    setGraphic(new HBox(6, iBtn));
                } else {
                    setGraphic(null);
                }
            }
        });

        t.getColumns().addAll(idKol, musteriKol, odaKol, girisKol, cikisKol,
                durumKol, tutarKol, islemKol);
        return t;
    }

    private boolean aramaEsles(Reservation r, String aranan) {
        if (r.getRezervasyonId().toLowerCase().contains(aranan)) return true;
        if (r.getMusteriId().toLowerCase().contains(aranan)) return true;
        Customer m = sistem.musteriBul(r.getMusteriId());
        if (m != null && m.getTamAd().toLowerCase().contains(aranan)) return true;
        return false;
    }

    private void tabloVeriYukle(String durumFiltre) {
        List<Reservation> hepsi = sistem.tumRezervasyonlar();
        if (durumFiltre != null) {
            hepsi = hepsi.stream()
                    .filter(r -> r.getDurum().getLabel().equals(durumFiltre))
                    .toList();
        }
        tablo.setItems(FXCollections.observableArrayList(hepsi));
    }

    private <T> TableColumn<Reservation, String> sutun(String baslik, double genislik,
                                                        java.util.function.Function<Reservation, String> deger) {
        TableColumn<Reservation, String> kol = new TableColumn<>(baslik);
        kol.setPrefWidth(genislik);
        kol.setCellValueFactory(data -> new SimpleStringProperty(deger.apply(data.getValue())));
        return kol;
    }

    private void yeniRezervasyonDiyalog() {
        Dialog<Reservation> diyalog = new Dialog<>();
        diyalog.setTitle("Yeni Rezervasyon");
        diyalog.setHeaderText(null);

        ButtonType ekleBtn = new ButtonType("Rezervasyon Yap", ButtonBar.ButtonData.OK_DONE);
        diyalog.getDialogPane().getButtonTypes().addAll(ekleBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> musteriCb = new ComboBox<>();
        sistem.tumMusteriler().forEach(m -> musteriCb.getItems()
                .add(m.getMusteriId() + " - " + m.getTamAd()));
        musteriCb.setPromptText("Müşteri seçin");
        musteriCb.setPrefWidth(280);

        ComboBox<String> odaCb = new ComboBox<>();
        sistem.tumOdalar().forEach(o -> odaCb.getItems()
                .add(o.getOdaId() + " - " + o.getTip().getGoruntulenenAd()));
        odaCb.setPromptText("Oda seçin");
        odaCb.setPrefWidth(280);

        DatePicker girisPicker = new DatePicker(LocalDate.now());
        DatePicker cikisPicker = new DatePicker(LocalDate.now().plusDays(1));
        Spinner<Integer> misafirSp = new Spinner<>(1, 10, 1);

        grid.addRow(0, new Label("Müşteri:"),    musteriCb);
        grid.addRow(1, new Label("Oda:"),        odaCb);
        grid.addRow(2, new Label("Giriş:"),      girisPicker);
        grid.addRow(3, new Label("Çıkış:"),      cikisPicker);
        grid.addRow(4, new Label("Misafir:"),    misafirSp);

        diyalog.getDialogPane().setContent(grid);

        diyalog.setResultConverter(tip -> {
            if (tip != ekleBtn) return null;
            if (musteriCb.getValue() == null || odaCb.getValue() == null) {
                HotelApp.uyariGoster("Eksik Bilgi", "Lütfen müşteri ve oda seçin");
                return null;
            }
            String musteriId = musteriCb.getValue().split(" - ")[0];
            String odaId     = odaCb.getValue().split(" - ")[0];
            try {
                return sistem.rezervasyonYap(musteriId, odaId,
                        girisPicker.getValue(), cikisPicker.getValue(),
                        misafirSp.getValue());
            } catch (Exception e) {
                HotelApp.uyariGoster("Hata", e.getMessage());
                return null;
            }
        });

        diyalog.showAndWait().ifPresent(r -> {
            if (r.getDurum() == ReservationStatus.BEKLEME_LISTESI)
                HotelApp.bilgiGoster("Bekleme Listesi",
                        "Oda dolu, bekleme listesine alındı:\n" + r.getRezervasyonId());
            else
                HotelApp.bilgiGoster("Başarılı",
                        "Rezervasyon oluşturuldu: " + r.getRezervasyonId()
                        + "\nToplam: " + String.format("%.0f₺", r.getToplamTutar()));
            tabloVeriYukle(null);
        });
    }
}
