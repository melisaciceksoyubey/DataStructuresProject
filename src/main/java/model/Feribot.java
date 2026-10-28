package model;

import datastructures.Yigin;

public class Feribot {
    public static final int GIRIS_KAT_KAPASITESI = 5;
    public static final int UST_KAT_KAPASITESI   = 5;

    public String  seferNo;
    public int     feribotNo;
    public double  feribotRihtimGirisSaati;
    public double  feribotRihtimKalkisSaati;
    public boolean feribotKalkisaHazirMi;
    public double  gercekKalkisSaati;
    public double  sonAracGirisSaati;

    private Yigin girisKati;
    private Yigin ustKat;

    // Feribotu ve iki kat icin bos yiginlari olusturur.
    public Feribot(String seferNo, int feribotNo, double girisSaati, double kalkisSaati, boolean hazirMi) {
        this.seferNo                    = seferNo;
        this.feribotNo                  = feribotNo;
        this.feribotRihtimGirisSaati    = girisSaati;
        this.feribotRihtimKalkisSaati   = kalkisSaati;
        this.feribotKalkisaHazirMi      = hazirMi;
        this.gercekKalkisSaati          = kalkisSaati;
        this.sonAracGirisSaati          = girisSaati;
        this.girisKati = new Yigin();
        this.ustKat    = new Yigin();
    }

    public int girisKatiAracSayisi() { return this.girisKati.size(); }
    public int ustKatAracSayisi()    { return this.ustKat.size(); }

    public boolean girisKatiDoluMu() { return girisKatiAracSayisi() >= GIRIS_KAT_KAPASITESI; }
    public boolean ustKatDoluMu()    { return ustKatAracSayisi()    >= UST_KAT_KAPASITESI; }
    public boolean tamamenDoluMu()   { return girisKatiDoluMu() && ustKatDoluMu(); }

    // Araci tipine gore uygun kata yukler.
    public boolean aracYukle(Arac arac) {
        if (arac.aracTipi == 1 && !girisKatiDoluMu()) {
            this.girisKati.push(arac);
        } else if (arac.aracTipi == 2 && !ustKatDoluMu()) {
            this.ustKat.push(arac);
        } else {
            return false;
        }
        arac.seferNo = this.seferNo;
        this.sonAracGirisSaati = arac.giseGirisSaati;
        return true;
    }

    // Iki kat da en az yuzde 50 dolu mu kontrol eder.
    private boolean herIkiKatYuzde50DoluMu() {
        int girisYarisi = (GIRIS_KAT_KAPASITESI + 1) / 2;
        int ustYarisi   = (UST_KAT_KAPASITESI   + 1) / 2;
        return girisKatiAracSayisi() >= girisYarisi && ustKatAracSayisi() >= ustYarisi;
    }

    private boolean birKat100DoluMu() { return girisKatiDoluMu() || ustKatDoluMu(); }

    // Feribotun kalkis sartlarini kontrol eder.
    public boolean kalkisKontrol(double anlikSaat) {
        boolean zamaniGeldi = anlikSaat >= this.feribotRihtimKalkisSaati;
        boolean kalkabilir  = tamamenDoluMu()
                || (zamaniGeldi && birKat100DoluMu())
                || (zamaniGeldi && herIkiKatYuzde50DoluMu());
        if (kalkabilir) {
            this.feribotKalkisaHazirMi = true;
            this.gercekKalkisSaati     = anlikSaat;
        }
        return kalkabilir;
    }

    public Arac[] girisKatiAraclari() { return this.girisKati.diziOlarakAl(); }
    public Arac[] ustKatAraclari()    { return this.ustKat.diziOlarakAl(); }

    public double gecikme() { return this.gercekKalkisSaati - this.feribotRihtimKalkisSaati; }

    public String toString() {
        return "Feribot[" + this.seferNo
                + " No:" + this.feribotNo
                + " Giris:" + this.feribotRihtimGirisSaati
                + " PlanKalkis:" + this.feribotRihtimKalkisSaati + "]";
    }
}
