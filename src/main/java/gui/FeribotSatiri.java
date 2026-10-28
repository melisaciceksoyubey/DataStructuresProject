package gui;

// TableView satir modeli - Feribot verileri icin.
public class FeribotSatiri {
    private final String sefer, no, giris, planKalkis, gercekKalkis, gKat, uKat, gecikme, durum;

    public FeribotSatiri(String sefer, String no, String giris, String planKalkis,
                         String gercekKalkis, String gKat, String uKat, String gecikme, String durum) {
        this.sefer        = sefer;
        this.no           = no;
        this.giris        = giris;
        this.planKalkis   = planKalkis;
        this.gercekKalkis = gercekKalkis;
        this.gKat         = gKat;
        this.uKat         = uKat;
        this.gecikme      = gecikme;
        this.durum        = durum;
    }

    public String getSefer()        { return sefer; }
    public String getNo()           { return no; }
    public String getGiris()        { return giris; }
    public String getPlanKalkis()   { return planKalkis; }
    public String getGercekKalkis() { return gercekKalkis; }
    public String getGKat()         { return gKat; }
    public String getUKat()         { return uKat; }
    public String getGecikme()      { return gecikme; }
    public String getDurum()        { return durum; }
}
