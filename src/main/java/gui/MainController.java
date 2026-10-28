package gui;

import datastructures.Kuyruk;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import logic.SimulationController;
import model.Arac;
import model.Feribot;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // --- Header ---
    @FXML private Label       simSaatLabel;
    @FXML private Label       surePctLabel;
    @FXML private ProgressBar simProgress;
    @FXML private Label       rihtimDurumLabel;
    @FXML private Button      btnBaslat;
    @FXML private Button      btnDurdur;
    @FXML private Button      btnAdim;

    // --- Feribot bilgi satiri ---
    @FXML private Label seferNoLabel;
    @FXML private Label feribotNoLabel;
    @FXML private Label feribotDurumLabel;

    // --- Ferry Scene (Canvas container) ---
    @FXML private StackPane ferryScenePane;

    // --- Kat gorseli ---
    @FXML private HBox        ustKatBox;
    @FXML private HBox        girisKatBox;
    @FXML private ProgressBar ustKatProgress;
    @FXML private ProgressBar girisKatProgress;
    @FXML private Label       ustKatLabel;
    @FXML private Label       girisKatLabel;

    // --- Kuyruklar ---
    @FXML private ListView<String> yol1List;
    @FXML private ListView<String> yol2List;
    @FXML private Label            yol1BaslikLabel;
    @FXML private Label            yol2BaslikLabel;

    // --- Log ---
    @FXML private ListView<String> logListesi;

    // --- Tablolar ---
    @FXML private TableView<AracSatiri>           aracTable;
    @FXML private TableColumn<AracSatiri, String> colAracNo;
    @FXML private TableColumn<AracSatiri, String> colPlaka;
    @FXML private TableColumn<AracSatiri, String> colTip;
    @FXML private TableColumn<AracSatiri, String> colGiseSaati;
    @FXML private TableColumn<AracSatiri, String> colSeferNo;
    @FXML private TableColumn<AracSatiri, String> colDurum;

    @FXML private TableView<FeribotSatiri>           feribotTable;
    @FXML private TableColumn<FeribotSatiri, String> colSefer;
    @FXML private TableColumn<FeribotSatiri, String> colFeribotNo;
    @FXML private TableColumn<FeribotSatiri, String> colGiris;
    @FXML private TableColumn<FeribotSatiri, String> colPlanKalkis;
    @FXML private TableColumn<FeribotSatiri, String> colGercekKalkis;
    @FXML private TableColumn<FeribotSatiri, String> colGKat;
    @FXML private TableColumn<FeribotSatiri, String> colUKat;
    @FXML private TableColumn<FeribotSatiri, String> colGecikme;
    @FXML private TableColumn<FeribotSatiri, String> colFeribotDurum;

    // --- Inline form ---
    @FXML private TextField        plakaField;
    @FXML private TextField        saatField;
    @FXML private ComboBox<String> tipBox;

    // --- Simulasyon ---
    private final SimulationController          ctrl            = new SimulationController();
    private final ObservableList<AracSatiri>    aracVerileri    = FXCollections.observableArrayList();
    private final ObservableList<FeribotSatiri> feribotVerileri = FXCollections.observableArrayList();
    private Timeline simTimeline;

    // --- Slot labels ---
    private final Label[] ustSlotlar   = new Label[5];
    private final Label[] girisSlotlar = new Label[5];

    // --- Canvas & Animasyon ---
    private Canvas       ferryCanvas;
    private AnimationTimer waterAnimator;
    private double       waveTime = 0;

    // Yildiz alani (yuzde cinsinden, sabit seed ile)
    private static final int STAR_COUNT = 90;
    private final double[] starXPct = new double[STAR_COUNT];
    private final double[] starYPct = new double[STAR_COUNT];
    private final double[] starBr   = new double[STAR_COUNT];

    // ══════════════════════════════════════════════
    // INITIALIZE
    // ══════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generateStars();
        kolonlariAyarla();
        renkliHucrelerAyarla();
        katSlotsOlustur();
        logCellAyarla();
        formBaslat();
        controllerBagla();
        ferryCanvasOlustur();
        dosyalariYukle();
    }

    private void generateStars() {
        Random r = new Random(42);
        for (int i = 0; i < STAR_COUNT; i++) {
            starXPct[i] = r.nextDouble();
            starYPct[i] = r.nextDouble() * 0.58;
            starBr[i]   = 0.25 + r.nextDouble() * 0.75;
        }
    }

    // ══════════════════════════════════════════════
    // CANVAS KURULUM
    // ══════════════════════════════════════════════

    private void ferryCanvasOlustur() {
        ferryCanvas = new Canvas(800, 500);
        ferryCanvas.setManaged(false); // layout döngüsünü engelle
        ferryScenePane.widthProperty().addListener((obs, o, n) -> ferryCanvas.setWidth(n.doubleValue()));
        ferryScenePane.heightProperty().addListener((obs, o, n) -> ferryCanvas.setHeight(n.doubleValue()));
        ferryScenePane.getChildren().add(0, ferryCanvas);

        waterAnimator = new AnimationTimer() {
            @Override public void handle(long now) {
                waveTime += 0.018;
                ferryiCiz();
            }
        };
        waterAnimator.start();
    }

    // ══════════════════════════════════════════════
    // CANVAS ÇİZİM MOTORU
    // ══════════════════════════════════════════════

    private void ferryiCiz() {
        if (ferryCanvas == null) return;
        GraphicsContext gc = ferryCanvas.getGraphicsContext2D();
        double w = ferryCanvas.getWidth();
        double h = ferryCanvas.getHeight();
        if (w < 10 || h < 10) return;

        drawSky(gc, w, h);
        drawDock(gc, w, h);
        boolean feribotVar = ctrl.aktifFeribotIndex >= 0 && ctrl.aktifFeribotIndex < ctrl.feribotSayisi;
        if (feribotVar) drawFerry(gc, w, h);
        drawWater(gc, w, h);
        drawAtmosphere(gc, w, h);
        drawSeagulls(gc, w, h);
    }

    private void drawSky(GraphicsContext gc, double w, double h) {
        // Gün batımı gökyüzü
        LinearGradient sky = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0,  Color.web("#1B0A3A")),
            new Stop(0.28, Color.web("#4A1C6E")),
            new Stop(0.58, Color.web("#A8371E")),
            new Stop(0.82, Color.web("#D96020")),
            new Stop(1.0,  Color.web("#F0952A")));
        gc.setFill(sky);
        gc.fillRect(0, 0, w, h);

        // Gün batımı güneşi (ufukta)
        double sunX = w * 0.78;
        double sunY = h * 0.60;
        RadialGradient sunGlow = new RadialGradient(0, 0, sunX, sunY, 110, false, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.color(1.0, 0.65, 0.10, 0.42)),
            new Stop(0.45, Color.color(1.0, 0.45, 0.05, 0.14)),
            new Stop(1.0, Color.TRANSPARENT));
        gc.setFill(sunGlow);
        gc.fillOval(sunX - 110, sunY - 110, 220, 220);
        RadialGradient sunBody = new RadialGradient(0.3, 0.3, sunX - 6, sunY - 6, 28, false, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#FFF8E1")),
            new Stop(0.6, Color.web("#FFB74D")),
            new Stop(1.0, Color.web("#F57C00")));
        gc.setFill(sunBody);
        gc.fillOval(sunX - 28, sunY - 28, 56, 56);

        // Bulutlar
        drawClouds(gc, w, h);
    }

    private void drawClouds(GraphicsContext gc, double w, double h) {
        double[] cx  = {w * 0.15, w * 0.50, w * 0.35};
        double[] cy  = {h * 0.13, h * 0.20, h * 0.09};
        double[] spd = {1.8, 1.2, 1.5};
        double[] scl = {1.0, 0.7, 0.55};
        for (int c = 0; c < 3; c++) {
            double x = ((cx[c] + waveTime * spd[c]) % (w * 1.3)) - w * 0.15;
            double y = cy[c];
            double s = scl[c];
            gc.setFill(Color.color(0.95, 0.68, 0.75, 0.72));
            gc.fillOval(x,          y,          62 * s, 28 * s);
            gc.fillOval(x + 18 * s, y - 14 * s, 50 * s, 32 * s);
            gc.fillOval(x + 40 * s, y,           48 * s, 24 * s);
            gc.fillOval(x + 8  * s, y + 6  * s,  44 * s, 20 * s);
        }
    }

    private void drawDock(GraphicsContext gc, double w, double h) {
        double waterY = h * 0.71;

        // Beton rıhtım platformu (gündüz rengi)
        LinearGradient dockGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#8D9E9A")),
            new Stop(1, Color.web("#607D8B")));
        gc.setFill(dockGrad);
        gc.fillRect(0, waterY - 24, w * 0.115, 24);
        gc.fillRect(w * 0.885, waterY - 24, w * 0.115, 24);
        // Rıhtım kenar çizgisi
        gc.setFill(Color.web("#B0BEC5"));
        gc.fillRect(0, waterY - 26, w * 0.115, 2);
        gc.fillRect(w * 0.885, waterY - 26, w * 0.115, 2);

        // Babalar / bitte (gündüz - koyu demir)
        for (int i = 0; i < 3; i++) {
            double bx = 10 + i * 26;
            double by = waterY - 24;
            gc.setFill(Color.web("#455A64"));
            gc.fillRoundRect(bx, by - 20, 12, 21, 3, 3);
            gc.setFill(Color.web("#546E7A"));
            gc.fillOval(bx - 1, by - 24, 14, 9);
            gc.setFill(Color.web("#37474F"));
            gc.fillRoundRect(bx + 2, by - 14, 8, 13, 2, 2);
        }

        // Liman direği (gündüz - sarı uyarı lambası)
        double lampX = 9;
        double lampY = waterY - 55;
        gc.setFill(Color.web("#546E7A"));
        gc.fillRect(lampX + 3, lampY + 10, 5, 22);
        gc.setFill(Color.web("#FFD54F"));
        gc.fillOval(lampX, lampY, 12, 12);

        // Halat çizgileri
        gc.setStroke(Color.web("#78909C"));
        gc.setLineWidth(1.2);
        gc.setLineDashes(4, 3);
        gc.strokeLine(26, waterY - 12, w * 0.115 + 8, waterY - 5);
        gc.strokeLine(52, waterY - 12, w * 0.115 + 8, waterY - 9);
        gc.setLineDashes(0);

        // Sağ taraf: Sarı liman vinci
        double craneX = w * 0.888;
        double craneBaseY = waterY - 22;
        gc.setFill(Color.web("#F57F17")); // sarı vinç
        gc.fillRect(craneX, craneBaseY - h * 0.30, 8, h * 0.30);
        gc.fillRect(craneX - w * 0.048, craneBaseY - h * 0.30, w * 0.08, 6);
        // Vinç kafesi detay
        gc.setStroke(Color.web("#E65100"));
        gc.setLineWidth(1.5);
        for (int i = 0; i < 4; i++) {
            double ty = craneBaseY - h * 0.30 + i * (h * 0.08);
            gc.strokeLine(craneX, ty, craneX + 8, ty + 6);
        }
        // Vinç halatı
        gc.setStroke(Color.web("#5D4037"));
        gc.setLineWidth(1.5);
        gc.strokeLine(craneX - w * 0.022, craneBaseY - h * 0.30,
                      craneX - w * 0.022, craneBaseY - h * 0.12);
    }

    private void drawFerry(GraphicsContext gc, double w, double h) {
        double waterY = h * 0.71;
        double hullW  = w * 0.76;
        double hullX  = (w - hullW) / 2;
        double hullH  = h * 0.39;
        double hullY  = waterY - hullH;

        // Beyaz feribot gövdesi (gündüz)
        LinearGradient hullGrad = new LinearGradient(0, hullY / h, 0, waterY / h, true, CycleMethod.NO_CYCLE,
            new Stop(0.0,  Color.web("#ECEFF1")),
            new Stop(0.55, Color.web("#CFD8DC")),
            new Stop(1.0,  Color.web("#90A4AE")));
        double[] hx = {hullX + 5, hullX + hullW - 5, hullX + hullW + 10, hullX - 10};
        double[] hy = {hullY,     hullY,              waterY,             waterY};
        gc.setFill(hullGrad);
        gc.fillPolygon(hx, hy, 4);

        // Altin şerit bant
        gc.setFill(Color.web("#F9A825"));
        gc.fillRect(hullX + 5, hullY, hullW - 10, 9);

        // Kırmızı/turuncu alt boya şeridi (su hattı)
        gc.setFill(Color.web("#BF360C"));
        gc.fillRect(hullX - 10, waterY - 8, hullW + 20, 8);

        // Güverte çizgisi
        gc.setStroke(Color.web("#F9A825"));
        gc.setLineWidth(2.0);
        gc.strokeLine(hullX + 5, hullY, hullX + hullW - 5, hullY);

        // Prova dalga köpüğü
        gc.setStroke(Color.color(1.0, 1.0, 1.0, 0.55));
        gc.setLineWidth(1.0);
        for (int i = 1; i <= 4; i++) {
            gc.strokeLine(hullX - 10 + i * 3, waterY - 2, hullX - 20, waterY + i * 5);
            gc.strokeLine(hullX + hullW + 10 - i * 3, waterY - 2, hullX + hullW + 20, waterY + i * 5);
        }

        // Üst yapı / köprü (beyaz)
        double superW = hullW * 0.34;
        double superH = h * 0.16;
        double superX = hullX + (hullW - superW) / 2;
        double superY = hullY - superH;
        LinearGradient superGrad = new LinearGradient(0, superY / h, 0, hullY / h, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#F5F7FA")),
            new Stop(1.0, Color.web("#DDEAF0")));
        gc.setFill(superGrad);
        gc.fillRoundRect(superX, superY, superW, superH, 7, 7);
        gc.setStroke(Color.web("#90A4AE"));
        gc.setLineWidth(1.0);
        gc.strokeRoundRect(superX, superY, superW, superH, 7, 7);
        // Altin üst yapı şeridi
        gc.setFill(Color.web("#F9A825"));
        gc.fillRect(superX, superY, superW, 6);

        // Köprü pencereleri (gün batımı yansıması - amber)
        int winCount = 7;
        double winW = 7, winH = 10;
        double winSpacing = superW / (winCount + 1);
        for (int i = 1; i <= winCount; i++) {
            double wx = superX + i * winSpacing - winW / 2;
            double wy = superY + superH * 0.28;
            LinearGradient winGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFE082")),
                new Stop(1, Color.web("#FFB300")));
            gc.setFill(winGrad);
            gc.fillRoundRect(wx, wy, winW, winH, 2, 2);
            gc.setStroke(Color.web("#FF8F00"));
            gc.setLineWidth(0.5);
            gc.strokeRoundRect(wx, wy, winW, winH, 2, 2);
        }
        // Alt pencereler
        for (int i = 0; i < 5; i++) {
            double wx = superX + superW * 0.1 + i * (superW * 0.8 / 5);
            double wy = superY + superH * 0.65;
            gc.setFill(Color.web("#FFD54F"));
            gc.fillRoundRect(wx, wy, 5, 7, 1, 1);
        }

        // Bacalar (kırmızı şeritli beyaz baca — klasik feribot)
        double s1x = superX + superW * 0.26;
        double s2x = superX + superW * 0.62;
        gc.setFill(Color.web("#ECEFF1"));
        gc.fillRoundRect(s1x, superY - h * 0.08, 14, h * 0.085, 3, 3);
        gc.fillRoundRect(s2x, superY - h * 0.06, 11, h * 0.065, 3, 3);
        gc.setFill(Color.web("#C62828")); // kırmızı bant
        gc.fillRect(s1x, superY - h * 0.042, 14, 6);
        gc.fillRect(s2x, superY - h * 0.030, 11, 5);
        gc.setStroke(Color.web("#B0BEC5"));
        gc.setLineWidth(0.8);
        gc.strokeRoundRect(s1x, superY - h * 0.08, 14, h * 0.085, 3, 3);
        gc.strokeRoundRect(s2x, superY - h * 0.06, 11, h * 0.065, 3, 3);

        // Duman (gündüz — açık gri, ince)
        for (int stack = 0; stack < 2; stack++) {
            double sx = (stack == 0) ? s1x + 7 : s2x + 5.5;
            double sy = (stack == 0) ? superY - h * 0.08 - 3 : superY - h * 0.06 - 3;
            for (int i = 0; i < 5; i++) {
                double t = i / 4.0;
                double driftX = Math.sin(waveTime * 0.6 + i * 1.0 + stack * 1.8) * 16 * t;
                double ey = sy - t * h * 0.08;
                double er = 3 + t * 13;
                double alpha = 0.18 * (1 - t);
                gc.setFill(Color.color(0.75, 0.78, 0.80, alpha));
                gc.fillOval(sx + driftX - er / 2, ey - er / 2, er, er);
            }
        }

        // Seyir lambaları
        gc.setFill(Color.web("#EF5350"));
        gc.fillOval(hullX - 7, hullY - 9, 9, 9);
        gc.setFill(Color.web("#66BB6A"));
        gc.fillOval(hullX + hullW - 2, hullY - 9, 9, 9);

        // Anten / direk (koyu metal)
        gc.setStroke(Color.web("#607D8B"));
        gc.setLineWidth(1.5);
        gc.strokeLine(superX + superW * 0.5, superY - h * 0.13, superX + superW * 0.5, superY);
        gc.strokeLine(superX + superW * 0.34, superY - h * 0.13, superX + superW * 0.66, superY - h * 0.13);
    }

    private void drawWater(GraphicsContext gc, double w, double h) {
        double waterY = h * 0.71;

        // Turkuaz-mavi Ege suyu
        gc.beginPath();
        gc.moveTo(0, waterY + 10);
        for (double x = 0; x <= w; x += 2) {
            double y = waterY
                + Math.sin(x * 0.015 + waveTime) * 4.5
                + Math.sin(x * 0.030 + waveTime * 1.7) * 2.5
                + Math.sin(x * 0.007 + waveTime * 0.65) * 3.5;
            gc.lineTo(x, y);
        }
        gc.lineTo(w, h);
        gc.lineTo(0, h);
        gc.closePath();
        LinearGradient waterFill = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#1A4555")),
            new Stop(0.3, Color.web("#0C2030")),
            new Stop(1.0, Color.web("#050E18")));
        gc.setFill(waterFill);
        gc.fill();

        // Dalga yüzey çizgisi (köpük - beyaz)
        gc.beginPath();
        gc.setStroke(Color.color(1.0, 1.0, 1.0, 0.45));
        gc.setLineWidth(2.0);
        for (double x = 0; x <= w; x += 2) {
            double y = waterY
                + Math.sin(x * 0.015 + waveTime) * 4.5
                + Math.sin(x * 0.030 + waveTime * 1.7) * 2.5
                + Math.sin(x * 0.007 + waveTime * 0.65) * 3.5;
            if (x == 0) gc.moveTo(x, y); else gc.lineTo(x, y);
        }
        gc.stroke();

        // İkinci dalga
        gc.beginPath();
        gc.setStroke(Color.color(1.0, 1.0, 1.0, 0.18));
        gc.setLineWidth(1.2);
        for (double x = 0; x <= w; x += 2) {
            double y = waterY + 18
                + Math.sin(x * 0.011 + waveTime * 0.85 + 1.2) * 3.5
                + Math.sin(x * 0.024 + waveTime * 1.3 + 0.5) * 2;
            if (x == 0) gc.moveTo(x, y); else gc.lineTo(x, y);
        }
        gc.stroke();

        // Gün batımı parıltıları (turuncu-altin)
        for (int i = 0; i < 18; i++) {
            double fx = (i * 78.3 + waveTime * 35 * (i % 2 == 0 ? 1 : -0.7)) % w;
            double fy = waterY + 4 + (i % 4) * 9;
            double br = Math.max(0, Math.min(1, 0.7 * (0.35 + 0.65 * Math.sin(waveTime * 3.5 + i * 0.85))));
            gc.setFill(Color.color(1.0, 0.70, 0.22, br));
            gc.fillOval(fx - 2.5, fy - 1, 5, 2.5);
        }

        // Gün batımı güneş yansıma şeridi (turuncu)
        double sunReflX = w * 0.78;
        for (int row = 0; row < 9; row++) {
            double ry = waterY + 5 + row * 11 + Math.sin(waveTime * 1.6 + row * 0.7) * 3;
            double rw = Math.max(1, 26 - row * 2.5);
            double ra = Math.max(0, 0.28 - row * 0.028);
            double drift = Math.sin(waveTime * 1.4 + row * 0.5) * 5;
            gc.setFill(Color.color(1.0, 0.62, 0.12, ra));
            gc.fillRoundRect(sunReflX - rw / 2 + drift, ry, rw, 6, 3, 3);
        }
    }

    private void drawAtmosphere(GraphicsContext gc, double w, double h) {
        // Gün batımı ufku — turuncu-amber sis
        double waterY = h * 0.71;
        LinearGradient fog = new LinearGradient(0, (waterY - 22) / h, 0, (waterY + 16) / h, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.TRANSPARENT),
            new Stop(0.45, Color.color(1.0, 0.70, 0.30, 0.32)),
            new Stop(1.0, Color.TRANSPARENT));
        gc.setFill(fog);
        gc.fillRect(0, waterY - 22, w, 38);
    }

    private void drawSeagulls(GraphicsContext gc, double w, double h) {
        // Gün batımı martıları — pembe-beyaz
        gc.setStroke(Color.color(1.0, 0.88, 0.92, 0.88));
        gc.setLineWidth(2.0);
        double[] phases = {0.0, 0.35, 0.68, 0.15};
        double[] speeds = {7.0, 9.5, 6.2, 8.0};
        double[] heights = {0.09, 0.16, 0.11, 0.06};
        for (int i = 0; i < 4; i++) {
            double x = ((waveTime * speeds[i] + w * (phases[i] + 0.1)) % (w * 1.4)) - w * 0.15;
            double y = h * heights[i] + Math.sin(waveTime * 0.9 + phases[i] * 6.28) * 7;
            double wingFlap = Math.sin(waveTime * 5.5 + i * 1.2) * 6;
            gc.beginPath();
            gc.moveTo(x - 11, y + wingFlap);
            gc.quadraticCurveTo(x - 5, y - 6, x, y);
            gc.quadraticCurveTo(x + 5, y - 6, x + 11, y + wingFlap);
            gc.stroke();
        }
    }

    // ══════════════════════════════════════════════
    // TABLO & UI KURULUM
    // ══════════════════════════════════════════════

    private void kolonlariAyarla() {
        colAracNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colPlaka.setCellValueFactory(new PropertyValueFactory<>("plaka"));
        colTip.setCellValueFactory(new PropertyValueFactory<>("tip"));
        colGiseSaati.setCellValueFactory(new PropertyValueFactory<>("giseSaati"));
        colSeferNo.setCellValueFactory(new PropertyValueFactory<>("seferNo"));
        colDurum.setCellValueFactory(new PropertyValueFactory<>("durum"));
        aracTable.setItems(aracVerileri);

        colSefer.setCellValueFactory(new PropertyValueFactory<>("sefer"));
        colFeribotNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colGiris.setCellValueFactory(new PropertyValueFactory<>("giris"));
        colPlanKalkis.setCellValueFactory(new PropertyValueFactory<>("planKalkis"));
        colGercekKalkis.setCellValueFactory(new PropertyValueFactory<>("gercekKalkis"));
        colGKat.setCellValueFactory(new PropertyValueFactory<>("gKat"));
        colUKat.setCellValueFactory(new PropertyValueFactory<>("uKat"));
        colGecikme.setCellValueFactory(new PropertyValueFactory<>("gecikme"));
        colFeribotDurum.setCellValueFactory(new PropertyValueFactory<>("durum"));
        feribotTable.setItems(feribotVerileri);
    }

    private void renkliHucrelerAyarla() {
        colDurum.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if ("Feribotta".equals(item))     setStyle("-fx-text-fill: #22d3ee; -fx-font-weight: bold;");
                else if ("Bekliyor".equals(item)) setStyle("-fx-text-fill: #f97316;");
                else                              setStyle("");
            }
        });
        colFeribotDurum.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if ("Kalkti".equals(item))        setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                else if ("Rihtimda".equals(item)) setStyle("-fx-text-fill: #22d3ee; -fx-font-weight: bold;");
                else                              setStyle("-fx-text-fill: #475569;");
            }
        });
        colGecikme.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.startsWith("+"))          setStyle("-fx-text-fill: #f87171;");
                else if (item.contains("erken"))   setStyle("-fx-text-fill: #4ade80;");
                else if ("Zamaninda".equals(item)) setStyle("-fx-text-fill: #22d3ee;");
                else                               setStyle("");
            }
        });
    }

    private void katSlotsOlustur() {
        for (int i = 0; i < 5; i++) {
            ustSlotlar[i]   = slotOlustur();
            girisSlotlar[i] = slotOlustur();
            HBox.setHgrow(ustSlotlar[i],   Priority.ALWAYS);
            HBox.setHgrow(girisSlotlar[i], Priority.ALWAYS);
            ustKatBox.getChildren().add(ustSlotlar[i]);
            girisKatBox.getChildren().add(girisSlotlar[i]);
        }
    }

    private Label slotOlustur() {
        Label lbl = new Label("BOŞ");
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER);
        lbl.getStyleClass().add("slot-empty");
        return lbl;
    }

    private void logCellAyarla() {
        logListesi.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String low = item.toLowerCase();
                if (low.contains("simulasyon") || low.contains("basladi") || low.contains("tamamlandi")) {
                    setStyle("-fx-text-fill: #22d3ee; -fx-font-weight: bold; -fx-font-family: Consolas; -fx-font-size: 11px; -fx-padding: 3 12;");
                } else if (low.contains("kalkti") || low.contains("kalkis") || low.contains("yanasti") || low.contains("rihtima")) {
                    setStyle("-fx-text-fill: #f97316; -fx-font-family: Consolas; -fx-font-size: 11px; -fx-padding: 3 12;");
                } else if (low.contains("[hata]") || low.contains("[uyari]") || low.contains("yuklenemedi") || low.contains("bulunamadi")) {
                    setStyle("-fx-text-fill: #f87171; -fx-font-family: Consolas; -fx-font-size: 11px; -fx-padding: 3 12;");
                } else {
                    setStyle("-fx-text-fill: #4ade80; -fx-font-family: Consolas; -fx-font-size: 11px; -fx-padding: 3 12;");
                }
            }
        });
    }

    private void formBaslat() {
        tipBox.getItems().addAll("Agir Vasita", "Otomobil");
        tipBox.getSelectionModel().selectFirst();
    }

    private void controllerBagla() {
        ctrl.setLogCallback(mesaj -> Platform.runLater(() -> {
            logListesi.getItems().add(mesaj);
            logListesi.scrollTo(logListesi.getItems().size() - 1);
        }));
        ctrl.onTickUI = () -> Platform.runLater(this::refreshAll);
    }

    private void dosyalariYukle() {
        ctrl.feribotlariOku();
        ctrl.araclariOku();
        aracTabloGuncelle();
        feribotTabloGuncelle();
    }

    // ══════════════════════════════════════════════
    // FXML EYLEM METOTLARI
    // ══════════════════════════════════════════════

    @FXML private void simulasyonBaslat() {
        ctrl.simulasyonuBaslat();
        if (!ctrl.simRunning) { showAlert("Dosyalar yuklenemedi. Arac.txt ve Feribot.txt mevcut mu?"); return; }
        btnBaslat.setDisable(true);
        btnDurdur.setDisable(false);
        btnDurdur.setText("⏸  Duraklat");
        if (simTimeline != null) simTimeline.stop();
        simTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> ctrl.tick()));
        simTimeline.setCycleCount(Timeline.INDEFINITE);
        simTimeline.play();
        refreshAll();
    }

    @FXML private void simDurdur() {
        if (simTimeline == null) return;
        if (ctrl.simRunning) {
            simTimeline.pause();
            ctrl.simRunning = false;
            btnDurdur.setText("▶  Devam Et");
        } else {
            ctrl.simRunning = true;
            simTimeline.play();
            btnDurdur.setText("⏸  Duraklat");
        }
    }

    @FXML private void adimAdimIlerle() {
        if (ctrl.simFinished) return;
        if (ctrl.simTimeMinutes == SimulationController.SIM_START && !ctrl.simRunning) {
            ctrl.simulasyonuBaslat();
            if (!ctrl.simRunning) { showAlert("Dosyalar yuklenemedi."); return; }
            btnBaslat.setDisable(true);
            btnDurdur.setDisable(false);
        }
        if (simTimeline != null) simTimeline.pause();
        Runnable saved = ctrl.onTickUI;
        ctrl.onTickUI = null;
        ctrl.simRunning = true;
        ctrl.tick();
        ctrl.onTickUI = saved;
        refreshAll();
        if (ctrl.simFinished) simulasyonBitti();
    }

    @FXML private void tamaminSimuleEt() {
        if (ctrl.simFinished) return;
        if (ctrl.simTimeMinutes == SimulationController.SIM_START && !ctrl.simRunning) {
            ctrl.simulasyonuBaslat();
            if (!ctrl.simRunning) { showAlert("Dosyalar yuklenemedi."); return; }
            btnBaslat.setDisable(true);
        }
        if (simTimeline != null) simTimeline.stop();
        Runnable saved = ctrl.onTickUI;
        ctrl.onTickUI = null;
        ctrl.simRunning = true;
        while (!ctrl.simFinished) ctrl.tick();
        ctrl.onTickUI = saved;
        refreshAll();
        simulasyonBitti();
    }

    @FXML private void aracEkleInline() {
        String plaka = plakaField.getText().trim().toUpperCase();
        if (plaka.isEmpty()) { showAlert("Plaka bos olamaz!"); return; }
        String saatMetin = saatField.getText().trim();
        double saat;
        try { saat = Double.parseDouble(saatMetin.replace(',', '.')); }
        catch (NumberFormatException e) { showAlert("Gecersiz saat! Ornek: 9.30"); return; }
        int tip = tipBox.getSelectionModel().getSelectedIndex() == 0 ? 1 : 2;
        if (ctrl.aracEkle(plaka, saat, tip)) {
            plakaField.clear();
            saatField.clear();
            aracTabloGuncelle();
        } else {
            showAlert("Bu plaka sistemde zaten kayitli: " + plaka);
        }
    }

    @FXML private void hashTabloGoster() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/HashTabloView.fxml"));
            Parent root = loader.load();
            HashTabloController c = loader.getController();
            c.init(ctrl);
            Stage stage = new Stage();
            stage.setTitle("Hash Tablosu Icerigi");
            stage.setScene(new Scene(root, 560, 520));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(aracTable.getScene().getWindow());
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void temizle() {
        if (simTimeline != null) simTimeline.stop();
        ctrl.simRunning       = false;
        ctrl.simFinished      = false;
        ctrl.simTimeMinutes   = SimulationController.SIM_START;
        ctrl.aktifFeribotIndex = -1;
        ctrl.yuklemeYolu1     = new Kuyruk();
        ctrl.yuklemeYolu2     = new Kuyruk();
        logListesi.getItems().clear();
        simProgress.setProgress(0);
        simSaatLabel.setText("08:00");
        surePctLabel.setText("0%");
        btnBaslat.setDisable(false);
        btnDurdur.setDisable(true);
        btnDurdur.setText("⏸  Duraklat");
        yol1List.getItems().clear();
        yol2List.getItems().clear();
        yol1BaslikLabel.setText("1. YUKLEME YOLU  (0)");
        yol2BaslikLabel.setText("2. YUKLEME YOLU  (0)");
        resetKatlar();
        setRihtimDurumu(false, null);
        dosyalariYukle();
    }

    // ══════════════════════════════════════════════
    // GÜNCELLEME METOTLARI
    // ══════════════════════════════════════════════

    private void refreshAll() {
        simSaatiGuncelle();
        aracTabloGuncelle();
        feribotTabloGuncelle();
        yollarGuncelle();
        feribotKatGuncelle();
        rihtimDurumGuncelle();
        if (ctrl.simFinished) simulasyonBitti();
    }

    private void simulasyonBitti() {
        if (simTimeline != null) simTimeline.stop();
        btnBaslat.setDisable(false);
        btnDurdur.setDisable(true);
        btnAdim.setDisable(true);
        btnDurdur.setText("⏸  Duraklat");
    }

    private void simSaatiGuncelle() {
        int dakika = ctrl.simTimeMinutes;
        int gecen  = dakika - SimulationController.SIM_START;
        int toplam = SimulationController.SIM_END - SimulationController.SIM_START;
        double pct = Math.max(0, Math.min(1.0, (double) gecen / toplam));
        simSaatLabel.setText(SimulationController.formatTime(dakika));
        surePctLabel.setText((int)(pct * 100) + "%");
        simProgress.setProgress(pct);
    }

    void aracTabloGuncelle() {
        aracVerileri.clear();
        for (int i = 0; i < ctrl.aracSayisi; i++) {
            Arac a = ctrl.araclar[i];
            String durum = "*".equals(a.seferNo) ? "Bekliyor" : "Feribotta";
            String no    = a.aracNo == 0 ? "-" : String.valueOf(a.aracNo);
            aracVerileri.add(new AracSatiri(no, a.aracPlaka, a.aracTipiMetin(),
                    Arac.doubleYazi(a.giseGirisSaati), a.seferNo, durum));
        }
    }

    void feribotTabloGuncelle() {
        feribotVerileri.clear();
        for (int i = 0; i < ctrl.feribotSayisi; i++) {
            Feribot f = ctrl.feribotlar[i];
            feribotVerileri.add(new FeribotSatiri(
                    f.seferNo, String.valueOf(f.feribotNo),
                    saatYaz(f.feribotRihtimGirisSaati),
                    saatYaz(f.feribotRihtimKalkisSaati),
                    gercekKalkisYaz(f),
                    f.girisKatiAracSayisi() + "/5",
                    f.ustKatAracSayisi() + "/5",
                    gecikmeYaz(f),
                    feribotDurumYaz(i, f)));
        }
    }

    private String feribotDurumYaz(int index, Feribot f) {
        if (ctrl.aktifFeribotIndex == index) return "Rihtimda";
        if (f.feribotKalkisaHazirMi)         return "Kalkti";
        return "Bekliyor";
    }

    private String gercekKalkisYaz(Feribot f) {
        return f.feribotKalkisaHazirMi ? saatYaz(f.gercekKalkisSaati) : "--:--";
    }

    private String saatYaz(double saat) {
        return SimulationController.formatTime(SimulationController.doubleToMinutes(saat));
    }

    private String gecikmeYaz(Feribot f) {
        if (!f.feribotKalkisaHazirMi) return "--";
        int plan   = SimulationController.doubleToMinutes(f.feribotRihtimKalkisSaati);
        int gercek = SimulationController.doubleToMinutes(f.gercekKalkisSaati);
        int fark   = gercek - plan;
        if (fark == 0) return "Zamaninda";
        if (fark > 0)  return "+" + fark + " dk gec";
        return (-fark) + " dk erken";
    }

    void yollarGuncelle() {
        int c1 = ctrl.yuklemeYolu1.size();
        int c2 = ctrl.yuklemeYolu2.size();
        yol1BaslikLabel.setText("1. YUKLEME YOLU  (" + c1 + ")");
        yol2BaslikLabel.setText("2. YUKLEME YOLU  (" + c2 + ")");
        kuyrukListeyiDoldur(yol1List, ctrl.yuklemeYolu1);
        kuyrukListeyiDoldur(yol2List, ctrl.yuklemeYolu2);
    }

    private void kuyrukListeyiDoldur(ListView<String> list, Kuyruk kuyruk) {
        list.getItems().clear();
        for (Arac a : kuyruk.diziOlarakAl())
            list.getItems().add(a.aracPlaka + "  (" + a.aracTipiMetin() + ")");
    }

    void feribotKatGuncelle() {
        int index = ctrl.aktifFeribotIndex;
        if (index < 0 || index >= ctrl.feribotSayisi) { resetKatlar(); return; }
        Feribot f = ctrl.feribotlar[index];
        katlariDoldur(f.girisKatiAraclari(), girisSlotlar, "slot-filled-lower");
        katlariDoldur(f.ustKatAraclari(),    ustSlotlar,   "slot-filled-upper");
        int g = f.girisKatiAracSayisi();
        int u = f.ustKatAracSayisi();
        girisKatProgress.setProgress((double) g / Feribot.GIRIS_KAT_KAPASITESI);
        ustKatProgress.setProgress((double) u / Feribot.UST_KAT_KAPASITESI);
        girisKatLabel.setText(g + " / 5");
        ustKatLabel.setText(u + " / 5");
    }

    private void katlariDoldur(Arac[] araclar, Label[] slotlar, String doluClass) {
        for (int i = 0; i < slotlar.length; i++) {
            slotlar[i].getStyleClass().removeAll("slot-empty", "slot-filled-upper", "slot-filled-lower");
            if (i < araclar.length) {
                slotlar[i].setText(araclar[i].aracPlaka);
                slotlar[i].getStyleClass().add(doluClass);
            } else {
                slotlar[i].setText("BOŞ");
                slotlar[i].getStyleClass().add("slot-empty");
            }
        }
    }

    private void resetKatlar() {
        for (int i = 0; i < 5; i++) { resetSlot(girisSlotlar[i]); resetSlot(ustSlotlar[i]); }
        girisKatProgress.setProgress(0);
        ustKatProgress.setProgress(0);
        girisKatLabel.setText("0 / 5");
        ustKatLabel.setText("0 / 5");
    }

    private void resetSlot(Label slot) {
        slot.getStyleClass().removeAll("slot-filled-upper", "slot-filled-lower");
        if (!slot.getStyleClass().contains("slot-empty")) slot.getStyleClass().add("slot-empty");
        slot.setText("BOŞ");
    }

    private void rihtimDurumGuncelle() {
        int idx = ctrl.aktifFeribotIndex;
        if (idx >= 0 && idx < ctrl.feribotSayisi) setRihtimDurumu(true, ctrl.feribotlar[idx]);
        else                                       setRihtimDurumu(false, null);
    }

    private void setRihtimDurumu(boolean aktif, Feribot f) {
        rihtimDurumLabel.getStyleClass().removeAll("status-chip-idle", "status-chip-active");
        feribotDurumLabel.getStyleClass().removeAll("ferry-status-idle", "ferry-status-active");
        if (aktif && f != null) {
            rihtimDurumLabel.setText("RIHTIMDA: " + f.seferNo);
            rihtimDurumLabel.getStyleClass().add("status-chip-active");
            seferNoLabel.setText(f.seferNo);
            feribotNoLabel.setText(String.valueOf(f.feribotNo));
            feribotDurumLabel.setText("Yukleniyor");
            feribotDurumLabel.getStyleClass().add("ferry-status-active");
        } else {
            rihtimDurumLabel.setText("RIHTIM BOS");
            rihtimDurumLabel.getStyleClass().add("status-chip-idle");
            seferNoLabel.setText("---");
            feribotNoLabel.setText("---");
            feribotDurumLabel.setText("Rihtim Bos");
            feribotDurumLabel.getStyleClass().add("ferry-status-idle");
        }
    }

    private void showAlert(String mesaj) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Uyari");
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }
}
