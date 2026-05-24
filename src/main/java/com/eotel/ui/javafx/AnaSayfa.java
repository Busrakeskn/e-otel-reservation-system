package com.eotel.ui.javafx;

import com.eotel.manager.HotelSystem;
import com.eotel.model.Branch;
import com.eotel.model.Reservation;
import com.eotel.model.ReservationStatus;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class AnaSayfa extends ScrollPane {

    private final HotelApp app;
    private final HotelSystem sistem;

    public AnaSayfa(HotelApp app) {
        this.app = app;
        this.sistem = app.getSistem();
        setFitToWidth(true);
        setStyle("-fx-background-color: #F0F2F5; -fx-background: #F0F2F5;");
        setContent(icerikOlustur());
    }

    private VBox icerikOlustur() {
        VBox ana = new VBox(0);
        ana.setStyle("-fx-background-color: #F0F2F5;");
        ana.getChildren().addAll(
                heroBolum(),
                istatistikBolum(),
                ortaBolum()
        );
        return ana;
    }

    // ---- Hero ----

    private StackPane heroBolum() {
        StackPane hero = new StackPane();
        hero.setPrefHeight(210);
        hero.setStyle("-fx-background-color: linear-gradient(to bottom right, #1A252F, #2C3E50);");

        HBox ic = new HBox();
        ic.setPadding(new Insets(0, 40, 0, 40));
        ic.setAlignment(Pos.CENTER_LEFT);

        // Sol: başlık + buton
        VBox sol = new VBox(10);
        sol.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(sol, Priority.ALWAYS);

        Label tarih = new Label(guncelTarih());
        tarih.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.5);"
                + "-fx-letter-spacing: 1;");

        Label baslik = new Label("Hoş geldiniz, Yönetici 👋");
        baslik.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label alt = new Label("Bugünkü rezervasyon ve müsaitlik durumu aşağıda");
        alt.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.65);");

        HBox butonlar = new HBox(10);
        Button yeniRez = new Button("+ Yeni Rezervasyon");
        yeniRez.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;"
                + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand;"
                + "-fx-padding: 10 22 10 22; -fx-background-radius: 6;");
        yeniRez.setOnAction(e -> app.sayfaGoster(new RezervasyonSayfasi(app)));
        yeniRez.setOnMouseEntered(e -> yeniRez.setStyle(yeniRez.getStyle()
                .replace("#C0392B", "#E74C3C")));
        yeniRez.setOnMouseExited(e -> yeniRez.setStyle(yeniRez.getStyle()
                .replace("#E74C3C", "#C0392B")));

        Button takvim = new Button("📅  Müsaitlik");
        takvim.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 10 22 10 22;"
                + "-fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.25);"
                + "-fx-border-radius: 6;");
        takvim.setOnAction(e -> app.sayfaGoster(new MusaitlikSayfasi(app)));

        butonlar.getChildren().addAll(yeniRez, takvim);
        sol.getChildren().addAll(tarih, baslik, alt, butonlar);

        // Sağ: mini istatistik kutuları
        HBox sagKutular = new HBox(12);
        sagKutular.setAlignment(Pos.CENTER_RIGHT);

        long beklemede = sistem.tumRezervasyonlar().stream()
                .filter(r -> r.getDurum() == ReservationStatus.BEKLEMEDE
                          || r.getDurum() == ReservationStatus.BEKLEME_LISTESI).count();
        long bugunCikis = sistem.tumRezervasyonlar().stream()
                .filter(r -> r.getDurum() == ReservationStatus.ONAYLANDI
                          && r.getCikis().equals(LocalDate.now())).count();
        long bugunGiris = sistem.tumRezervasyonlar().stream()
                .filter(r -> r.getDurum() == ReservationStatus.ONAYLANDI
                          && r.getGiris().equals(LocalDate.now())).count();

        sagKutular.getChildren().addAll(
                miniKart("Bugün Giriş",  String.valueOf(bugunGiris),  "#2ECC71"),
                miniKart("Bugün Çıkış",  String.valueOf(bugunCikis),  "#3498DB"),
                miniKart("Bekleyen",     String.valueOf(beklemede),   "#F39C12")
        );

        ic.getChildren().addAll(sol, sagKutular);
        hero.getChildren().add(ic);
        return hero;
    }

    private VBox miniKart(String baslik, String deger, String renk) {
        VBox k = new VBox(4);
        k.setPadding(new Insets(14, 18, 14, 18));
        k.setAlignment(Pos.CENTER);
        k.setMinWidth(90);
        k.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10;"
                + "-fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 10;");

        Label d = new Label(deger);
        d.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + renk + ";");
        Label b = new Label(baslik);
        b.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.55);");
        k.getChildren().addAll(d, b);
        return k;
    }

    // ---- İstatistik kartları ----

    private HBox istatistikBolum() {
        HBox kutu = new HBox(16);
        kutu.setPadding(new Insets(24, 32, 8, 32));

        long toplamRez  = sistem.tumRezervasyonlar().size();
        long aktifRez   = sistem.tumRezervasyonlar().stream()
                .filter(r -> r.getDurum() == ReservationStatus.ONAYLANDI).count();
        long toplamOda  = sistem.tumOdalar().size();
        long toplamMus  = sistem.tumMusteriler().size();

        kutu.getChildren().addAll(
                HotelApp.istatKart("Toplam Rezervasyon", String.valueOf(toplamRez),  "#2980B9", null, false),
                HotelApp.istatKart("Aktif Rezervasyon",  String.valueOf(aktifRez),   "#27AE60", null, false),
                HotelApp.istatKart("Toplam Oda",         String.valueOf(toplamOda),  "#8E44AD", null, false),
                HotelApp.istatKart("Müşteri Sayısı",     String.valueOf(toplamMus),  "#E67E22", null, false)
        );

        for (var kart : kutu.getChildren()) {
            HBox.setHgrow(kart, Priority.ALWAYS);
            ((VBox) kart).setMaxWidth(Double.MAX_VALUE);
        }
        return kutu;
    }

    // ---- Orta içerik: son rezervasyonlar + hızlı erişim ----

    private HBox ortaBolum() {
        HBox bolum = new HBox(16);
        bolum.setPadding(new Insets(20, 32, 32, 32));

        VBox sonRezervasyonlar = sonRezervasyonlarBolum();
        HBox.setHgrow(sonRezervasyonlar, Priority.ALWAYS);

        VBox sagPanel = sagPanelOlustur();
        sagPanel.setMinWidth(260);
        sagPanel.setMaxWidth(260);

        bolum.getChildren().addAll(sonRezervasyonlar, sagPanel);
        return bolum;
    }

    @SuppressWarnings("unchecked")
    private VBox sonRezervasyonlarBolum() {
        VBox bolum = new VBox(12);
        bolum.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);");
        bolum.setPadding(new Insets(20));

        HBox baslikSatir = new HBox();
        baslikSatir.setAlignment(Pos.CENTER_LEFT);
        Label baslik = new Label("Son Rezervasyonlar");
        baslik.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1A252F;");
        Region bosluk = new Region();
        HBox.setHgrow(bosluk, Priority.ALWAYS);
        Button hepsiniGor = new Button("Tümünü gör →");
        hepsiniGor.setStyle("-fx-background-color: transparent; -fx-text-fill: #C0392B;"
                + "-fx-font-size: 12px; -fx-cursor: hand;");
        hepsiniGor.setOnAction(e -> app.sayfaGoster(new RezervasyonSayfasi(app)));
        baslikSatir.getChildren().addAll(baslik, bosluk, hepsiniGor);

        // Tablo
        TableView<Reservation> tablo = new TableView<>();
        tablo.setFixedCellSize(42);
        tablo.setStyle("-fx-background-color: transparent; -fx-table-cell-border-color: transparent;");
        tablo.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablo.setPlaceholder(new Label("Henüz rezervasyon yok"));

        TableColumn<Reservation, String> idKol = new TableColumn<>("ID");
        idKol.setPrefWidth(130);
        idKol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRezervasyonId()));

        TableColumn<Reservation, String> musteriKol = new TableColumn<>("Müşteri");
        musteriKol.setPrefWidth(120);
        musteriKol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMusteriId()));

        TableColumn<Reservation, String> tarihKol = new TableColumn<>("Tarihler");
        tarihKol.setPrefWidth(160);
        tarihKol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getGiris() + "  →  " + d.getValue().getCikis()));

        TableColumn<Reservation, String> durumKol = new TableColumn<>("Durum");
        durumKol.setPrefWidth(130);
        durumKol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Reservation r = (Reservation) getTableRow().getItem();
                setGraphic(HotelApp.durumRozeti(r.getDurum().getLabel()));
            }
        });

        TableColumn<Reservation, String> tutarKol = new TableColumn<>("Tutar");
        tutarKol.setPrefWidth(90);
        tutarKol.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%.0f₺", d.getValue().getToplamTutar())));
        tutarKol.setStyle("-fx-alignment: CENTER-RIGHT;");

        tablo.getColumns().addAll(idKol, musteriKol, tarihKol, durumKol, tutarKol);

        List<Reservation> son = sistem.tumRezervasyonlar().stream()
                .sorted(Comparator.comparing(Reservation::getOlusturmaTarihi).reversed())
                .limit(7)
                .toList();
        tablo.setItems(FXCollections.observableArrayList(son));
        tablo.setPrefHeight(son.size() * 42 + 40);
        tablo.setMaxHeight(350);

        bolum.getChildren().addAll(baslikSatir, new Separator(), tablo);
        return bolum;
    }

    private VBox sagPanelOlustur() {
        VBox panel = new VBox(14);

        // Hızlı erişim
        VBox hizliErisim = new VBox(8);
        hizliErisim.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + "-fx-padding: 18;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);");
        Label hBaslik = new Label("Hızlı Erişim");
        hBaslik.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A252F;");
        hizliErisim.getChildren().add(hBaslik);
        hizliErisim.getChildren().add(new Separator());

        String[][] hizlilar = {
            {"📋", "Rezervasyonlar"},
            {"🛏", "Odalar"},
            {"👥", "Müşteriler"},
            {"🏨", "Şubeler"},
            {"📊", "Raporlar"},
        };
        for (String[] h : hizlilar) {
            Button btn = hizliErisimButonu(h[0], h[1]);
            btn.setOnAction(e -> {
                switch (h[1]) {
                    case "Rezervasyonlar" -> app.sayfaGoster(new RezervasyonSayfasi(app));
                    case "Odalar"         -> app.sayfaGoster(new OdaSayfasi(app));
                    case "Müşteriler"     -> app.sayfaGoster(new MusteriSayfasi(app));
                    case "Şubeler"        -> app.sayfaGoster(new SubeSayfasi(app));
                    case "Raporlar"       -> app.sayfaGoster(new RaporSayfasi(app));
                }
            });
            hizliErisim.getChildren().add(btn);
        }

        // Şubeler özet
        VBox subelerKutu = subelerOzetKutu();

        panel.getChildren().addAll(hizliErisim, subelerKutu);
        return panel;
    }

    private Button hizliErisimButonu(String ikon, String metin) {
        HBox ic = new HBox(10);
        ic.setAlignment(Pos.CENTER_LEFT);

        Label ikonL = new Label(ikon);
        ikonL.setStyle("-fx-font-size: 14px; -fx-min-width: 22;");
        Label metinL = new Label(metin);
        metinL.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C3E50;");

        ic.getChildren().addAll(ikonL, metinL);

        Button btn = new Button();
        btn.setGraphic(ic);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(8, 10, 8, 4));
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-background-radius: 6;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #F8F9FA; -fx-cursor: hand; -fx-background-radius: 6;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-background-radius: 6;"));
        return btn;
    }

    private VBox subelerOzetKutu() {
        VBox kutu = new VBox(10);
        kutu.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + "-fx-padding: 18;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);");

        Label baslik = new Label("Şubeler");
        baslik.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A252F;");
        kutu.getChildren().addAll(baslik, new Separator());

        List<Branch> subeler = sistem.tumSubeler();
        if (subeler.isEmpty()) {
            Label bos = new Label("Henüz şube yok");
            bos.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");
            kutu.getChildren().add(bos);
            return kutu;
        }

        for (Branch s : subeler) {
            HBox satir = new HBox(10);
            satir.setAlignment(Pos.CENTER_LEFT);
            satir.setPadding(new Insets(4, 0, 4, 0));

            Circle nokta = new Circle(5, Color.web("#C0392B"));

            VBox bilgi = new VBox(2);
            HBox.setHgrow(bilgi, Priority.ALWAYS);
            Label adL = new Label(s.getAd());
            adL.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
            Label sehirL = new Label(s.getSehir());
            sehirL.setStyle("-fx-font-size: 11px; -fx-text-fill: #7F8C8D;");
            bilgi.getChildren().addAll(adL, sehirL);

            int odaSayisi = sistem.subeOdalari(s.getSubeId()).size();
            Label odaL = new Label(odaSayisi + " oda");
            odaL.setStyle("-fx-font-size: 11px; -fx-text-fill: #C0392B; -fx-font-weight: bold;");

            satir.getChildren().addAll(nokta, bilgi, odaL);
            kutu.getChildren().add(satir);
        }
        return kutu;
    }

    private String guncelTarih() {
        LocalDate bugun = LocalDate.now();
        String[] aylar = {"Ocak","Şubat","Mart","Nisan","Mayıs","Haziran",
                "Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık"};
        return bugun.getDayOfMonth() + " " + aylar[bugun.getMonthValue()-1] + " " + bugun.getYear();
    }
}
