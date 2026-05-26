package com.eotel.ui.javafx;

import com.eotel.manager.HotelSystem;
import com.eotel.model.Customer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.regex.Pattern;

public class MusteriSayfasi extends BorderPane {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFON_PATTERN =
            Pattern.compile("^(\\+90|0)?5[0-9]{9}$");

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

        TableColumn<Customer, String> islemKol = new TableColumn<>("İşlem");
        islemKol.setPrefWidth(80);
        islemKol.setMinWidth(80);
        islemKol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMusteriId()));
        islemKol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String musteriId, boolean empty) {
                super.updateItem(musteriId, empty);
                if (empty || musteriId == null) { setGraphic(null); return; }
                Button silBtn = HotelApp.grisBtn("Sil");
                silBtn.setPadding(new Insets(4, 10, 4, 10));
                silBtn.setStyle(silBtn.getStyle()
                        + "-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                silBtn.setOnAction(e -> {
                    Customer m = getTableView().getItems().get(getIndex());
                    if (HotelApp.onayDiyalog(m.getTamAd() + " silinsin mi?")) {
                        try {
                            sistem.musteriSil(m.getMusteriId());
                            tabloVeriYukle();
                        } catch (Exception ex) {
                            HotelApp.uyariGoster("Silinemedi", ex.getMessage());
                        }
                    }
                });
                setGraphic(silBtn);
            }
        });

        t.getColumns().addAll(
            sutun("ID",          110, m -> m.getMusteriId()),
            sutun("Ad Soyad",    170, m -> m.getTamAd()),
            sutun("E-posta",     200, m -> m.getEmail()),
            sutun("Telefon",     130, m -> m.getTelefon() != null ? m.getTelefon() : "-"),
            sutun("Rezervasyon",  80, m -> String.valueOf(m.getToplamRezervasyon())),
            islemKol
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

        TextField adField    = new TextField();
        TextField soyadField = new TextField();
        TextField emailField = new TextField();
        TextField telField   = new TextField();
        telField.setPromptText("05XX XXX XX XX");

        Label emailUyari = new Label();
        emailUyari.setStyle("-fx-text-fill: #C0392B; -fx-font-size: 11px;");
        Label telUyari = new Label();
        telUyari.setStyle("-fx-text-fill: #C0392B; -fx-font-size: 11px;");

        String gecerliStil  = "-fx-border-color: #27AE60; -fx-border-radius: 4;";
        String hataliStil   = "-fx-border-color: #C0392B; -fx-border-radius: 4;";
        String normalStil   = "";

        emailField.textProperty().addListener((obs, ov, nv) -> {
            if (nv.isBlank()) {
                emailField.setStyle(normalStil); emailUyari.setText("");
            } else if (EMAIL_PATTERN.matcher(nv.trim()).matches()) {
                emailField.setStyle(gecerliStil); emailUyari.setText("");
            } else {
                emailField.setStyle(hataliStil);
                emailUyari.setText("Geçersiz format (örn: ad@mail.com)");
            }
        });

        telField.textProperty().addListener((obs, ov, nv) -> {
            if (nv.isBlank()) {
                telField.setStyle(normalStil); telUyari.setText("");
            } else {
                String temiz = nv.replaceAll("[\\s\\-()]", "");
                if (TELEFON_PATTERN.matcher(temiz).matches()) {
                    telField.setStyle(gecerliStil); telUyari.setText("");
                } else {
                    telField.setStyle(hataliStil);
                    telUyari.setText("Geçersiz numara (örn: 05XX XXX XX XX)");
                }
            }
        });

        grid.addRow(0, new Label("Ad:"),      adField);
        grid.addRow(1, new Label("Soyad:"),   soyadField);
        grid.addRow(2, new Label("E-posta:"), emailField);
        grid.add(emailUyari, 1, 3);
        grid.addRow(4, new Label("Telefon:"), telField);
        grid.add(telUyari, 1, 5);

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
