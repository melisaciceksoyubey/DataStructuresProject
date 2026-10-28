package model;

public class Arac {
    public String seferNo;
    public int aracNo;
    public String aracPlaka;
    public double giseGirisSaati;
    public int aracTipi;

    // Arac bilgilerini saklar.
    public Arac(String seferNo, int aracNo, String aracPlaka, double giseGirisSaati, int aracTipi) {
        this.seferNo = seferNo;
        this.aracNo = aracNo;
        this.aracPlaka = aracPlaka;
        this.giseGirisSaati = giseGirisSaati;
        this.aracTipi = aracTipi;
    }

    // Arac tipinin ekranda gorunecek adini verir.
    public String aracTipiMetin() {
        if (this.aracTipi == 1) return "Agir Vasita";
        return "Otomobil";
    }

    // 9.3 gibi degeri 9.30 gibi iki basamakli yaziya cevirir.
    public static String doubleYazi(double deger) {
        int saat = (int) deger;
        int dakika = (int) ((deger - saat) * 100 + 0.5);
        if (dakika >= 100) { saat++; dakika -= 100; }
        if (dakika < 10) return saat + ".0" + dakika;
        return saat + "." + dakika;
    }

    // Aracin kisa ozet bilgisini verir.
    public String toString() {
        return "AracNo=" + this.aracNo
                + " Plaka=" + this.aracPlaka
                + " Tip=" + this.aracTipiMetin()
                + " GiseSaati=" + doubleYazi(this.giseGirisSaati)
                + " SeferNo=" + this.seferNo;
    }
}
