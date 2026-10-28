package logic;

import datastructures.HashTablosu;
import datastructures.Kuyruk;
import model.Arac;
import model.Feribot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SimulationController {
    public static final String FERIBOT_DOSYA = "Feribot.txt";
    public static final String ARAC_DOSYA    = "Arac.txt";
    public static final int MAX       = 1000;
    public static final int SIM_START = 480;
    public static final int SIM_END   = 1200;

    public Feribot[] feribotlar  = new Feribot[MAX];
    public int       feribotSayisi = 0;
    public Arac[]    araclar     = new Arac[MAX];
    public int       aracSayisi  = 0;

    public HashTablosu hashTablosu  = new HashTablosu();
    public Kuyruk      yuklemeYolu1 = new Kuyruk();
    public Kuyruk      yuklemeYolu2 = new Kuyruk();

    public int     simTimeMinutes  = SIM_START;
    public boolean simRunning      = false;
    public boolean simFinished     = false;
    public int     aktifFeribotIndex = -1;
    public Runnable onTickUI;

    private int     vehicleNextIdx = 0;
    private int     laneToggle     = 0;
    private int     nextFeribotIdx = 0;
    private boolean loadLane1Next  = true;
    private LogDinleyici logDinleyici;

    public interface LogDinleyici {
        // Yeni log mesajini alir.
        void logYaz(String mesaj);
    }

    // Kontrolcu nesnesini olusturur.
    public SimulationController() {}

    // Log mesajlarini arayuze gonderecek dinleyiciyi ayarlar.
    public void setLogCallback(LogDinleyici dinleyici) {
        this.logDinleyici = dinleyici;
    }

    // Mesaji saat bilgisiyle birlikte log alanina yollar.
    private void log(String mesaj) {
        if (this.logDinleyici != null)
            this.logDinleyici.logYaz("[" + formatTime(this.simTimeMinutes) + "] " + mesaj);
    }

    // Dosyadan gelen saat yazisini double degere cevirir.
    private static double parseD(String metin) {
        metin = metin.trim().replace(',', '.');
        return Double.parseDouble(metin);
    }

    // 9.30 gibi saat degerini toplam dakikaya cevirir.
    public static int doubleToMinutes(double saatDegeri) {
        int saat   = (int) saatDegeri;
        int dakika = (int) ((saatDegeri - saat) * 100 + 0.5);
        return saat * 60 + dakika;
    }

    // Toplam dakikayi 9.30 gibi double saat degerine cevirir.
    public static double minutesToDouble(int toplamDakika) {
        int saat   = toplamDakika / 60;
        int dakika = toplamDakika % 60;
        return saat + dakika / 100.0;
    }

    // Toplam dakikayi 09:30 biciminde yazar.
    public static String formatTime(int toplamDakika) {
        int saat   = toplamDakika / 60;
        int dakika = toplamDakika % 60;
        return (saat < 10 ? "0" : "") + saat + ":" + (dakika < 10 ? "0" : "") + dakika;
    }

    // Feribot.txt dosyasindaki feribotlari okur.
    public void feribotlariOku() {
        this.feribotSayisi = 0;
        try (BufferedReader okuyucu = new BufferedReader(new FileReader(FERIBOT_DOSYA))) {
            String satir;
            while ((satir = okuyucu.readLine()) != null) feribotSatiriEkle(satir);
            log(this.feribotSayisi + " feribot Feribot.txt'den okundu.");
        } catch (FileNotFoundException e) {
            log("[HATA] Feribot.txt bulunamadi.");
        } catch (IOException e) {
            log("[HATA] Feribot.txt: " + e.getMessage());
        }
    }

    // Tek feribot satirini parcala ve diziye ekle.
    private void feribotSatiriEkle(String satir) {
        satir = satir.trim();
        if (satir.isEmpty() || this.feribotSayisi >= MAX) return;
        String[] parcalar = satir.split("-");
        if (parcalar.length < 5) return;
        String  seferNo    = parcalar[0].trim();
        int     feribotNo  = Integer.parseInt(parcalar[1].trim());
        double  girisSaati = parseD(parcalar[2]);
        double  kalkisSaati= parseD(parcalar[3]);
        boolean hazirMi    = Boolean.parseBoolean(parcalar[4].trim());
        this.feribotlar[this.feribotSayisi++] = new Feribot(seferNo, feribotNo, girisSaati, kalkisSaati, hazirMi);
    }

    // Arac.txt dosyasindaki araclari okur.
    public void araclariOku() {
        this.aracSayisi = 0;
        this.hashTablosu = new HashTablosu();
        try (BufferedReader okuyucu = new BufferedReader(new FileReader(ARAC_DOSYA))) {
            String satir;
            while ((satir = okuyucu.readLine()) != null) aracSatiriEkle(satir);
            log(this.aracSayisi + " arac Arac.txt'den okundu.");
        } catch (FileNotFoundException e) {
            log("[HATA] Arac.txt bulunamadi.");
        } catch (IOException e) {
            log("[HATA] Arac.txt: " + e.getMessage());
        }
    }

    // Tek arac satirini parcala ve plaka tekrar etmiyorsa diziye ekle.
    private void aracSatiriEkle(String satir) {
        satir = satir.trim();
        if (satir.isEmpty() || this.aracSayisi >= MAX) return;
        String[] parcalar = satir.split("-");
        if (parcalar.length < 5) return;
        String plaka    = parcalar[2].trim();
        double giseSaati= parseD(parcalar[3]);
        int    aracTipi = Integer.parseInt(parcalar[4].trim());
        if (!this.hashTablosu.insert(plaka)) { log("[UYARI] Tekrar plaka: " + plaka); return; }
        this.araclar[this.aracSayisi++] = new Arac("*", 0, plaka, giseSaati, aracTipi);
    }

    // Yeni araci bellekteki listeye ve Arac.txt dosyasina ekler.
    public boolean aracEkle(String plaka, double giseSaati, int aracTipi) {
        if (this.hashTablosu.contains(plaka)) { log("[UYARI] Plaka zaten kayitli: " + plaka); return false; }
        this.hashTablosu.insert(plaka);
        this.araclar[this.aracSayisi++] = new Arac("*", 0, plaka, giseSaati, aracTipi);
        araciDosyayaYaz(plaka, giseSaati, aracTipi);
        log("Yeni arac eklendi: " + plaka);
        return true;
    }

    // Yeni eklenen araci dosyanin sonuna yazar.
    private void araciDosyayaYaz(String plaka, double giseSaati, int aracTipi) {
        try (FileWriter yazici = new FileWriter(ARAC_DOSYA, true)) {
            yazici.write("\n*-*-" + plaka + "-" + Arac.doubleYazi(giseSaati) + "-" + aracTipi);
        } catch (IOException e) {
            log("[HATA] Dosyaya yazilamadi: " + e.getMessage());
        }
    }

    // Simulasyonu ilk durumuna getirip baslatir.
    public void simulasyonuBaslat() {
        feribotlariOku();
        araclariOku();
        if (this.feribotSayisi == 0 || this.aracSayisi == 0) { log("[!] Dosyalar yuklu degil!"); return; }
        resetSimulasyon();
        araclariSirala();
        feribotlariSirala();
        aracNumaralariniVer();
        log("=== SIMULASYON BASLADI (08:00 -> 20:00) ===");
    }

    // Simulasyon icin zaman ve kuyruk bilgilerini sifirlar.
    private void resetSimulasyon() {
        this.simTimeMinutes    = SIM_START;
        this.simRunning        = true;
        this.simFinished       = false;
        this.vehicleNextIdx    = 0;
        this.laneToggle        = 0;
        this.nextFeribotIdx    = 0;
        this.aktifFeribotIndex = -1;
        this.loadLane1Next     = true;
        this.yuklemeYolu1      = new Kuyruk();
        this.yuklemeYolu2      = new Kuyruk();
    }

    private void aracNumaralariniVer() {
        for (int i = 0; i < this.aracSayisi; i++) this.araclar[i].aracNo = i + 1;
    }

    // Simulasyonu bir dakika ilerletir.
    public void tick() {
        if (!this.simRunning) return;
        this.simTimeMinutes++;
        if (this.simTimeMinutes > SIM_END) {
            this.simRunning  = false;
            this.simFinished = true;
            log("=== 20:00 SIMULASYON TAMAMLANDI ===");
            arayuzuGuncelle();
            return;
        }
        zamaniGelenAraclariYolaAl();
        zamaniGelenFeribotuRihtimaAl();
        aktifFeribotaAracYukle();
        arayuzuGuncelle();
    }

    // Gise saati gelen araclari sirayla iki yukleme yoluna dagitir.
    private void zamaniGelenAraclariYolaAl() {
        while (this.vehicleNextIdx < this.aracSayisi
                && this.simTimeMinutes >= doubleToMinutes(this.araclar[this.vehicleNextIdx].giseGirisSaati)) {
            Arac arac = this.araclar[this.vehicleNextIdx];
            if (this.laneToggle % 2 == 0) { this.yuklemeYolu1.enqueue(arac); log(arac.aracPlaka + " -> 1.Yukleme Yolu"); }
            else                           { this.yuklemeYolu2.enqueue(arac); log(arac.aracPlaka + " -> 2.Yukleme Yolu"); }
            this.laneToggle++;
            this.vehicleNextIdx++;
        }
    }

    // Giris saati gelen feribotu rihtima alir.
    private void zamaniGelenFeribotuRihtimaAl() {
        if (this.aktifFeribotIndex != -1 || this.nextFeribotIdx >= this.feribotSayisi) return;
        Feribot feribot = this.feribotlar[this.nextFeribotIdx];
        if (this.simTimeMinutes < doubleToMinutes(feribot.feribotRihtimGirisSaati)) return;
        this.aktifFeribotIndex = this.nextFeribotIdx++;
        this.loadLane1Next     = true;
        log("Feribot rihtima yanasti: " + feribot.seferNo
                + " (Planlanan kalkis: " + formatTime(doubleToMinutes(feribot.feribotRihtimKalkisSaati)) + ")");
    }

    // Aktif feribota siradaki yoldan bir arac yukler.
    private void aktifFeribotaAracYukle() {
        if (this.aktifFeribotIndex < 0) return;
        Feribot feribot = this.feribotlar[this.aktifFeribotIndex];
        if (feribot.feribotKalkisaHazirMi) return;
        yukleBirArac(feribot);
        feribotKalkacakMi(feribot);
    }

    private void feribotKalkacakMi(Feribot feribot) {
        if (feribot.tamamenDoluMu()) {
            feribot.feribotKalkisaHazirMi = true;
            feribot.gercekKalkisSaati     = minutesToDouble(this.simTimeMinutes);
            log("[ERKEN KALKIS] " + feribot.seferNo + " doldu ve kalkti.");
            this.aktifFeribotIndex = -1;
            return;
        }
        double simSaat = minutesToDouble(this.simTimeMinutes);
        if (feribot.kalkisKontrol(simSaat)) {
            log("Feribot kalkti: " + feribot.seferNo
                    + " Gercek kalkis: " + formatTime(doubleToMinutes(feribot.gercekKalkisSaati)));
            this.aktifFeribotIndex = -1;
        }
    }

    private void yukleBirArac(Feribot feribot) {
        if (this.loadLane1Next) kuyruktanFeribotaYukle(this.yuklemeYolu1, feribot, "Y1");
        else                    kuyruktanFeribotaYukle(this.yuklemeYolu2, feribot, "Y2");
        this.loadLane1Next = !this.loadLane1Next;
    }

    private void kuyruktanFeribotaYukle(Kuyruk kuyruk, Feribot feribot, String yolAdi) {
        if (kuyruk.isEmpty()) return;
        Arac arac = kuyruk.peek();
        if (feribot.aracYukle(arac)) {
            kuyruk.dequeue();
            log("  [" + yolAdi + "->F] " + arac.aracPlaka + " (" + arac.aracTipiMetin() + ") yuklendi -> " + feribot.seferNo);
        } else {
            log("  [" + yolAdi + "] " + arac.aracPlaka + " yuklenemedi (kat dolu).");
        }
    }

    private void arayuzuGuncelle() {
        if (this.onTickUI != null) this.onTickUI.run();
    }

    // Hash tablosunun ekranda gosterilecek icerigini verir.
    public String getHashTabloIcerigi() {
        return this.hashTablosu.getIcerik();
    }

    private void araclariSirala() {
        for (int i = 0; i < this.aracSayisi - 1; i++)
            for (int j = 0; j < this.aracSayisi - 1 - i; j++)
                if (this.araclar[j].giseGirisSaati > this.araclar[j + 1].giseGirisSaati) {
                    Arac gecici = this.araclar[j]; this.araclar[j] = this.araclar[j+1]; this.araclar[j+1] = gecici;
                }
    }

    private void feribotlariSirala() {
        for (int i = 0; i < this.feribotSayisi - 1; i++)
            for (int j = 0; j < this.feribotSayisi - 1 - i; j++)
                if (this.feribotlar[j].feribotRihtimGirisSaati > this.feribotlar[j+1].feribotRihtimGirisSaati) {
                    Feribot gecici = this.feribotlar[j]; this.feribotlar[j] = this.feribotlar[j+1]; this.feribotlar[j+1] = gecici;
                }
    }
}
