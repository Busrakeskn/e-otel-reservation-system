package com.eotel.ui.javafx;

import com.eotel.manager.HotelSystem;
import com.eotel.persistence.JsonDataManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class HotelApp extends Application {

    static final String KIRMIZI    = "#C0392B";
    static final String KIRMIZI_AC = "#E74C3C";
    static final String KOYU_GRI   = "#1A252F";
    static final String MENU_GRI   = "#212F3C";
    static final String BEYAZ      = "#FFFFFF";

    private Stage sahne;
    private BorderPane ana;
    private final HotelSystem sistem = HotelSystem.getInstance();
    private final JsonDataManager json = new JsonDataManager();

    // aktif menü butonu takibi
    private Button aktifMenuBtn;
    private final List<Button> menuButonlari = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        json.tumunuYukle(sistem);
        this.sahne = stage;

        ana = new BorderPane();
        ana.setLeft(solMenuOlustur());
        sayfaGoster(new AnaSayfa(this), "Ana Sayfa");

        Scene scene = new Scene(ana, 1280, 780);
        try {
            String css = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ignored) {}

        try {
            Image ikon = new Image(getClass().getResourceAsStream("/31d76f4f-6f04-47f2-8fe8-e47e1c84453d.png"));
            stage.getIcons().add(ikon);
        } catch (Exception ignored) {}

        stage.setTitle("Holistay — Rezervasyon Yönetim Sistemi");
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(680);
        stage.show();
    }

    @Override
    public void stop() {
        json.tumunuKaydet(sistem);
    }

    public void sayfaGoster(Region sayfa) {
        ana.setCenter(sayfa);
    }

    void sayfaGoster(Region sayfa, String menuAdi) {
        ana.setCenter(sayfa);
        menuButonlari.forEach(b -> pasifMenuStil(b));
        menuButonlari.stream()
                .filter(b -> b.getUserData() != null && b.getUserData().equals(menuAdi))
                .findFirst()
                .ifPresent(b -> { aktifMenuStil(b); aktifMenuBtn = b; });
    }

    private VBox solMenuOlustur() {
        VBox menu = new VBox();
        menu.setPrefWidth(230);
        menu.setStyle("-fx-background-color: " + KOYU_GRI + ";");

        // Logo alanı
        HBox logoKutu = new HBox();
        logoKutu.setAlignment(Pos.CENTER);
        logoKutu.setPadding(new Insets(20, 12, 16, 12));
        logoKutu.setStyle("-fx-background-color: " + KOYU_GRI + ";");

        try {
            Image logoImg = new Image(getClass().getResourceAsStream("/31d76f4f-6f04-47f2-8fe8-e47e1c84453d.png"));
            ImageView logoView = new ImageView(logoImg);
            logoView.setFitWidth(130);
            logoView.setFitHeight(130);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);

            Circle klip = new Circle(65, 65, 65);
            logoView.setClip(klip);

            logoKutu.getChildren().add(logoView);
        } catch (Exception e) {
            Circle logoCircle = new Circle(18);
            logoCircle.setFill(Color.web(KIRMIZI));
            Label logoHarf = new Label("H");
            logoHarf.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
            StackPane logoDaire = new StackPane(logoCircle, logoHarf);
            Label logoText = new Label("holistay");
            logoText.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + BEYAZ + ";");
            logoKutu.setSpacing(10);
            logoKutu.setAlignment(Pos.CENTER_LEFT);
            logoKutu.getChildren().addAll(logoDaire, logoText);
        }

        menu.getChildren().add(logoKutu);

        // Menü grubu başlığı
        Label yonetimLabel = menuGrupBaslik("YÖNETİM");
        menu.getChildren().add(yonetimLabel);

        String[][] menuler = {
            {"Ana Sayfa",         "⊞"},
            {"Rezervasyonlar",    "≡"},
            {"Müsaitlik Takvimi", "▦"},
            {"Odalar",            "⊡"},
            {"Müşteriler",        "◉"},
        };
        for (String[] m : menuler) {
            Button btn = menuButonu(m[0], m[1]);
            btn.setOnAction(e -> menuTikla(btn, m[0]));
            menuButonlari.add(btn);
            menu.getChildren().add(btn);
        }

        Label analizLabel = menuGrupBaslik("ANALİZ");
        menu.getChildren().add(analizLabel);

        String[][] analizMenuler = {
            {"Şubeler",  "⬡"},
            {"Raporlar", "◈"},
        };
        for (String[] m : analizMenuler) {
            Button btn = menuButonu(m[0], m[1]);
            btn.setOnAction(e -> menuTikla(btn, m[0]));
            menuButonlari.add(btn);
            menu.getChildren().add(btn);
        }

        Region bosluk = new Region();
        VBox.setVgrow(bosluk, Priority.ALWAYS);
        menu.getChildren().add(bosluk);

        // Alt separator
        Separator altSep = new Separator();
        altSep.setStyle("-fx-background-color: rgba(255,255,255,0.08);");
        altSep.setPadding(new Insets(0, 12, 4, 12));
        menu.getChildren().add(altSep);

        // Kullanıcı profili alanı
        HBox profilKutu = new HBox(10);
        profilKutu.setAlignment(Pos.CENTER_LEFT);
        profilKutu.setPadding(new Insets(12, 16, 10, 16));
        profilKutu.setStyle("-fx-cursor: hand;");

        Circle avatar = new Circle(18);
        avatar.setFill(Color.web("#2980B9"));
        Label avatarHarf = new Label("Y");
        avatarHarf.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
        StackPane avatarDaire = new StackPane(avatar, avatarHarf);

        VBox profilMetin = new VBox(2);
        Label kullanici = new Label("Yönetici");
        kullanici.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + BEYAZ + ";");
        Label rol = new Label("Admin");
        rol.setStyle("-fx-font-size: 11px; -fx-text-fill: #7F8C8D;");
        profilMetin.getChildren().addAll(kullanici, rol);

        profilKutu.getChildren().addAll(avatarDaire, profilMetin);
        menu.getChildren().add(profilKutu);

        // Çıkış butonu
        Button cikis = cikisButonu();
        cikis.setOnAction(e -> { json.tumunuKaydet(sistem); sahne.close(); });
        menu.getChildren().add(cikis);

        // İlk menüyü aktif yap
        if (!menuButonlari.isEmpty()) {
            aktifMenuStil(menuButonlari.get(0));
            aktifMenuBtn = menuButonlari.get(0);
        }

        return menu;
    }

    private Button menuButonu(String ad, String ikon) {
        Button btn = new Button();
        btn.setUserData(ad);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(11, 16, 11, 20));
        btn.setGraphicTextGap(12);

        Label ikonLabel = new Label(ikon);
        ikonLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #7F8C8D; -fx-min-width: 20;");

        Label adLabel = new Label(ad);
        adLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #BDC3C7;");

        HBox icerik = new HBox(12, ikonLabel, adLabel);
        icerik.setAlignment(Pos.CENTER_LEFT);
        btn.setGraphic(icerik);

        pasifMenuStil(btn);

        btn.setOnMouseEntered(e -> {
            if (btn != aktifMenuBtn) {
                btn.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-cursor: hand;"
                        + "-fx-border-color: transparent; -fx-background-radius: 6;"
                        + "-fx-border-radius: 6;");
                ikonLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #ECF0F1; -fx-min-width: 20;");
                adLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ECF0F1;");
            }
        });
        btn.setOnMouseExited(e -> {
            if (btn != aktifMenuBtn) {
                pasifMenuStil(btn);
                ikonLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #7F8C8D; -fx-min-width: 20;");
                adLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #BDC3C7;");
            }
        });

        return btn;
    }

    private void pasifMenuStil(Button btn) {
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"
                + "-fx-border-color: transparent; -fx-background-radius: 6; -fx-border-radius: 6;");
        if (btn.getGraphic() instanceof HBox hb) {
            if (hb.getChildren().get(0) instanceof Label ikon)
                ikon.setStyle("-fx-font-size: 15px; -fx-text-fill: #7F8C8D; -fx-min-width: 20;");
            if (hb.getChildren().get(1) instanceof Label ad)
                ad.setStyle("-fx-font-size: 13px; -fx-text-fill: #BDC3C7;");
        }
    }

    private void aktifMenuStil(Button btn) {
        btn.setStyle("-fx-background-color: rgba(192,57,43,0.18); -fx-cursor: hand;"
                + "-fx-border-color: " + KIRMIZI + "; -fx-border-width: 0 0 0 3;"
                + "-fx-background-radius: 0 6 6 0; -fx-border-radius: 0 6 6 0;");
        if (btn.getGraphic() instanceof HBox hb) {
            if (hb.getChildren().get(0) instanceof Label ikon)
                ikon.setStyle("-fx-font-size: 15px; -fx-text-fill: " + KIRMIZI_AC + "; -fx-min-width: 20;");
            if (hb.getChildren().get(1) instanceof Label ad)
                ad.setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: bold;");
        }
    }

    private Button cikisButonu() {
        Label ikon = new Label("↩");
        ikon.setStyle("-fx-font-size: 15px; -fx-text-fill: #E74C3C; -fx-min-width: 20;");
        Label ad = new Label("Çıkış Yap");
        ad.setStyle("-fx-font-size: 13px; -fx-text-fill: #E74C3C;");
        HBox ic = new HBox(12, ikon, ad);
        ic.setAlignment(Pos.CENTER_LEFT);

        Button btn = new Button();
        btn.setGraphic(ic);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 16, 14, 20));
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(231,76,60,0.1); -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand;"));
        return btn;
    }

    private Label menuGrupBaslik(String metin) {
        Label l = new Label(metin);
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: #566573; -fx-font-weight: bold;"
                + "-fx-letter-spacing: 1; -fx-padding: 14 0 4 20;");
        return l;
    }

    private void menuTikla(Button btn, String sayfa) {
        if (aktifMenuBtn != null) pasifMenuStil(aktifMenuBtn);
        aktifMenuStil(btn);
        aktifMenuBtn = btn;

        switch (sayfa) {
            case "Ana Sayfa"         -> sayfaGoster(new AnaSayfa(this));
            case "Rezervasyonlar"    -> sayfaGoster(new RezervasyonSayfasi(this));
            case "Müsaitlik Takvimi" -> sayfaGoster(new MusaitlikSayfasi(this));
            case "Odalar"            -> sayfaGoster(new OdaSayfasi(this));
            case "Müşteriler"        -> sayfaGoster(new MusteriSayfasi(this));
            case "Şubeler"           -> sayfaGoster(new SubeSayfasi(this));
            case "Raporlar"          -> sayfaGoster(new RaporSayfasi(this));
        }
    }

    public HotelSystem getSistem() { return sistem; }
    public Stage getSahne() { return sahne; }

    // ========== Ortak UI bileşenleri ==========

    public static Button kirmiziBtnOlustur(String metin) {
        Button b = new Button(metin);
        b.setStyle("-fx-background-color: " + KIRMIZI + "; -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 9 20 9 20;"
                + "-fx-background-radius: 6; -fx-font-weight: bold;");
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace(KIRMIZI + ";", KIRMIZI_AC + ";")));
        b.setOnMouseExited(e  -> b.setStyle(b.getStyle().replace(KIRMIZI_AC + ";", KIRMIZI + ";")));
        return b;
    }

    public static Button grisBtn(String metin) {
        Button b = new Button(metin);
        b.setStyle("-fx-background-color: #ECF0F1; -fx-text-fill: #2C3E50;"
                + "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 9 20 9 20;"
                + "-fx-background-radius: 6;");
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace("#ECF0F1", "#D5D8DC")));
        b.setOnMouseExited(e  -> b.setStyle(b.getStyle().replace("#D5D8DC", "#ECF0F1")));
        return b;
    }

    public static HBox sayfaBasligi(String baslik, String altBaslik) {
        VBox metinler = new VBox(4);
        Label b = new Label(baslik);
        b.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A252F;");
        Label a = new Label(altBaslik);
        a.setStyle("-fx-font-size: 13px; -fx-text-fill: #7F8C8D;");
        metinler.getChildren().addAll(b, a);

        HBox kutu = new HBox(metinler);
        kutu.setPadding(new Insets(0, 0, 16, 0));
        HBox.setHgrow(metinler, Priority.ALWAYS);
        return kutu;
    }

    // Renkli sol şeritli istatistik kartı
    public static VBox istatKart(String baslik, String deger, String renk) {
        return istatKart(baslik, deger, renk, null, false);
    }

    public static VBox istatKart(String baslik, String deger, String renk,
                                  String trend, boolean artan) {
        VBox kart = new VBox(6);
        kart.setPadding(new Insets(18, 20, 18, 20));
        kart.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 3);"
                + "-fx-border-color: " + renk + "; -fx-border-width: 0 0 0 4;"
                + "-fx-border-radius: 0 10 10 0;");
        kart.setMinWidth(155);

        Label degerLabel = new Label(deger);
        degerLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #1A252F;");

        Label baslikLabel = new Label(baslik);
        baslikLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");

        kart.getChildren().addAll(degerLabel, baslikLabel);

        if (trend != null) {
            String trendRenk = artan ? "#27AE60" : "#E74C3C";
            String ok = artan ? "▲" : "▼";
            Label trendLabel = new Label(ok + " " + trend);
            trendLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + trendRenk + ";");
            kart.getChildren().add(trendLabel);
        }

        return kart;
    }

    // Renkli durum rozeti
    public static Label durumRozeti(String durum) {
        Label l = new Label(durum);
        l.setPadding(new Insets(3, 10, 3, 10));
        l.setStyle("-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        switch (durum) {
            case "Onaylandı"       -> l.setStyle(l.getStyle()
                    + "-fx-background-color: #EAFAF1; -fx-text-fill: #1E8449;");
            case "İptal Edildi"    -> l.setStyle(l.getStyle()
                    + "-fx-background-color: #FDEDEC; -fx-text-fill: #C0392B;");
            case "Tamamlandı"      -> l.setStyle(l.getStyle()
                    + "-fx-background-color: #EBF5FB; -fx-text-fill: #2471A3;");
            case "Bekleme Listesi" -> l.setStyle(l.getStyle()
                    + "-fx-background-color: #FEF9E7; -fx-text-fill: #B7950B;");
            default                -> l.setStyle(l.getStyle()
                    + "-fx-background-color: #F2F3F4; -fx-text-fill: #566573;");
        }
        return l;
    }

    public static void uyariGoster(String baslik, String mesaj) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(baslik); a.setHeaderText(null); a.setContentText(mesaj);
        a.showAndWait();
    }

    public static void bilgiGoster(String baslik, String mesaj) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(baslik); a.setHeaderText(null); a.setContentText(mesaj);
        a.showAndWait();
    }

    public static boolean onayDiyalog(String mesaj) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Onay"); a.setHeaderText(null); a.setContentText(mesaj);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
