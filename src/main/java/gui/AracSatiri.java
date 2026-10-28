package gui;

// TableView satir modeli - Arac verileri icin.
public class AracSatiri {
    private final String no, plaka, tip, giseSaati, seferNo, durum;

    public AracSatiri(String no, String plaka, String tip, String giseSaati, String seferNo, String durum) {
        this.no       = no;
        this.plaka    = plaka;
        this.tip      = tip;
        this.giseSaati= giseSaati;
        this.seferNo  = seferNo;
        this.durum    = durum;
    }

    public String getNo()        { return no; }
    public String getPlaka()     { return plaka; }
    public String getTip()       { return tip; }
    public String getGiseSaati() { return giseSaati; }
    public String getSeferNo()   { return seferNo; }
    public String getDurum()     { return durum; }
}
