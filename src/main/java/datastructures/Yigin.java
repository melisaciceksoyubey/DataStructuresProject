package datastructures;

import model.Arac;

// Bagla liste (LinkedList) tabanli Yigin (Stack) veri yapisi.
// LIFO: Son giren ilk cikar.
public class Yigin {
    private Node tepe;
    private int boyut;

    // Bos bir yigin olusturur.
    public Yigin() {
        this.tepe = null;
        this.boyut = 0;
    }

    // Yigin bos mu kontrol eder.
    public boolean isEmpty() {
        return this.boyut == 0;
    }

    // Yigindaki eleman sayisini verir.
    public int size() {
        return this.boyut;
    }

    // Tepedeki araci yigindan cikarmadan verir.
    public Arac peek() {
        if (isEmpty()) return null;
        return (Arac) this.tepe.veri;
    }

    // Araci yiginin tepesine ekler.
    public void push(Arac arac) {
        Node yeni = new Node(arac);
        yeni.sonraki = this.tepe;
        this.tepe = yeni;
        this.boyut++;
    }

    // Tepedeki araci yigindan cikarip verir.
    public Arac pop() {
        if (isEmpty()) return null;
        Arac arac = (Arac) this.tepe.veri;
        this.tepe = this.tepe.sonraki;
        this.boyut--;
        return arac;
    }

    // Yigindaki araclari eklenme sirasina gore diziye aktarir.
    public Arac[] diziOlarakAl() {
        Arac[] araclar = new Arac[this.boyut];
        Node simdiki = this.tepe;
        int index = this.boyut - 1;
        while (simdiki != null) {
            araclar[index] = (Arac) simdiki.veri;
            simdiki = simdiki.sonraki;
            index--;
        }
        return araclar;
    }
}
