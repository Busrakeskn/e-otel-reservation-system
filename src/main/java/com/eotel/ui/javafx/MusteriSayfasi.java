package com.eotel.ui.javafx;

import com.eotel.manager.HotelSystem;
import com.eotel.model.Customer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class MusteriSayfasi extends BorderPane {

    private final HotelSystem sistem;
    private TableView<Customer> tablo;

    public MusteriSayfasi(HotelApp app) {
        this.sistem = app.getSistem();
        setStyle("-fx-background-color: #F0F2F5;");
        setPadding(new Insets(28, 32, 28, 32));
        olustur();
    }

    private void olustur() {
        HBox baslikSatiri = HotelApp.sayfaBasligi("Müşteriler",
                sistem.tumMusteriler().size() + " kayıt");

        Button yeniBtn = HotelApp.kirmiziBtnOlustur("+ Müşteri Ekle");
        yeniBtn.setOnAction(e -> musteriEkleDiyalog());
        HBox.setHgrow(baslikSatiri.getChildren().get(0), Priority.ALWAYS);
        baslikSatiri.setAlignment(Pos.CENTER_LEFT);
        baslikSatiri.getChildren().add(yeniBtn);

        TextField araField = new TextField();
        araField.setPromptText("Ad, soyad veya e-posta ara...");
        araField.setPrefWidth(300);
        Button araBtn = HotelApp.grisBtn("Ara");
        araBtn.setOnAction(e -> {
            String q = araField.getText().trim().toLowerCase();
            if (q.isEmpty()) { tabloVeriYukle(); return; }
            tablo.setItems(FXCollections.observableArrayList(
                sistem.tumMusteriler().stream()
                    .filter(m -> m.getTamAd().toLowerCase().contains(q)
                              || m.getEmail().toLowerCase().contains(q))
                    .toList()
            ));
        });

        HBox filtre = new HBox(10, araField, araBtn);
        filtre.setAlignment(Pos.CENTER_LEFT);

        tablo = tabloOlustur();
        tabloVeriYukle();

        VBox icerik = new VBox(12, baslikSatiri, filtre, tablo);
        VBox.setVgrow(tablo, Priority.ALWAYS);
        setCenter(icerik);
    }

    @SuppressWarnings("unchecked")
    private TableView<Customer> tabloOlustur() {
        TableView<Customer> t = new TableView<>();
        t.setStyle("-fx-background-color: white;");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        t.getColumns().addAll(
            sutun("ID",          120, m -> m.getMusteriId()),
            sutun("Ad Soyad",    180, m -> m.getTamAd()),
            sutun("E-posta",     220, m -> m.getEmail()),
            sutun("Telefon",     130, m -> m.getTelefon() != null ? m.getTelefon() : "-"),
            sutun("Rezervasyon",  90, m -> String.valueOf(m.getToplamRezervasyon()))
        );
        return t;
    }

    private void tabloVeriYukle() {
        tablo.setItems(FXCollections.observableArrayList(sistem.tumMusteriler()));
    }

    private TableColumn<Customer, String> sutun(String baslik, double genislik,
                                                java.util.function.Function<Customer, String> fn) {
        TableColumn<Customer, String> kol = new TableColumn<>(baslik);
        kol.setPrefWidth(genislik);
        kol.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return kol;
    }

    private void musteriEkleDiyalog() {
        Dialog<Customer> diyalog = new Dialog<>();
        diyalog.setTitle("Yeni Müşteri");
        diyalog.setHeaderText(null);

        ButtonType ekleBtn = new ButtonType("Kaydet", ButtonBar.ButtonData.OK_DONE);
        diyalog.getDialogPane().getButtonTypes().addAll(ekleBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField adField     = new TextField();
        TextField soyadField  = new TextField();
        TextField emailField  = new TextField();
        TextField telField    = new TextField();

        grid.addRow(0, new Label("Ad:"),      adField);
        grid.addRow(1, new Label("Soyad:"),   soyadField);
        grid.addRow(2, new Label("E-posta:"), emailField);
        grid.addRow(3, new Label("Telefon:"), telField);

        diyalog.getDialogPane().setContent(grid);

        diyalog.setResultConverter(tip -> {
            if (tip != ekleBtn) return null;
            if (adField.getText().isBlank() || emailField.getText().isBlank()) {
                HotelApp.uyariGoster("Eksik Bilgi", "Ad ve e-posta zorunludur");
                return null;
            }
            try {
                return sistem.musteriEkle(adField.getText().trim(),
                        soyadField.getText().trim(),
                        emailField.getText().trim(),
                        telField.getText().trim());
            } catch (Exception e) {
                HotelApp.uyariGoster("Hata", e.getMessage());
                return null;
            }
        });

        diyalog.showAndWait().ifPresent(m -> {
            HotelApp.bilgiGoster("Başarılı", "Müşteri oluşturuldu: " + m.getMusteriId());
            tabloVeriYukle();
        });
    }
}
