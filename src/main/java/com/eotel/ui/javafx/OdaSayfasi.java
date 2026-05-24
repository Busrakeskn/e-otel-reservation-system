package com.eotel.ui.javafx;

import com.eotel.manager.HotelSystem;
import com.eotel.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class OdaSayfasi extends BorderPane {

    private final HotelSystem sistem;
    private TabPane tabPane;

    public OdaSayfasi(HotelApp app) {
        this.sistem = app.getSistem();
        setStyle("-fx-background-color: #F0F2F5;");
        setPadding(new Insets(28, 32, 28, 32));
        olustur();
    }

    private void olustur() {
        HBox baslikSatiri = HotelApp.sayfaBasligi("Odalar",
                sistem.tumOdalar().size() + " kayıt");

        Button yeniBtn = HotelApp.kirmiziBtnOlustur("+ Oda Ekle");
        yeniBtn.setOnAction(e -> odaEkleDiyalog());
        baslikSatiri.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(baslikSatiri.getChildren().get(0), Priority.ALWAYS);
        baslikSatiri.getChildren().add(yeniBtn);

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        sekmeleriYenile();

        VBox icerik = new VBox(12, baslikSatiri, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        setCenter(icerik);
    }

    private void sekmeleriYenile() {
        int secilenIndex = tabPane.getSelectionModel().getSelectedIndex();
        tabPane.getTabs().clear();

        // "Tüm Odalar" sekmesi
        Tab tumTab = sekmesiOlustur("Tüm Odalar", null, sistem.tumOdalar());
        tabPane.getTabs().add(tumTab);

        // Her şube için ayrı sekme
        for (Branch sube : sistem.tumSubeler()) {
            List<Room> odalar = sistem.subeOdalari(sube.getSubeId());
            String baslik = sube.getAd() + "  (" + odalar.size() + ")";
            Tab tab = sekmesiOlustur(baslik, sube.getSubeId(), odalar);
            tabPane.getTabs().add(tab);
        }

        // Önceki sekmeyi seç, yoksa ilkini
        int toplamSekme = tabPane.getTabs().size();
        if (secilenIndex >= 0 && secilenIndex < toplamSekme) {
            tabPane.getSelectionModel().select(secilenIndex);
        }
    }

    private Tab sekmesiOlustur(String baslik, String subeId, List<Room> odalar) {
        Tab tab = new Tab(baslik);

        TableView<Room> tablo = tabloOlustur(subeId == null);
        tablo.setItems(FXCollections.observableArrayList(odalar));

        if (odalar.isEmpty()) {
            tablo.setPlaceholder(subeBosMesaji());
        }

        StackPane kap = new StackPane(tablo);
        kap.setPadding(new Insets(12, 0, 0, 0));
        tab.setContent(kap);
        return tab;
    }

    private Label subeBosMesaji() {
        Label l = new Label("Bu şubeye henüz oda eklenmemiş.");
        l.setStyle("-fx-text-fill: #ADB5BD; -fx-font-size: 13px;");
        return l;
    }

    @SuppressWarnings("unchecked")
    private TableView<Room> tabloOlustur(boolean subeIdSutunu) {
        TableView<Room> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        if (subeIdSutunu) {
            t.getColumns().add(sutun("Şube", 120, o -> {
                Branch b = sistem.subeBul(o.getSubeId());
                return b != null ? b.getAd() : o.getSubeId();
            }));
        }

        t.getColumns().addAll(
            sutun("Oda No",    70,  o -> String.valueOf(o.getOdaNumarasi())),
            sutun("Tip",      150,  o -> o.getTip().getGoruntulenenAd()),
            sutun("Kat",       60,  o -> String.valueOf(o.getKat())),
            sutun("Fiyat",    110,  o -> String.format("%.0f ₺", o.getGecelikFiyat())),
            sutun("Kapasite",  90,  o -> o.getTip().getMaksKapasite() + " kişi"),
            sutun("Oda ID",   150,  o -> o.getOdaId())
        );

        t.setRowFactory(tv -> {
            TableRow<Room> row = new TableRow<>();
            row.itemProperty().addListener((obs, eski, yeni) -> {
                if (yeni == null) return;
                // Tip'e göre hafif sol şerit rengi
                String renk = tipRenk(yeni.getTip());
                row.setStyle("-fx-border-color: " + renk
                        + " transparent transparent transparent;"
                        + "-fx-border-width: 0 0 0 3;");
            });
            return row;
        });

        return t;
    }

    private String tipRenk(RoomType tip) {
        return switch (tip) {
            case SUIT     -> "#8E44AD";
            case DELUXE   -> "#2980B9";
            case SUPERIOR -> "#2980B9";
            case AILE     -> "#27AE60";
            default       -> "#95A5A6";
        };
    }

    private TableColumn<Room, String> sutun(String baslik, double genislik,
                                            java.util.function.Function<Room, String> fn) {
        TableColumn<Room, String> kol = new TableColumn<>(baslik);
        kol.setPrefWidth(genislik);
        kol.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return kol;
    }

    private void odaEkleDiyalog() {
        if (sistem.tumSubeler().isEmpty()) {
            HotelApp.uyariGoster("Şube Yok", "Önce şube eklemelisiniz.");
            return;
        }

        Dialog<Room> diyalog = new Dialog<>();
        diyalog.setTitle("Yeni Oda Ekle");
        diyalog.setHeaderText(null);

        ButtonType ekleBtn = new ButtonType("Ekle", ButtonBar.ButtonData.OK_DONE);
        diyalog.getDialogPane().getButtonTypes().addAll(ekleBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> subeCb = new ComboBox<>();
        sistem.tumSubeler().forEach(s ->
                subeCb.getItems().add(s.getSubeId() + " - " + s.getAd()));
        subeCb.setPromptText("Şube seçin");
        subeCb.setPrefWidth(220);

        Spinner<Integer> odaNoSp = new Spinner<>(1, 999, 101);
        Spinner<Integer> katSp   = new Spinner<>(1, 50, 1);
        Spinner<Double>  fiyatSp = new Spinner<>(500.0, 50000.0, 2000.0, 500.0);
        fiyatSp.setEditable(true);

        ComboBox<RoomType> tipCb = new ComboBox<>();
        tipCb.getItems().addAll(RoomType.values());
        tipCb.setValue(RoomType.STANDART);
        tipCb.setPrefWidth(220);

        grid.addRow(0, new Label("Şube:"),    subeCb);
        grid.addRow(1, new Label("Oda No:"),  odaNoSp);
        grid.addRow(2, new Label("Kat:"),     katSp);
        grid.addRow(3, new Label("Tip:"),     tipCb);
        grid.addRow(4, new Label("Fiyat:"),   fiyatSp);

        diyalog.getDialogPane().setContent(grid);

        diyalog.setResultConverter(tip -> {
            if (tip != ekleBtn) return null;
            if (subeCb.getValue() == null) {
                HotelApp.uyariGoster("Eksik", "Şube seçiniz.");
                return null;
            }
            String subeId = subeCb.getValue().split(" - ")[0];
            try {
                return sistem.odaEkle(subeId, odaNoSp.getValue(),
                        tipCb.getValue(), fiyatSp.getValue(), katSp.getValue());
            } catch (Exception e) {
                HotelApp.uyariGoster("Hata", e.getMessage());
                return null;
            }
        });

        diyalog.showAndWait().ifPresent(oda -> {
            HotelApp.bilgiGoster("Başarılı", "Oda eklendi: " + oda.getOdaId());
            // Başlık sayacını güncelle
            ((Label) ((HBox) ((VBox) getCenter()).getChildren().get(0))
                    .getChildren().get(0)).setText("Odalar");
            sekmeleriYenile();
            // Eklenen odanın şubesinin sekmesine geç
            String subeId = oda.getSubeId();
            for (int i = 0; i < tabPane.getTabs().size(); i++) {
                Tab t = tabPane.getTabs().get(i);
                TableView<?> tv = (TableView<?>)
                        ((StackPane) t.getContent()).getChildren().get(0);
                if (!tv.getItems().isEmpty()) {
                    Object ilk = tv.getItems().get(0);
                    if (ilk instanceof Room r && r.getSubeId().equals(subeId)) {
                        tabPane.getSelectionModel().select(i);
                        break;
                    }
                }
            }
        });
    }
}
