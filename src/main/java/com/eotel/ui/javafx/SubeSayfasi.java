package com.eotel.ui.javafx;

import com.eotel.manager.HotelSystem;
import com.eotel.model.Branch;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class SubeSayfasi extends BorderPane {

    private final HotelSystem sistem;
    private TableView<Branch> tablo;

    public SubeSayfasi(HotelApp app) {
        this.sistem = app.getSistem();
        setStyle("-fx-background-color: #F0F2F5;");
        setPadding(new Insets(28, 32, 28, 32));
        olustur();
    }

    private void olustur() {
        HBox baslikSatiri = HotelApp.sayfaBasligi("Şubeler",
                sistem.tumSubeler().size() + " şube");

        Button yeniBtn = HotelApp.kirmiziBtnOlustur("+ Şube Ekle");
        yeniBtn.setOnAction(e -> subeEkleDiyalog());
        HBox.setHgrow(baslikSatiri.getChildren().get(0), Priority.ALWAYS);
        baslikSatiri.setAlignment(Pos.CENTER_LEFT);
        baslikSatiri.getChildren().add(yeniBtn);

        tablo = tabloOlustur();
        tabloVeriYukle();

        VBox icerik = new VBox(12, baslikSatiri, tablo);
        VBox.setVgrow(tablo, Priority.ALWAYS);
        setCenter(icerik);
    }

    @SuppressWarnings("unchecked")
    private TableView<Branch> tabloOlustur() {
        TableView<Branch> t = new TableView<>();
        t.setStyle("-fx-background-color: white;");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        t.getColumns().addAll(
            sutun("Şube ID",  120, Branch::getSubeId),
            sutun("Ad",       200, Branch::getAd),
            sutun("Şehir",    120, Branch::getSehir),
            sutun("Adres",    250, s -> s.getAdres() != null ? s.getAdres() : "-"),
            sutun("Oda",       80, s ->
                    String.valueOf(sistem.subeOdalari(s.getSubeId()).size()))
        );
        return t;
    }

    private void tabloVeriYukle() {
        tablo.setItems(FXCollections.observableArrayList(sistem.tumSubeler()));
    }

    private TableColumn<Branch, String> sutun(String baslik, double genislik,
                                              java.util.function.Function<Branch, String> fn) {
        TableColumn<Branch, String> kol = new TableColumn<>(baslik);
        kol.setPrefWidth(genislik);
        kol.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return kol;
    }

    private void subeEkleDiyalog() {
        Dialog<Branch> diyalog = new Dialog<>();
        diyalog.setTitle("Yeni Şube");
        diyalog.setHeaderText(null);

        ButtonType ekleBtn = new ButtonType("Ekle", ButtonBar.ButtonData.OK_DONE);
        diyalog.getDialogPane().getButtonTypes().addAll(ekleBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField adField    = new TextField();
        TextField sehirField = new TextField();
        TextField adresField = new TextField();
        adresField.setPrefWidth(260);

        grid.addRow(0, new Label("Şube Adı:"), adField);
        grid.addRow(1, new Label("Şehir:"),    sehirField);
        grid.addRow(2, new Label("Adres:"),    adresField);

        diyalog.getDialogPane().setContent(grid);

        diyalog.setResultConverter(tip -> {
            if (tip != ekleBtn) return null;
            if (adField.getText().isBlank()) {
                HotelApp.uyariGoster("Eksik", "Şube adı zorunludur");
                return null;
            }
            return sistem.subeEkle(adField.getText().trim(),
                    sehirField.getText().trim(),
                    adresField.getText().trim());
        });

        diyalog.showAndWait().ifPresent(s -> {
            HotelApp.bilgiGoster("Başarılı", "Şube oluşturuldu: " + s.getSubeId());
            tabloVeriYukle();
        });
    }
}
