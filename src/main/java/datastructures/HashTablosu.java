package datastructures;

/*
 * Collision Cozumu: SEPARATE CHAINING (Ayri Zincirleme)
 * Her dizi indeksi bir bagla liste basini gosterir.
 * Ayni hash degerine dusen plakalar bu listede sirayla tutulur.
 * Ortalama durum: O(1) ekleme ve arama.
 * En kotu durum: O(n) (tum elemanlar ayni indekste).
 */
public class HashTablosu {
    private static final int TABLO_BOYUTU = 101;
    private HashDugum[] tablo = new HashDugum[TABLO_BOYUTU];

    static class HashDugum {
        String plaka;
        HashDugum sonraki;

        // Hash tablosundaki bagla liste dugumunu olusturur.
        HashDugum(String plaka) {
            this.plaka = plaka;
            this.sonraki = null;
        }
    }

    // Bos bir hash tablosu olusturur.
    public HashTablosu() {
    }

    // Plakayi tablo icin sayisal indekse cevirir.
    public int hashFunction(String plaka) {
        int hash = 0;
        for (int i = 0; i < plaka.length(); i++) {
            hash = (hash * 31 + plaka.charAt(i)) % TABLO_BOYUTU;
        }
        return Math.abs(hash);
    }

    // Plaka yoksa tabloya ekler; varsa false doner.
    public boolean insert(String plaka) {
        if (contains(plaka)) {
            return false;
        }
        int index = hashFunction(plaka);
        HashDugum yeni = new HashDugum(plaka);
        yeni.sonraki = this.tablo[index];
        this.tablo[index] = yeni;
        return true;
    }

    // Plaka tabloda var mi kontrol eder.
    public boolean contains(String plaka) {
        int index = hashFunction(plaka);
        HashDugum dugum = this.tablo[index];
        while (dugum != null) {
            if (dugum.plaka.equals(plaka)) return true;
            dugum = dugum.sonraki;
        }
        return false;
    }

    // Hash tablosunun dolu satirlarini yazi olarak verir.
    public String getIcerik() {
        StringBuilder sonuc = new StringBuilder();
        for (int i = 0; i < this.tablo.length; i++) {
            if (this.tablo[i] != null) {
                sonuc.append("tablo[").append(i).append("]: ");
                sonuc.append(zincirYaz(this.tablo[i])).append("\n");
            }
        }
        if (sonuc.length() == 0) return "(Hash tablosu bos)";
        return sonuc.toString();
    }

    // Ayni indekse dusen plakalari sirayla yazar.
    private String zincirYaz(HashDugum dugum) {
        StringBuilder sonuc = new StringBuilder();
        while (dugum != null) {
            sonuc.append("[").append(dugum.plaka).append("]");
            dugum = dugum.sonraki;
            if (dugum != null) sonuc.append(" -> ");
        }
        return sonuc.toString();
    }
}
